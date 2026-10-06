package tw.com.conference.manager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.AttendeeConvert;
import tw.com.conference.convert.CheckinRecordConvert;
import tw.com.conference.enums.CheckinActionTypeEnum;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.handler.AttendeeVOHandler;
import tw.com.conference.pojo.DTO.addEntityDTO.AddCheckinRecordDTO;
import tw.com.conference.pojo.VO.AttendeeVO;
import tw.com.conference.pojo.VO.CheckinRecordVO;
import tw.com.conference.pojo.VO.EventCheckinVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.CheckinRecord;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.excelPojo.AttendeeExcel;
import tw.com.conference.pojo.excelPojo.CheckinRecordExcel;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.CheckinRecordService;
import tw.com.conference.service.EventService;
import tw.com.conference.service.MemberService;

@Component
@RequiredArgsConstructor
public class CheckinRecordManager {

	private final MemberService memberService;
	private final CheckinRecordService checkinRecordService;
	private final CheckinRecordConvert checkinRecordConvert;
	private final AttendeeService attendeeService;
	private final AttendeeEventService attendeeEventService;
	private final EventService eventService;
	private final AttendeeConvert attendeeConvert;
	private final AttendeeVOHandler attendeeVOHandler;

	/**
	 * 獲得此筆簽到退資料 及 簽到者身分
	 * 
	 * @param checkinRecordId
	 * @return
	 */
	public CheckinRecordVO getCheckinRecordVO(Long checkinRecordId) {

		// 1.獲取這筆簽到記錄
		CheckinRecord checkinRecord = checkinRecordService.getCheckinRecord(checkinRecordId);

		// 2.查詢此簽到者的基本資訊
		AttendeeVO attendeeVO = attendeeVOHandler.getAttendeeVO(checkinRecord.getAttendeeId());

		// 3.實體類轉換成VO
		CheckinRecordVO checkinRecordVO = checkinRecordConvert.entityToVO(checkinRecord);

		// 4.vo中填入與會者VO對象  2025/9/24 重構臨時註解
		checkinRecordVO.setAttendeeVO(attendeeVO);

		// 5.填入這筆報到所屬的場次資訊 (舊資料的 attendeeEventId 可能為 null)
		this.fillEventInfo(checkinRecordVO, checkinRecord.getAttendeeEventId());

		return checkinRecordVO;
	}

	/**
	 * 依 attendeeEventId 補上 VO 的場次資訊
	 *
	 * @param vo
	 * @param attendeeEventId 可為 null (報到紀錄尚未綁定場次)
	 */
	private void fillEventInfo(CheckinRecordVO vo, Long attendeeEventId) {
		if (attendeeEventId == null) {
			return;
		}
		AttendeeEvent attendeeEvent = attendeeEventService.get(attendeeEventId);
		if (attendeeEvent == null) {
			return;
		}
		vo.setEventId(attendeeEvent.getEventId());
		Event event = eventService.get(attendeeEvent.getEventId());
		if (event != null) {
			vo.setEventTitle(event.getTitle());
		}
	}

	/**
	 * 轉換 簽到/退紀錄,並補上簽到者資料
	 * 
	 * @param checkinRecordList
	 * @return
	 */
	private List<CheckinRecordVO> convertToCheckinRecordVOList(List<CheckinRecord> checkinRecordList) {

		// 1.獲取與會者的ID(去重)
		Set<Long> attendeesIdSet = checkinRecordList.stream()
				.map(CheckinRecord::getAttendeeId)
				.collect(Collectors.toSet());

		// 2.透過去重的與會者ID拿到資料
		List<AttendeeVO> attendeesVOList = attendeeVOHandler.getAttendeesVOsByAttendeesIds(attendeesIdSet);

		// 3.做成資料映射attendeesID 對應 AttendeesVO
		Map<Long, AttendeeVO> AttendeesVOMap = attendeesVOList.stream()
				.collect(Collectors.toMap(AttendeeVO::getAttendeeId, Function.identity()));

		// 4.checkinRecordList stream轉換後映射組裝成VO對象
		List<CheckinRecordVO> checkinRecordVOList = checkinRecordList.stream().map(checkinRecord -> {
			CheckinRecordVO vo = checkinRecordConvert.entityToVO(checkinRecord);
			vo.setAttendeeVO(AttendeesVOMap.get(checkinRecord.getAttendeeId()));
			return vo;
		}).collect(Collectors.toList());

		return checkinRecordVOList;
	}

	/**
	 * 獲取CheckinRecordVO 列表
	 * 
	 * @return
	 */
	public List<CheckinRecordVO> getCheckinRecordVOList() {

		// 1.獲取所有簽到/退紀錄
		List<CheckinRecord> checkinRecordList = checkinRecordService.getCheckinRecordList();

		// 2.使用私有方法獲取CheckinRecordVOList
		List<CheckinRecordVO> checkinRecordVOList = this.convertToCheckinRecordVOList(checkinRecordList);

		return checkinRecordVOList;
	}

