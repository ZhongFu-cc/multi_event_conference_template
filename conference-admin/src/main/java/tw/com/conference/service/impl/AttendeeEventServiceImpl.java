package tw.com.conference.service.impl;

import tw.com.conference.pojo.DTO.addEntityDTO.AddAttendeeEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutAttendeeEventDTO;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.convert.AttendeeEventConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.mapper.AttendeeEventMapper;
import tw.com.conference.service.AttendeeEventService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

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

	@Override
	public void batchCreateUnpaidRecords(Long memberId, Collection<Long> eventIds) {

		if (eventIds != null && eventIds.isEmpty()) {
			return;
		}

		// 組裝參加的活動紀錄
		List<AttendeeEvent> AttendeeEventList = eventIds.stream().map(eventId -> {
			AttendeeEvent attendeeEvent = new AttendeeEvent();
			attendeeEvent.setMemberId(memberId);
			attendeeEvent.setEventId(eventId);
			attendeeEvent.setIsPaid(CommonStatusEnum.NO);
			return attendeeEvent;
		}).toList();

		// 批量新增
		this.saveOrUpdateBatch(AttendeeEventList);

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
