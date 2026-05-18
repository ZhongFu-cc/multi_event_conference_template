package tw.com.conference.service;

import com.baomidou.mybatisplus.extension.service.IService;

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
	 * 新增活動參與
	 * 
	 * @param addAttendeeEventDTO
	 */
	AttendeeEvent create(AddAttendeeEventDTO addAttendeeEventDTO);

	/**
	 * 修改活動參與
	 * 
	 * @param putAttendeeEventDTO
	 */
	void update(PutAttendeeEventDTO putAttendeeEventDTO);

	/**
	 * 刪除活動參與
	 * 
	 * @param attendeeEventId
	 */
	void remove(Long attendeeEventId);
}
