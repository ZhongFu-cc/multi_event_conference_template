package tw.com.conference.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeTag;
import tw.com.conference.pojo.entity.Tag;

/**
 * <p>
 * 與會者 與 標籤 的關聯表 服务类
 * </p>
 *
 * @author Joey
 * @since 2025-05-14
 */
public interface AttendeeTagService extends IService<AttendeeTag> {

	
	/**
	 * 根據 attendeeId 查詢與之有關tagIds關聯
	 * 
	 * @param attendeeId
	 * @return
	 */
	Set<Long> getTagIdsByAttendeeId(Long attendeeId);
	
	/**
	 * 根據 attendeeId 查詢與之有關的所有Tag關聯
	 * 
	 * @param attendeeId
	 * @return
	 */
	List<AttendeeTag> getAttendeeTagsByAttendeeId(Long attendeeId);
	
	
	/**
	 * 拿到與會者持有的Tag
	 * @param attendeeId
	 * @return
	 */
	List<Tag> getTagsByAttendeeId(Long attendeeId);
	
	/**
	 * 根據 attendeeIds 查詢 與會者ID 和 標籤 關聯關係 的映射
	 * 
	 * @param attendeeIds
	 * @return
	 */
	Map<Long, List<Long>> getAttendeeTagMapByAttendeeIds(Collection<Long> attendeeIds);

	/**
	 * 根據 attendeeList, 獲取範圍內與會者的 標籤映射對象
	 * 
	 * @param attendeeList
	 * @return 獲得以attendeeId為key , List<Tag> 為值的 映射對象
	 */
	Map<Long, List<Tag>> getTagMapByAttendeeId(Collection<Attendee> attendeeList);
	
	/**
	 * 根據 tagId 查詢與之有關的所有Attendee關聯
	 * 
	 * @param tagId
	 * @return
	 */
	List<AttendeeTag> getAttendeeTagByTagId(Long tagId);

	/**
	 * 根據複數 attendeeId 查詢與之有關的所有Tag關聯
	 * 
	 * @param attendeeIds
	 * @return
	 */
	List<AttendeeTag> getAttendeeTagsByAttendeeIds(Collection<Long> attendeeIds);

	/**
	 * 根據複數 tagId 查詢與之有關的所有Tag關聯
	 * 
	 * @param tagIds
	 * @return
	 */
	List<AttendeeTag> getAttendeeTagsByTagIds(Collection<Long> tagIds);

	/**
	 * 為一個tag和attendee新增關聯
	 * 
	 * @param attendeeTag
	 */
	void addAttendeeTag(AttendeeTag attendeeTag);
	
	/**
	 * 透過與會者ID 和 標籤ID 建立關聯
	 * 
	 * @param attendeeId 與會者ID
	 * @param tagId 標籤ID
	 */
	 void addAttendeeTag(Long attendeeId, Long tagId);

	 /**
	  * 為與會者新增多個Tag
	  * 
	  * @param attendeeId
	  * @param tagsToAdd
	  */
	 void addTagsToAttendee(Long attendeeId, Collection<Long> tagsToAdd);
	 
	 /**
	  * 根據標籤 ID 新增多個與會者 關聯
	  * 
	  * @param attendeeId
	  * @param tagsToAdd
	  */
	 void addAttendeesToTag(Long tagId, Collection<Long> attendeesToAdd);

	/**
	 * 為與會者移除多個Tag
	 * 
	 * @param attendeeId
	 * @param tagsToRemove
	 */
	void removeTagsFromAttendee(Long attendeeId, Collection<Long> tagsToRemove);
	
	/**
	 * 根據標籤 ID 刪除多個與會者 關聯
	 * 
	 * @param tagId
	 * @param attendeesToRemove
	 */
	void removeAttendeesFromTag(Long tagId, Set<Long> attendeesToRemove);
	

	
}
