package tw.com.conference.service.impl;

import tw.com.conference.pojo.DTO.addEntityDTO.AddAttendeeEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutAttendeeEventDTO;
import tw.com.conference.pojo.BO.MemberEventStatusBO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.convert.AttendeeEventConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.mapper.AttendeeEventMapper;
import tw.com.conference.mapper.AttendeeMapper;
import tw.com.conference.mapper.EventMapper;
import tw.com.conference.service.AttendeeEventService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 與會者-參加活動 表 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-18
 */
@Service
@RequiredArgsConstructor
public class AttendeeEventServiceImpl extends ServiceImpl<AttendeeEventMapper, AttendeeEvent>
		implements AttendeeEventService {

	private final AttendeeEventConvert attendeeEventConvert;
	private final EventMapper eventMapper;
	private final AttendeeMapper attendeeMapper;

	@Override
	public AttendeeEvent get(Long attendeeEventId) {
		return baseMapper.selectById(attendeeEventId);
	}

	@Override
	public long countByEventId(Long eventId) {
		return baseMapper.countByEventId(eventId);
	}

	@Override
	public AttendeeEvent create(AddAttendeeEventDTO addAttendeeEventDTO) {
		AttendeeEvent attendeeEvent = attendeeEventConvert.addDTOToEntity(addAttendeeEventDTO);
		baseMapper.insert(attendeeEvent);
		return attendeeEvent;
	}

	/**
	 * 批次建立報名紀錄
	 *
	 * @param memberId
	 * @param eventIds
	 * @param isPaid   報名時的付款狀態
	 */
	private void batchCreateRecords(Long memberId, Collection<Long> eventIds, CommonStatusEnum isPaid) {

		if (eventIds == null || eventIds.isEmpty()) {
			return;
		}

		// 若會員已是與會者，新的報名紀錄直接帶上 attendee_id
		LambdaQueryWrapper<Attendee> attendeeWrapper = new LambdaQueryWrapper<>();
		attendeeWrapper.eq(Attendee::getMemberId, memberId);
		Attendee attendee = attendeeMapper.selectOne(attendeeWrapper);
		Long attendeeId = attendee != null ? attendee.getAttendeeId() : null;

		// 組裝參加的活動紀錄
		List<AttendeeEvent> attendeeEventList = eventIds.stream().map(eventId -> {
			AttendeeEvent attendeeEvent = new AttendeeEvent();
			attendeeEvent.setMemberId(memberId);
			attendeeEvent.setAttendeeId(attendeeId);
			attendeeEvent.setEventId(eventId);
			attendeeEvent.setIsPaid(isPaid);
			return attendeeEvent;
		}).toList();

		// 批量新增
		this.saveOrUpdateBatch(attendeeEventList);
	}

	@Override
	public void linkAttendee(Long memberId, Long attendeeId) {
		baseMapper.updateAttendeeIdByMemberId(memberId, attendeeId);
	}

	@Override
	public void batchCreateUnpaidRecords(Long memberId, Collection<Long> eventIds) {
		this.batchCreateRecords(memberId, eventIds, CommonStatusEnum.NO);
	}

	@Override
	public void batchCreatePaidRecords(Long memberId, Collection<Long> eventIds) {
		this.batchCreateRecords(memberId, eventIds, CommonStatusEnum.YES);
	}

	@Override
	public AttendeeEvent getByMemberAndEvent(Long memberId, Long eventId) {
		return baseMapper.selectByMemberAndEvent(memberId, eventId);
	}

	@Override
	public AttendeeEvent getByAttendeeAndEvent(Long attendeeId, Long eventId) {
		return baseMapper.selectByAttendeeAndEvent(attendeeId, eventId);
	}

	@Override
	public long countPaidByEventId(Long eventId) {
		return baseMapper.countPaidByEventId(eventId);
	}

	@Override
	public List<AttendeeEvent> findPaidByEventId(Long eventId) {
		return baseMapper.selectByEventIdAndPaid(eventId, CommonStatusEnum.YES);
	}

	@Override
	public List<AttendeeEvent> findByMemberId(Long memberId) {
		return baseMapper.selectByMemberId(memberId);
	}

	@Override
	public List<MemberEventStatusBO> findEventStatusByMemberId(Long memberId) {
		List<AttendeeEvent> attendeeEventList = baseMapper.selectByMemberId(memberId);
		if (attendeeEventList.isEmpty()) {
			return Collections.emptyList();
		}
		// 一次撈出所有相關活動，補上標題與是否主活動
		List<Long> eventIds = attendeeEventList.stream().map(AttendeeEvent::getEventId).distinct().toList();
		Map<Long, Event> eventById = eventMapper.selectBatchIds(eventIds)
				.stream()
				.collect(Collectors.toMap(Event::getEventId, Function.identity()));

		return attendeeEventList.stream().map(attendeeEvent -> {
			Event event = eventById.get(attendeeEvent.getEventId());
			return new MemberEventStatusBO(attendeeEvent.getEventId(), event != null ? event.getTitle() : null,
					event != null ? event.getIsMain() : CommonStatusEnum.NO, attendeeEvent.getIsPaid());
		}).toList();
	}

	@Override
	public boolean isEventPaid(Long memberId, Long eventId) {
		AttendeeEvent attendeeEvent = baseMapper.selectByMemberAndEvent(memberId, eventId);
		return attendeeEvent != null && CommonStatusEnum.YES.equals(attendeeEvent.getIsPaid());
	}

	@Override
	public Map<Long, CommonStatusEnum> getPaidMapByEventAndMemberIds(Long eventId, Collection<Long> memberIds) {
		if (memberIds == null || memberIds.isEmpty()) {
			return Collections.emptyMap();
		}
		// 查到的以實際狀態為準，沒查到的視為未付款
		Map<Long, CommonStatusEnum> paidMap = baseMapper.selectByEventIdAndMemberIds(eventId, memberIds)
				.stream()
				.collect(Collectors.toMap(AttendeeEvent::getMemberId, AttendeeEvent::getIsPaid, (a, b) -> a));
		memberIds.forEach(memberId -> paidMap.putIfAbsent(memberId, CommonStatusEnum.NO));
		return paidMap;
	}

	@Override
	public List<Long> findMemberIdsByEventAndPaid(Long eventId, CommonStatusEnum isPaid) {
		return baseMapper.selectByEventIdAndPaid(eventId, isPaid)
				.stream()
				.map(AttendeeEvent::getMemberId)
				.distinct()
				.toList();
	}

	@Override
	public void update(PutAttendeeEventDTO putAttendeeEventDTO) {
		AttendeeEvent attendeeEvent = attendeeEventConvert.putDTOToEntity(putAttendeeEventDTO);
		baseMapper.updateById(attendeeEvent);
	}
	
	@Override
	public void batchConfirmPayment(Long memberId, Collection<Long> eventIds) {
		baseMapper.updatePaymentStatusByMemberAndEvents(memberId,eventIds);
	}

	@Override
	public void remove(Long attendeeEventId) {
		baseMapper.deleteById(attendeeEventId);
	}



}
