package tw.com.conference.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.mapper.AttendeeMapper;
import tw.com.conference.mapper.AttendeeTagMapper;
import tw.com.conference.mapper.TagMapper;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeTag;
import tw.com.conference.pojo.entity.Tag;
import tw.com.conference.service.AttendeeTagService;

/**
 * <p>
 * 與會者 與 標籤 的關聯表 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2025-05-14
 */
@Service
@RequiredArgsConstructor
public class AttendeeTagServiceImpl extends ServiceImpl<AttendeeTagMapper, AttendeeTag>
		implements AttendeeTagService {

	private final AttendeeMapper attendeesMapper;
	private final TagMapper tagMapper;

	@Override
	public Set<Long> getTagIdsByAttendeesId(Long attendeesId) {
		// 1.透過attendeesId 找到attendees 與 tag 的關聯
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.eq(AttendeeTag::getAttendeesId, attendeesId);
		List<AttendeeTag> attendeesTagList = baseMapper.selectList(currentQueryWrapper);

		// 2.透過stream流抽取tagId, 變成Set集合
		return attendeesTagList.stream().map(attendeesTag -> attendeesTag.getTagId()).collect(Collectors.toSet());

	}

	@Override
	public List<AttendeeTag> getAttendeesTagByAttendeesId(Long attendeesId) {
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.eq(AttendeeTag::getAttendeesId, attendeesId);
		List<AttendeeTag> attendeesTagList = baseMapper.selectList(currentQueryWrapper);

		return attendeesTagList;
	}

	@Override
	public List<Tag> getTagsByAttendeesId(Long attendeesId) {
		// 1.查找關聯,提取tagIds
		List<AttendeeTag> attendeesTags = this.getAttendeesTagByAttendeesId(attendeesId);
		Set<Long> tagIds = attendeesTags.stream().map(AttendeeTag::getTagId).collect(Collectors.toSet());

		// 2.沒有返回空陣列
		if (tagIds.isEmpty()) {
			Collections.emptyList();
		}

		// 3.查找持有的tag
		LambdaQueryWrapper<Tag> tagyWrapper = new LambdaQueryWrapper<>();
		tagyWrapper.in(Tag::getTagId, tagIds);
		return tagMapper.selectList(tagyWrapper);

	}

	@Override
	public Map<Long, List<Long>> getAttendeesTagMapByAttendeesIds(Collection<Long> attendeesIds) {
		// 先獲取所有關聯關係
		List<AttendeeTag> tagList = this.getAttendeesTagByAttendeesIds(attendeesIds);
		// 設立結果集用來儲存
		Map<Long, List<Long>> result = new HashMap<>();

		// 將所有關係進行遍歷
		for (AttendeeTag at : tagList) {
			// 1. 分組：attendeesId → List<tagId>
			/**
			 * 
			 * 如果 result 中已經存在 at.getAttendeesId() 這個鍵：
			 * 
			 * 直接返回與該鍵關聯的現有 List<Long> (不會創建新的 ArrayList)
			 * Lambda 表達式 k -> new ArrayList<>() 不會被執行
			 * 
			 * 
			 * 如果 result 中不存在這個鍵：
			 * 
			 * 執行 Lambda 表達式創建新的 ArrayList<>()
			 * 將這個新列表與鍵 at.getAttendeesId() 關聯並存入 result
			 * 返回這個新列表
			 * 
			 * 
			 * 無論是哪種情況，computeIfAbsent 都會返回一個與該鍵關聯的 List<Long>，然後調用 .add(at.getTagId())
			 * 將標籤ID添加到這個列表中。
			 * 
			 * computeIfAbsent 和後續的 .add() 操作實際上是兩個分開的步驟
			 * 
			 */
			result.computeIfAbsent(at.getAttendeesId(), k -> new ArrayList<>()).add(at.getTagId());
		}
		return result;
	}

	@Override
	public Map<Long, List<Tag>> getTagMapByAttendeesId(Collection<Attendee> attendeesList) {
		
		// 如果列表為空,則返回空Map
		if(attendeesList.isEmpty()) {
			return Collections.emptyMap();
		}
		
		// 1.將attendeesList提取attendeesId ，獲取所有關聯
		Set<Long> attnedeesIdSet = attendeesList.stream().map(Attendee::getAttendeesId).collect(Collectors.toSet());
		List<AttendeeTag> attendeesTags = this.getAttendeesTagByAttendeesIds(attnedeesIdSet);

		// 2. 按 attendeesId 分組，收集 tagId
		Map<Long, List<Long>> attendeesIdToTagIds = attendeesTags.stream()
				.collect(Collectors.groupingBy(AttendeeTag::getAttendeesId,
						Collectors.mapping(AttendeeTag::getTagId, Collectors.toList())));

		// 3. 收集所有 tagId，獲取map中所有value,兩層List(Collection<List<Long>>)要拆開
		Set<Long> allTagIds = attendeesIdToTagIds.values().stream().flatMap(List::stream).collect(Collectors.toSet());

		// 4. 批量查詢所有 Tag，並組成映射關係tagId:Tag
		Map<Long, Tag> tagMap = tagMapper.selectBatchIds(allTagIds)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Tag::getTagId, Function.identity()));

		// 5. 構建最終結果：attendeesId -> List<Tag>
		Map<Long, List<Tag>> result = new HashMap<>();

		attendeesIdToTagIds.forEach((attendeesId, tagIds) -> {
			List<Tag> tags = tagIds.stream().map(tagMap::get).filter(Objects::nonNull).collect(Collectors.toList());
			result.put(attendeesId, tags);
		});

		return result;

	}

	@Override
	public List<AttendeeTag> getAttendeesTagByTagId(Long tagId) {
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.eq(AttendeeTag::getTagId, tagId);
		List<AttendeeTag> attendeesTagList = baseMapper.selectList(currentQueryWrapper);

		return attendeesTagList;
	}

	@Override
	public List<AttendeeTag> getAttendeesTagByAttendeesIds(Collection<Long> attendeesIds) {
		if (attendeesIds.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.in(AttendeeTag::getAttendeesId, attendeesIds);
		List<AttendeeTag> attendeesTagList = baseMapper.selectList(currentQueryWrapper);

		return attendeesTagList;
	}

	@Override
	public List<AttendeeTag> getAttendeesTagByTagIds(Collection<Long> tagIds) {
		LambdaQueryWrapper<AttendeeTag> attendeesTagWrapper = new LambdaQueryWrapper<>();
		attendeesTagWrapper.in(AttendeeTag::getTagId, tagIds);
		List<AttendeeTag> attendeesTagList = baseMapper.selectList(attendeesTagWrapper);

		return attendeesTagList;
	}

	@Override
	public void addAttendeesTag(AttendeeTag attendeesTag) {
		baseMapper.insert(attendeesTag);

	}

	@Override
	public void addAttendeesTag(Long attendeesId, Long tagId) {
		AttendeeTag attendeesTag = new AttendeeTag();
		attendeesTag.setAttendeesId(attendeesId);
		attendeesTag.setTagId(tagId);
		baseMapper.insert(attendeesTag);
	}

	@Override
	public void addTagsToAttendees(Long attendeesId, Collection<Long> tagsToAdd) {
		// 1.建立多個新連結
		List<AttendeeTag> newAttendeesTags = tagsToAdd.stream().map(tagId -> {
			AttendeeTag attendeesTag = new AttendeeTag();
			attendeesTag.setTagId(tagId);
			attendeesTag.setAttendeesId(attendeesId);
			return attendeesTag;
		}).collect(Collectors.toList());

		// 2.批量新增
		this.saveBatch(newAttendeesTags);
	}

	@Override
	public void addAttendeesToTag(Long tagId, Collection<Long> attendeesToAdd) {
		// 1.建立多個新連結
		List<AttendeeTag> newAttendeesTags = attendeesToAdd.stream().map(attendeesId -> {
			AttendeeTag attendeesTag = new AttendeeTag();
			attendeesTag.setTagId(tagId);
			attendeesTag.setAttendeesId(attendeesId);
			return attendeesTag;
		}).collect(Collectors.toList());

		// 2.批量新增
		this.saveBatch(newAttendeesTags);
	}

	@Override
	public void removeTagsFromAttendee(Long attendeesId, Collection<Long> tagsToRemove) {
		LambdaQueryWrapper<AttendeeTag> deleteAttendeesTagWrapper = new LambdaQueryWrapper<>();
		deleteAttendeesTagWrapper.eq(AttendeeTag::getAttendeesId, attendeesId)
				.in(AttendeeTag::getTagId, tagsToRemove);
		baseMapper.delete(deleteAttendeesTagWrapper);

	}

	@Override
	public void removeAttendeesFromTag(Long tagId, Set<Long> attendeessToRemove) {
		LambdaQueryWrapper<AttendeeTag> deleteAttendeesTagWrapper = new LambdaQueryWrapper<>();
		deleteAttendeesTagWrapper.eq(AttendeeTag::getTagId, tagId)
				.in(AttendeeTag::getAttendeesId, attendeessToRemove);
		baseMapper.delete(deleteAttendeesTagWrapper);

	}

}
