package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.pojo.entity.AttendeeEvent;

/**
 * <p>
 * 與會者-參加活動 表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-18
 */
public interface AttendeeEventMapper extends BaseMapper<AttendeeEvent> {

	/**
	 * 更新付款狀態
	 * 
	 * @param memberId 會員ID
	 * @param eventIds 活動事件IDs
	 */
	default void updatePaymentStatusByMemberAndEvents(Long memberId, Collection<Long> eventIds) {
		LambdaUpdateWrapper<AttendeeEvent> updateWrapper = new LambdaUpdateWrapper<>();

		// 設定更新欄位
		updateWrapper.set(AttendeeEvent::getIsPaid, CommonStatusEnum.YES)
				// 設定條件
				.eq(AttendeeEvent::getMemberId, memberId)
				.in(AttendeeEvent::getEventId, eventIds);

		// 執行更新
		this.update(updateWrapper);
	}

	/**
	 * 查詢某場次的應到人數 (已繳費的報名者)
	 *
	 * @param eventId
	 * @return
	 */
	default long countPaidByEventId(Long eventId) {
		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getEventId, eventId).eq(AttendeeEvent::getIsPaid, CommonStatusEnum.YES);
		return this.selectCount(queryWrapper);
	}

	/**
	 * 查詢符合eventId的總數
	 * 
	 * @param eventId
	 * @return
	 */
	default long countByEventId(Long eventId) {

		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getEventId, eventId);

		return this.selectCount(queryWrapper);

	}

	/**
	 * 會員成為與會者後，回填該會員所有報名紀錄的 attendee_id
	 *
	 * @param memberId
	 * @param attendeeId
	 */
	default void updateAttendeeIdByMemberId(Long memberId, Long attendeeId) {
		LambdaUpdateWrapper<AttendeeEvent> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(AttendeeEvent::getAttendeeId, attendeeId)
				.eq(AttendeeEvent::getMemberId, memberId)
				.isNull(AttendeeEvent::getAttendeeId);
		this.update(updateWrapper);
	}

	/**
	 * 查詢與會者在某活動的報名紀錄 (報到端使用，手上拿到的是 attendeeId)
	 *
	 * @param attendeeId
	 * @param eventId
	 * @return 沒有報名則為 null
	 */
	default AttendeeEvent selectByAttendeeAndEvent(Long attendeeId, Long eventId) {
		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getAttendeeId, attendeeId).eq(AttendeeEvent::getEventId, eventId);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 查詢會員在某活動的報名紀錄
	 *
	 * @param memberId
	 * @param eventId
	 * @return 沒有報名則為 null
	 */
	default AttendeeEvent selectByMemberAndEvent(Long memberId, Long eventId) {
		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getMemberId, memberId).eq(AttendeeEvent::getEventId, eventId);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 查詢某會員的所有報名紀錄
	 *
	 * @param memberId
	 * @return
	 */
	default List<AttendeeEvent> selectByMemberId(Long memberId) {
		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getMemberId, memberId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢某活動 + 付款狀態 的報名紀錄
	 *
	 * @param eventId
	 * @param isPaid  null 為不限
	 * @return
	 */
	default List<AttendeeEvent> selectByEventIdAndPaid(Long eventId, CommonStatusEnum isPaid) {
		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getEventId, eventId).eq(isPaid != null, AttendeeEvent::getIsPaid, isPaid);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢某活動中，指定會員的報名紀錄
	 *
	 * @param eventId
	 * @param memberIds
	 * @return
	 */
	default List<AttendeeEvent> selectByEventIdAndMemberIds(Long eventId, Collection<Long> memberIds) {
		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getEventId, eventId).in(AttendeeEvent::getMemberId, memberIds);
		return this.selectList(queryWrapper);
	}
}