	/**
	 * 獲取CheckinRecordVO 分頁對象
	 * 
	 * @param page
	 * @return
	 */
	public IPage<CheckinRecordVO> getCheckinRecordVOPage(Page<CheckinRecord> page) {
		// 1.獲取簽到記錄分頁對象
		IPage<CheckinRecord> checkinRecordPage = checkinRecordService.getCheckinRecordPage(page);

		// 2.轉換資料拿到CheckinRecordVO對向
		List<CheckinRecordVO> checkinRecordVOList = this.convertToCheckinRecordVOList(checkinRecordPage.getRecords());

		// 3.封裝成VOpage
		Page<CheckinRecordVO> checkinRecordVOPage = new Page<>(checkinRecordPage.getCurrent(),
				checkinRecordPage.getSize(), checkinRecordPage.getTotal());
		checkinRecordVOPage.setRecords(checkinRecordVOList);

		return checkinRecordVOPage;
	}

	/**
	 * 新增簽到記錄
	 * 
	 * @param addCheckinRecordDTO
	 * @return
	 */
	public CheckinRecordVO addCheckinRecord(AddCheckinRecordDTO addCheckinRecordDTO) {
		// 1.新增簽到/退紀錄
		CheckinRecord checkinRecord = checkinRecordService.addCheckinRecord(addCheckinRecordDTO);

		// 2.組裝VO對象並返回
		return this.getCheckinRecordVO(checkinRecord.getCheckinRecordId());
	}

	/**
	 * 單場活動的報到名單<br>
	 * 以該場「已繳費的報名紀錄」為基底，left join 其簽到/退紀錄
	 *
	 * @param eventId
	 * @param isCheckedIn 選填; 帶 YES 只回已簽到過的, 帶 NO 只回尚未簽到的, null 為全部
	 * @return
	 */
	public List<EventCheckinVO> getEventCheckinList(Long eventId, CommonStatusEnum isCheckedIn) {

		// 1.該場次所有已繳費的報名紀錄 (應到名單)
		List<AttendeeEvent> paidRecords = attendeeEventService.findPaidByEventId(eventId);
		if (paidRecords.isEmpty()) {
			return Collections.emptyList();
		}

		// 2.一次撈出這些報名紀錄的簽到/退紀錄 (已按 action_time 升冪)
		Map<Long, List<CheckinRecord>> checkinMap = checkinRecordService.getCheckinMapByAttendeeEventIds(
				paidRecords.stream().map(AttendeeEvent::getAttendeeEventId).toList());

		// 3.一次撈出與會者與會員資料
		Map<Long, Attendee> attendeeMap = attendeeService.getAttendeeMap();
		Map<Long, Member> memberMap = memberService
				.getMemberMapByIds(paidRecords.stream().map(AttendeeEvent::getMemberId).distinct().toList());

		// 4.組裝VO
		return paidRecords.stream().map(attendeeEvent -> {
			List<CheckinRecord> records = checkinMap.getOrDefault(attendeeEvent.getAttendeeEventId(),
					Collections.emptyList());

			EventCheckinVO vo = new EventCheckinVO();
			vo.setAttendeeEventId(attendeeEvent.getAttendeeEventId());
			vo.setAttendeeId(attendeeEvent.getAttendeeId());
			vo.setMemberId(attendeeEvent.getMemberId());

			Attendee attendee = attendeeMap.get(attendeeEvent.getAttendeeId());
			if (attendee != null) {
				vo.setSequenceNo(attendee.getSequenceNo());
			}

			Member member = memberMap.get(attendeeEvent.getMemberId());
			if (member != null) {
				vo.setChineseName(member.getChineseName());
				vo.setFirstName(member.getFirstName());
				vo.setLastName(member.getLastName());
				vo.setEmail(member.getEmail());
				vo.setAffiliation(member.getAffiliation());
				vo.setJobTitle(member.getJobTitle());
			}

			// 有任何一筆簽到紀錄就算報到過
			boolean checkedIn = records.stream()
					.anyMatch(r -> CheckinActionTypeEnum.CHECKIN.getValue().equals(r.getActionType()));
			vo.setIsCheckedIn(CommonStatusEnum.fromBoolean(checkedIn));

			if (!records.isEmpty()) {
				// 最後一筆動作為簽到 → 仍在會場
				CheckinRecord last = records.get(records.size() - 1);
				vo.setLastActionTime(last.getActionTime());
				vo.setIsOnSite(CommonStatusEnum
						.fromBoolean(CheckinActionTypeEnum.CHECKIN.getValue().equals(last.getActionType())));

				records.stream()
						.filter(r -> CheckinActionTypeEnum.CHECKIN.getValue().equals(r.getActionType()))
						.findFirst()
						.ifPresent(first -> vo.setFirstCheckinTime(first.getActionTime()));
			} else {
				vo.setIsOnSite(CommonStatusEnum.NO);
			}

			return vo;
		})
				// 5.依報到狀態篩選
				.filter(vo -> isCheckedIn == null || isCheckedIn.equals(vo.getIsCheckedIn()))
				.toList();
	}

