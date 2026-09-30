package tw.com.conference.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.pojo.BO.MemberEventStatusBO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddAttendeeEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutAttendeeEventDTO;
import tw.com.conference.pojo.entity.AttendeeEvent;

/**
 * <p>
 * 與會者-參加活動 表 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-18
 */
public interface AttendeeEventService extends IService<AttendeeEvent> {

	/**
	 * 查詢單個活動參與
	 * 
	 * @param attendeeEventId
	 * @return
	 */
	AttendeeEvent get(Long attendeeEventId);

	/**
	 * 查詢該活動的報名人數
	 * 
	 * @param eventId
	 * @return
	 */
	long countByEventId(Long eventId);

	/**
	 * 新增活動參與
	 * 
	 * @param addAttendeeEventDTO
	 */
	AttendeeEvent create(AddAttendeeEventDTO addAttendeeEventDTO);

	/**
	 * 新增未付款的報名紀錄
	 * 
	 * @param memberId
	 * @param eventIds
	 * @return
	 */
	void batchCreateUnpaidRecords(Long memberId, Collection<Long> eventIds);

	/**
	 * 修改活動參與
	 * 
	 * @param putAttendeeEventDTO
	 */
	void update(PutAttendeeEventDTO putAttendeeEventDTO);
	
	
	void batchConfirmPayment(Long memberId, Collection<Long> eventIds);

	/**
	 * 新增已付款的報名紀錄 (後台新增 / 現場報到 等免費報名使用)
	 *
	 * @param memberId
	 * @param eventIds
	 */
	void batchCreatePaidRecords(Long memberId, Collection<Long> eventIds);

	/**
	 * 會員成為與會者後，回填該會員所有報名紀錄的 attendee_id<br>
	 * 讓後續報到 / 分組可以直接以 attendee_event 關聯與會者
	 *
	 * @param memberId
	 * @param attendeeId
	 */
	void linkAttendee(Long memberId, Long attendeeId);

	/**
	 * 查詢會員在某活動的報名紀錄
	 *
	 * @param memberId
	 * @param eventId
	 * @return 沒有報名則為 null
	 */
	AttendeeEvent getByMemberAndEvent(Long memberId, Long eventId);

	/**
	 * 查詢會員所有的報名紀錄
	 *
	 * @param memberId
	 * @return
	 */
	List<AttendeeEvent> findByMemberId(Long memberId);

	/**
	 * 查詢會員所有的報名活動 及 各自的繳費狀態 (含活動標題)
	 *
	 * @param memberId
	 * @return
	 */
	List<MemberEventStatusBO> findEventStatusByMemberId(Long memberId);

	/**
	 * 會員是否已付款 某活動
	 *
	 * @param memberId
	 * @param eventId
	 * @return
	 */
	boolean isEventPaid(Long memberId, Long eventId);

	/**
	 * 批次查詢 指定會員 在某活動的付款狀態<br>
	 * 沒有報名紀錄的會員 視為 未付款 (NO)
	 *
	 * @param eventId
	 * @param memberIds
	 * @return memberId -> isPaid
	 */
	Map<Long, CommonStatusEnum> getPaidMapByEventAndMemberIds(Long eventId, Collection<Long> memberIds);

	/**
	 * 查詢報名某活動 且 符合付款狀態 的會員ID
	 *
	 * @param eventId
	 * @param isPaid  null 為不限 (即所有報名者)
	 * @return
	 */
	List<Long> findMemberIdsByEventAndPaid(Long eventId, CommonStatusEnum isPaid);

	/**
	 * 刪除活動參與
	 * 
	 * @param attendeeEventId
	 */
	void remove(Long attendeeEventId);
}
