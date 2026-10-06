package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.BO.PresenceStatsBO;
import tw.com.conference.pojo.entity.CheckinRecord;

/**
 * <p>
 * 簽到退紀錄 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2025-05-14
 */
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

	// 因為是For Excel 匯出使用，所以按照與會者排序
	@Select("SELECT * FROM checkin_record WHERE is_deleted = 0 ORDER BY attendee_id, action_time")
	List<CheckinRecord> selectCheckinRecords();

	// For Excel 匯出使用，只取某一場次的紀錄
	@Select("SELECT cr.* FROM checkin_record cr "
			+ "JOIN attendee_event ae ON ae.attendee_event_id = cr.attendee_event_id "
			+ "WHERE cr.is_deleted = 0 AND ae.event_id = #{eventId} "
			+ "ORDER BY cr.attendee_id, cr.action_time")
	List<CheckinRecord> selectCheckinRecordsByEventId(@Param("eventId") Long eventId);

	/**
	 * 查詢 實到 人數 (整體)
	 *
	 * @return
	 */
	@Select("SELECT COUNT(DISTINCT attendee_id) FROM checkin_record WHERE is_deleted = 0 AND action_type = 1")
	Integer countCheckedIn();

	/**
	 * 查詢某場次的 實到 人數<br>
	 * 以 attendee_event_id 為單位，所以同一人在不同場次各自計算
	 *
	 * @param eventId
	 * @return
	 */
	@Select("SELECT COUNT(DISTINCT cr.attendee_event_id) FROM checkin_record cr "
			+ "JOIN attendee_event ae ON ae.attendee_event_id = cr.attendee_event_id "
			+ "WHERE cr.is_deleted = 0 AND cr.action_type = 1 AND ae.event_id = #{eventId}")
	Integer countCheckedInByEventId(@Param("eventId") Long eventId);

	/**
	 * 查詢 尚在會場 和 已離場 人數 (整體, 以 attendee_id 為單位)
	 *
	 * @return
	 */
	PresenceStatsBO selectPresenceStats();

	/**
	 * 查詢某場次 尚在會場 和 已離場 人數 (以 attendee_event_id 為單位)
	 *
	 * @param eventId
	 * @return
	 */
	PresenceStatsBO selectPresenceStatsByEventId(@Param("eventId") Long eventId);

	/**
	 * 查詢某筆報名紀錄(場次)的所有簽到/退紀錄
	 *
	 * @param attendeeEventId
	 * @return
	 */
	default List<CheckinRecord> selectByAttendeeEventId(Long attendeeEventId) {
		LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(CheckinRecord::getAttendeeEventId, attendeeEventId)
				.orderByAsc(CheckinRecord::getActionTime);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢某筆報名紀錄(場次)最新的一筆簽到/退紀錄
	 *
	 * @param attendeeEventId
	 * @return 沒有紀錄則為 null
	 */
	default CheckinRecord selectLatestByAttendeeEventId(Long attendeeEventId) {
		LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(CheckinRecord::getAttendeeEventId, attendeeEventId)
				.orderByDesc(CheckinRecord::getCheckinRecordId)
				.last("LIMIT 1");
		return this.selectOne(queryWrapper);
	}

	/**
	 * 查詢多筆報名紀錄(場次)最新的一筆簽到/退紀錄<br>
	 * 用於組裝單場報到名單
	 *
	 * @param attendeeEventIds
	 * @return
	 */
	default List<CheckinRecord> selectByAttendeeEventIds(Collection<Long> attendeeEventIds) {
		LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(CheckinRecord::getAttendeeEventId, attendeeEventIds)
				.orderByAsc(CheckinRecord::getActionTime);
		return this.selectList(queryWrapper);
	}

}
