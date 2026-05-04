package tw.com.conference.service;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutEventDTO;
import tw.com.conference.pojo.entity.Event;

/**
 * <p>
 * 活動事件表 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface EventService extends IService<Event> {

	/**
	 * 判斷是否有任何活動事件
	 * @return
	 */
	boolean existAny();
	
	/**
	 * 查詢單一事件活動
	 * @param eventId
	 * @return
	 */
	Event get(Long eventId);
	
	/**
	 * 新增活動事件
	 * @param addEventDTO
	 */
	void create(AddEventDTO addEventDTO); 
	
	/**
	 * 修改活動事件
	 * @param putEventDTO
	 */
	void update(PutEventDTO putEventDTO);
	
	/**
	 * 刪除活動事件
	 * @param eventId
	 */
	void remove(Long eventId);
	
}
