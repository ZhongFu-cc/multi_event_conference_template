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

	private final AttendeeMapper attendeeMapper;
	private final TagMapper tagMapper;

	@Override
	public Set<Long> getTagIdsByAttendeeId(Long attendeeId) {
		// 1.透過attendeeId 找到attendee 與 tag 的關聯
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.eq(AttendeeTag::getAttendeeId, attendeeId);
		List<AttendeeTag> attendeeTagList = baseMapper.selectList(currentQueryWrapper);

		// 2.透過stream流抽取tagId, 變成Set集合
		return attendeeTagList.stream().map(attendeeTag -> attendeeTag.getTagId()).collect(Collectors.toSet());

	}

	@Override
	public List<AttendeeTag> getAttendeeTagsByAttendeeId(Long attendeeId) {
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.eq(AttendeeTag::getAttendeeId, attendeeId);
		List<AttendeeTag> attendeeTagList = baseMapper.selectList(currentQueryWrapper);

		return attendeeTagList;
	}

	@Override
	public List<Tag> getTagsByAttendeeId(Long attendeeId) {
		// 1.查找關聯,提取tagIds
		List<AttendeeTag> attendeeTags = this.getAttendeeTagsByAttendeeId(attendeeId);
		Set<Long> tagIds = attendeeTags.stream().map(AttendeeTag::getTagId).collect(Collectors.toSet());

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
	public Map<Long, List<Long>> getAttendeeTagMapByAttendeeIds(Collection<Long> attendeeIds) {
		// 先獲取所有關聯關係
		List<AttendeeTag> tagList = this.getAttendeeTagsByAttendeeIds(attendeeIds);
		// 設立結果集用來儲存
		Map<Long, List<Long>> result = new HashMap<>();

		// 將所有關係進行遍歷
		for (AttendeeTag at : tagList) {
			// 1. 分組：attendeeId → List<tagId>
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
			result.computeIfAbsent(at.getAttendeeId(), k -> new ArrayList<>()).add(at.getTagId());
		}
		return result;
	}

	@Override
	public Map<Long, List<Tag>> getTagMapByAttendeeId(Collection<Attendee> attendeeList) {
		
		// 如果列表為空,則返回空Map
		if(attendeeList.isEmpty()) {
			return Collections.emptyMap();
		}
		
		// 1.將attendeeList提取attendeeId ，獲取所有關聯
		Set<Long> attnedeesIdSet = attendeeList.stream().map(Attendee::getAttendeeId).collect(Collectors.toSet());
		List<AttendeeTag> attendeeTags = this.getAttendeeTagsByAttendeeIds(attnedeesIdSet);

		// 2. 按 attendeeId 分組，收集 tagId
		Map<Long, List<Long>> attendeeIdToTagIds = attendeeTags.stream()
				.collect(Collectors.groupingBy(AttendeeTag::getAttendeeId,
						Collectors.mapping(AttendeeTag::getTagId, Collectors.toList())));

		// 3. 收集所有 tagId，獲取map中所有value,兩層List(Collection<List<Long>>)要拆開
		Set<Long> allTagIds = attendeeIdToTagIds.values().stream().flatMap(List::stream).collect(Collectors.toSet());

		// 4. 批量查詢所有 Tag，並組成映射關係tagId:Tag
		Map<Long, Tag> tagMap = tagMapper.selectBatchIds(allTagIds)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Tag::getTagId, Function.identity()));

		// 5. 構建最終結果：attendeeId -> List<Tag>
		Map<Long, List<Tag>> result = new HashMap<>();

		attendeeIdToTagIds.forEach((attendeeId, tagIds) -> {
			List<Tag> tags = tagIds.stream().map(tagMap::get).filter(Objects::nonNull).collect(Collectors.toList());
			result.put(attendeeId, tags);
		});

		return result;

	}

	@Override
	public List<AttendeeTag> getAttendeeTagByTagId(Long tagId) {
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.eq(AttendeeTag::getTagId, tagId);
		List<AttendeeTag> attendeeTagList = baseMapper.selectList(currentQueryWrapper);

		return attendeeTagList;
	}

	@Override
	public List<AttendeeTag> getAttendeeTagsByAttendeeIds(Collection<Long> attendeeIds) {
		if (attendeeIds.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<AttendeeTag> currentQueryWrapper = new LambdaQueryWrapper<>();
		currentQueryWrapper.in(AttendeeTag::getAttendeeId, attendeeIds);
		List<AttendeeTag> attendeeTagList = baseMapper.selectList(currentQueryWrapper);

		return attendeeTagList;
	}

	@Override
	public List<AttendeeTag> getAttendeeTagsByTagIds(Collection<Long> tagIds) {
		LambdaQueryWrapper<AttendeeTag> attendeeTagWrapper = new LambdaQueryWrapper<>();
		attendeeTagWrapper.in(AttendeeTag::getTagId, tagIds);
		List<AttendeeTag> attendeeTagList = baseMapper.selectList(attendeeTagWrapper);

		return attendeeTagList;
	}

	@Override
	public void addAttendeeTag(AttendeeTag attendeeTag) {
		baseMapper.insert(attendeeTag);

	}

	@Override
	public void addAttendeeTag(Long attendeeId, Long tagId) {
		AttendeeTag attendeeTag = new AttendeeTag();
		attendeeTag.setAttendeeId(attendeeId);
		attendeeTag.setTagId(tagId);
		baseMapper.insert(attendeeTag);
	}

	@Override
	public void addTagsToAttendee(Long attendeeId, Collection<Long> tagsToAdd) {
		// 1.建立多個新連結
		List<AttendeeTag> newAttendeeTags = tagsToAdd.stream().map(tagId -> {
			AttendeeTag attendeeTag = new AttendeeTag();
			attendeeTag.setTagId(tagId);
			attendeeTag.setAttendeeId(attendeeId);
			return attendeeTag;
		}).collect(Collectors.toList());

		// 2.批量新增
		this.saveBatch(newAttendeeTags);
	}

	@Override
	public void addAttendeesToTag(Long tagId, Collection<Long> attendeesToAdd) {
		// 1.建立多個新連結
		List<AttendeeTag> newAttendeeTags = attendeesToAdd.stream().map(attendeeId -> {
			AttendeeTag attendeeTag = new AttendeeTag();
			attendeeTag.setTagId(tagId);
			attendeeTag.setAttendeeId(attendeeId);
			return attendeeTag;
		}).collect(Collectors.toList());

		// 2.批量新增
		this.saveBatch(newAttendeeTags);
	}

	@Override
	public void removeTagsFromAttendee(Long attendeeId, Collection<Long> tagsToRemove) {
		LambdaQueryWrapper<AttendeeTag> deleteAttendeesTagWrapper = new LambdaQueryWrapper<>();
		deleteAttendeesTagWrapper.eq(AttendeeTag::getAttendeeId, attendeeId)
				.in(AttendeeTag::getTagId, tagsToRemove);
		baseMapper.delete(deleteAttendeesTagWrapper);

	}

	@Override
	public void removeAttendeesFromTag(Long tagId, Set<Long> attendeesToRemove) {
		LambdaQueryWrapper<AttendeeTag> deleteAttendeesTagWrapper = new LambdaQueryWrapper<>();
		deleteAttendeesTagWrapper.eq(AttendeeTag::getTagId, tagId)
				.in(AttendeeTag::getAttendeeId, attendeesToRemove);
		baseMapper.delete(deleteAttendeesTagWrapper);

	}

}
