package tw.com.conference.service.impl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.CheckinRecordConvert;
import tw.com.conference.enums.CheckinActionTypeEnum;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.exception.CheckinRecordException;
import tw.com.conference.mapper.AttendeeEventMapper;
import tw.com.conference.mapper.CheckinRecordMapper;
import tw.com.conference.pojo.BO.CheckinInfoBO;
import tw.com.conference.pojo.BO.PresenceStatsBO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddCheckinRecordDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutCheckinRecordDTO;
import tw.com.conference.pojo.VO.CheckinRecordVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.pojo.entity.CheckinRecord;
import tw.com.conference.service.CheckinRecordService;

/**
 * <p>
 * 簽到退紀錄 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2025-05-14
 */
@Service
@RequiredArgsConstructor
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord>
		implements CheckinRecordService {

	private final CheckinRecordConvert checkinRecordConvert;
	private final AttendeeEventMapper attendeeEventMapper;

	@Override
	public CheckinRecord getCheckinRecord(Long checkinRecordId) {
		return baseMapper.selectById(checkinRecordId);
	}

	@Override
	public List<CheckinRecord> getCheckinRecordList() {
		return baseMapper.selectList(null);
	}

	@Override
	public List<CheckinRecord> getCheckinRecordsEfficiently() {
		return baseMapper.selectCheckinRecords();
	}

	@Override
	public List<CheckinRecord> getCheckinRecordsEfficiently(Long eventId) {
		return baseMapper.selectCheckinRecordsByEventId(eventId);
	}

	@Override
	public List<CheckinRecord> getCheckinRecordByAttendeeId(Long attendeeId) {
		// 找到這個與會者所有的checkin紀錄
		LambdaQueryWrapper<CheckinRecord> checkinRecordWrapper = new LambdaQueryWrapper<>();
		checkinRecordWrapper.eq(CheckinRecord::getAttendeeId, attendeeId);
		return baseMapper.selectList(checkinRecordWrapper);
	}
	
	@Override
	public long getCheckinRecordCountByAttendeeId(Long attendeeId) {
		// 找到這個與會者所有的checkin紀錄總數
		LambdaQueryWrapper<CheckinRecord> checkinRecordWrapper = new LambdaQueryWrapper<>();
		checkinRecordWrapper.eq(CheckinRecord::getAttendeeId, attendeeId);
		return baseMapper.selectCount(checkinRecordWrapper);
	}

	@Override
	public List<CheckinRecord> getCheckinRecordByAttendeeIds(Collection<Long> attendeeIds) {
		if (attendeeIds.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<CheckinRecord> checkinRecordWrapper = new LambdaQueryWrapper<>();
		checkinRecordWrapper.in(CheckinRecord::getAttendeeId, attendeeIds);
		return baseMapper.selectList(checkinRecordWrapper);

	}

	@Override
	public IPage<CheckinRecord> getCheckinRecordPage(Page<CheckinRecord> page) {
		return baseMapper.selectPage(page, null);
	}

	@Override
	public Map<Long, List<CheckinRecord>> getCheckinMapByAttendeeList(Collection<Attendee> attendeeList) {
		// 1.提取與會者列表的ID
		Set<Long> attendeeIdSet = attendeeList.stream().map(Attendee::getAttendeeId).collect(Collectors.toSet());
		// 2.拿到符合與會者ID列表的 所有簽到退紀錄
		List<CheckinRecord> checkinRecords = this.getCheckinRecordByAttendeeIds(attendeeIdSet);
		// 3.如果簽到記錄為空,直接返回
		if (checkinRecords.isEmpty()) {
			return Collections.emptyMap();
		}
		// 4.根據 attendeesId 群組化
		return checkinRecords.stream().collect(Collectors.groupingBy(CheckinRecord::getAttendeeId));
	}

	@Override
	public Map<Long, Boolean> getCheckinStatusMap(Map<Long, List<CheckinRecord>> checkinMap) {
		// 1.預定義用來儲存與會者的簽到狀態
		Map<Long, Boolean> statusMap = new HashMap<>();

		// 2.透過Map.entrySet, 獲取key,value 的每次遍歷值
		for (Map.Entry<Long, List<CheckinRecord>> entry : checkinMap.entrySet()) {
			CheckinRecord latest = entry.getValue()
					.stream()
					.max(Comparator.comparing(CheckinRecord::getCheckinRecordId))
					.orElse(null);

			boolean isCheckedIn = latest != null
					&& CheckinActionTypeEnum.CHECKIN.getValue().equals(latest.getActionType());
			statusMap.put(entry.getKey(), isCheckedIn);
		}
		return statusMap;
	}

	@Override
	public CheckinRecordVO walkInRegistration(Long attendeesId, Long attendeeEventId) {
		// 1.幫現場註冊的與會者產生簽到記錄
		CheckinRecord checkinRecord = new CheckinRecord();
		checkinRecord.setAttendeeId(attendeesId);
		checkinRecord.setAttendeeEventId(attendeeEventId);
		checkinRecord.setActionType(CheckinActionTypeEnum.CHECKIN.getValue());
		checkinRecord.setActionTime(LocalDateTime.now());
		baseMapper.insert(checkinRecord);

		// 2.返回簽到時的顯示格式
		return checkinRecordConvert.entityToVO(checkinRecord);

	}

	@Override
	public CheckinRecord addCheckinRecord(AddCheckinRecordDTO addCheckinRecordDTO) {

		// 1.確認這位與會者有報名該場活動，且已完成繳費
		AttendeeEvent attendeeEvent = this.resolvePaidAttendeeEvent(addCheckinRecordDTO.getAttendeesId(),
				addCheckinRecordDTO.getEventId());

		// 2.查詢「該場次」最新的一筆
		// 以 attendee_event_id 為單位，所以同一人在其他場次的簽到/退不會互相干擾
		CheckinRecord latestRecord = baseMapper
				.selectLatestByAttendeeEventId(attendeeEvent.getAttendeeEventId());

		// 3.如果完全沒資料，代表他沒簽到過， 再判斷此次動作是否為簽退，如果是則拋出異常
		if (latestRecord == null
				&& CheckinActionTypeEnum.CHECKOUT.getValue().equals(addCheckinRecordDTO.getActionType())) {
			throw new CheckinRecordException("此場活動沒有簽到記錄，不可簽退");
		}

		// 4.最新數據不為null，判斷是否操作行為一致，如果一致，拋出異常，告知不可連續簽到 或 簽退
		if (latestRecord != null && latestRecord.getActionType().equals(addCheckinRecordDTO.getActionType())) {
			throw new CheckinRecordException("此場活動不可連續簽到 或 連續簽退");
		}

		// 5.轉換成entity對象
		CheckinRecord checkinRecord = checkinRecordConvert.addDTOToEntity(addCheckinRecordDTO);
		checkinRecord.setAttendeeEventId(attendeeEvent.getAttendeeEventId());
		checkinRecord.setActionTime(LocalDateTime.now());

		// 6.新增進資料庫
		baseMapper.insert(checkinRecord);

		// 7.準備返回的數據
		return checkinRecord;

	}

	/**
	 * 取得「該與會者 + 該場活動」的報名紀錄，並檢查報到資格<br>
	 * 繳費後才能報到：未報名或未繳費都直接擋下
	 *
	 * @param attendeeId
	 * @param eventId
	 * @return
	 */
	private AttendeeEvent resolvePaidAttendeeEvent(Long attendeeId, Long eventId) {

		AttendeeEvent attendeeEvent = attendeeEventMapper.selectByAttendeeAndEvent(attendeeId, eventId);
		if (attendeeEvent == null) {
			throw new CheckinRecordException("此與會者未報名該場活動");
		}

		if (!CommonStatusEnum.YES.equals(attendeeEvent.getIsPaid())) {
			throw new CheckinRecordException("該場活動尚未完成繳費，請先完成繳費");
		}

		return attendeeEvent;
	}

	@Override
	public void undoLastCheckin(Long attendeeId, Long eventId) {
		// 確認報名與繳費資格，並取得該場次的報名紀錄
		AttendeeEvent attendeeEvent = this.resolvePaidAttendeeEvent(attendeeId, eventId);

		// 查詢此與會者「在該場次」的最後一筆簽到/退資料
		CheckinRecord checkinRecord = baseMapper.selectLatestByAttendeeEventId(attendeeEvent.getAttendeeEventId());
		if (checkinRecord == null) {
			throw new CheckinRecordException("此與會者在該場活動尚未簽到或簽退");
		}

		// 如果最後一筆資料為簽到,則刪除此筆簽到資料
		if (checkinRecord.getActionType().equals(CheckinActionTypeEnum.CHECKIN.getValue())) {
			baseMapper.deleteById(checkinRecord);
			return;
		}

		throw new CheckinRecordException("最後一筆資料不是簽到行為，無法撤銷");

	};

	@Override
	public void updateCheckinRecord(PutCheckinRecordDTO putCheckinRecordDTO) {
		CheckinRecord checkinRecord = checkinRecordConvert.putDTOToEntity(putCheckinRecordDTO);
		baseMapper.updateById(checkinRecord);
	}

	@Override
	public void deleteCheckinRecord(Long checkinRecordId) {
		baseMapper.deleteById(checkinRecordId);
	}

	@Override
	public void deleteCheckinRecordByAttendeeId(Long attendeeId) {
		LambdaQueryWrapper<CheckinRecord> checkinRecordWrapper = new LambdaQueryWrapper<>();
		checkinRecordWrapper.eq(CheckinRecord::getAttendeeId, attendeeId);
		baseMapper.delete(checkinRecordWrapper);
	}

	@Override
	public void deleteCheckinRecordList(List<Long> checkinRecordIds) {
		for (Long checkinRecordId : checkinRecordIds) {
			this.deleteCheckinRecord(checkinRecordId);
		}
	}

	@Override
	public Integer getCountCheckedIn() {
		return baseMapper.countCheckedIn();
	}

	@Override
	public PresenceStatsBO getPresenceStats() {
		return baseMapper.selectPresenceStats();
	}

	@Override
	public Integer getCountCheckedInByEventId(Long eventId) {
		return baseMapper.countCheckedInByEventId(eventId);
	}

	@Override
	public PresenceStatsBO getPresenceStatsByEventId(Long eventId) {
		return baseMapper.selectPresenceStatsByEventId(eventId);
	}

	@Override
	public Map<Long, List<CheckinRecord>> getCheckinMapByAttendeeEventIds(Collection<Long> attendeeEventIds) {
		if (attendeeEventIds == null || attendeeEventIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return baseMapper.selectByAttendeeEventIds(attendeeEventIds)
				.stream()
				.collect(Collectors.groupingBy(CheckinRecord::getAttendeeEventId));
	}

	@Override
	public CheckinInfoBO getLastCheckinRecordByAttendeeId(Long attendeeId) {
		// 先找到這個與會者所有的checkin紀錄
		LambdaQueryWrapper<CheckinRecord> checkinRecordWrapper = new LambdaQueryWrapper<>();
		checkinRecordWrapper.eq(CheckinRecord::getAttendeeId, attendeeId);
		List<CheckinRecord> checkinRecordList = baseMapper.selectList(checkinRecordWrapper);

		// 創建簡易簽到/退紀錄的BO對象
		CheckinInfoBO checkinInfoBO = new CheckinInfoBO();
		LocalDateTime checkinTime = null;
		LocalDateTime checkoutTime = null;

		// 遍歷所有簽到/退紀錄
		for (CheckinRecord record : checkinRecordList) {
			// 如果此次紀錄為 '簽到'
			if (record.getActionType() == 1) {
				// 在簽到時間為null 或者 遍歷對象的執行時間 早於 當前簽到時間的數值
				if (checkinTime == null || record.getActionTime().isBefore(checkinTime)) {
					// checkinTime的值進行覆蓋
					checkinTime = record.getActionTime();
				}
				// 如果此次紀錄為 '簽退'
			} else if (record.getActionType() == 2) {
				// 在簽到時間為null 或者 遍歷對象的執行時間 晚於 當前簽退時間的數值
				if (checkoutTime == null || record.getActionTime().isAfter(checkoutTime)) {
					checkoutTime = record.getActionTime();
				}
			}
		}

		// 將最早的簽到時間 和 最晚的簽退時間,組裝到BO對象中
		checkinInfoBO.setCheckinTime(checkinTime);
		checkinInfoBO.setCheckoutTime(checkoutTime);

		return checkinInfoBO;
	}



}