	/**
	 * 下載所有簽到/退紀錄
	 *
	 * @param response
	 * @throws IOException
	 */
	public void downloadExcel(HttpServletResponse response, Long eventId) throws IOException {

		// 1.初始設定; 有指定場次時檔名帶上場次標題
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		String baseName = "簽到退紀錄名單";
		if (eventId != null) {
			Event event = eventService.get(eventId);
			if (event != null) {
				baseName = baseName + "-" + event.getTitle();
			}
		}
		// 这里URLEncoder.encode可以防止中文乱码 ， 和easyexcel没有关系
		String fileName = URLEncoder.encode(baseName, "UTF-8").replaceAll("\\+", "%20");
		response.setHeader("Content-disposition", "attachment;filename*=" + fileName + ".xlsx");

		// 2.高效獲取簽到/退資料 (有指定場次則只取該場)
		List<CheckinRecord> checkinRecordList = eventId == null
				? checkinRecordService.getCheckinRecordsEfficiently()
				: checkinRecordService.getCheckinRecordsEfficiently(eventId);

		// 3.高效獲取所有會員資料映射
		Map<Long, Member> memberMap = memberService.getMemberMap();

		// 4.高效獲取所有與會者資料映射
		Map<Long, Attendee> attendeesMap = attendeeService.getAttendeeMap();

		// 5.取得報名紀錄 → 活動標題 的映射，用來標示每筆報到屬於哪一場
		Map<Long, String> eventTitleByAttendeeEventId = this.resolveEventTitles(checkinRecordList);

		// 資料轉換成Excel
		List<CheckinRecordExcel> excelData = checkinRecordList.stream().map(checkinRecord -> {
			// 透過attendeesId先拿到attendeesVO
			AttendeeVO attendeesVO = attendeeConvert.entityToVO(attendeesMap.get(checkinRecord.getAttendeeId()));
			// 再透過 memberId放入Member
			attendeesVO.setMember(memberMap.get(attendeesVO.getMemberId()));
			// 獲取到AttendeesExcel 再轉換成 CheckinRecordExcel
			AttendeeExcel attendeesExcel = attendeeConvert.voToExcel(attendeesVO);
			CheckinRecordExcel checkinRecordExcel = checkinRecordConvert
					.attendeeExcelToCheckinRecordExcel(attendeesExcel);

			//最後再補上缺失的屬性
			checkinRecordExcel.setActionTime(checkinRecord.getActionTime());
			checkinRecordExcel.setActionType(CheckinActionTypeEnum.fromValue(checkinRecord.getActionType()).getLabel());
			checkinRecordExcel.setLocation(checkinRecord.getLocation());
			checkinRecordExcel.setCheckinRecordId(checkinRecord.getCheckinRecordId().toString());
			checkinRecordExcel.setRemark(checkinRecord.getRemark());
			checkinRecordExcel.setEventTitle(eventTitleByAttendeeEventId.get(checkinRecord.getAttendeeEventId()));
			return checkinRecordExcel;

		}).collect(Collectors.toList());

		EasyExcel.write(response.getOutputStream(), CheckinRecordExcel.class).sheet("簽到退紀錄列表").doWrite(excelData);

	}

	/**
	 * 取得 報名紀錄ID → 活動標題 的映射<br>
	 * 舊資料的 attendee_event_id 可能為 null，這類紀錄不會出現在映射中
	 *
	 * @param checkinRecordList
	 * @return
	 */
	private Map<Long, String> resolveEventTitles(List<CheckinRecord> checkinRecordList) {

		List<Long> attendeeEventIds = checkinRecordList.stream()
				.map(CheckinRecord::getAttendeeEventId)
				.filter(java.util.Objects::nonNull)
				.distinct()
				.toList();
		if (attendeeEventIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<AttendeeEvent> attendeeEvents = attendeeEventService.listByIds(attendeeEventIds);
		Map<Long, Event> eventById = eventService.list()
				.stream()
				.collect(Collectors.toMap(Event::getEventId, Function.identity()));

		return attendeeEvents.stream()
				.filter(ae -> eventById.containsKey(ae.getEventId()))
				.collect(Collectors.toMap(AttendeeEvent::getAttendeeEventId,
						ae -> eventById.get(ae.getEventId()).getTitle()));
	}

}
