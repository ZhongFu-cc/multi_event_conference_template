package tw.com.conference.strategy.tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.TagTypeEnum;
import tw.com.conference.pojo.entity.AttendeeTag;
import tw.com.conference.service.AttendeeTagService;

@Component
@RequiredArgsConstructor
public class AttendeeTagStrategy implements TagStrategy {

	private final AttendeeTagService attendeeTagService;

	@Override
	public String supportType() {
		return TagTypeEnum.ATTENDEE.getType();
	}

	@Override
	public long countHoldersByTagId(Long tagId) {
		return attendeeTagService.lambdaQuery().eq(AttendeeTag::getTagId, tagId).count();
	}

	@Override
	public long countHoldersByTagIds(Collection<Long> tagIds) {
		// 拿到關聯
		List<AttendeeTag> list = attendeeTagService.lambdaQuery().in(AttendeeTag::getTagId, tagIds).list();
		// 收集唯一的 attendeeId
		Set<Long> uniqueAttendees = list.stream().map(AttendeeTag::getAttendeeId).collect(Collectors.toSet());

		return uniqueAttendees.size();
	}

	@Override
	public List<Long> getAssociatedIdsByTagId(Long tagId) {
		// 1. 查詢當前 tag 的所有關聯 attendeeTag
		List<AttendeeTag> attendeeTagList = attendeeTagService.getAttendeeTagByTagId(tagId);
		// 2. stream取出 attendeeIdList
		return attendeeTagList.stream().map(AttendeeTag::getAttendeeId).toList();
	}

	@Transactional
	@Override
	public void assignEntitiesToTag(List<Long> entityIdList, Long tagId) {
		// 1. 查詢當前 tag 的所有關聯 attendee
		List<AttendeeTag> currentAttendeeTags = attendeeTagService.getAttendeeTagByTagId(tagId);

		// 2. 提取當前關聯的 attendeeId Set
		Set<Long> currentAttendeeIdSet = currentAttendeeTags.stream()
				.map(AttendeeTag::getAttendeeId)
				.collect(Collectors.toSet());

		// 3. 獲取目標的 attendeeId 的Set集合
		Set<Long> targetAttendeeIdSet = new HashSet<>(entityIdList);

		// 4. 計算差集：當前有但目標沒有 → 需刪除
		Set<Long> attendeesToRemove = Sets.difference(currentAttendeeIdSet, targetAttendeeIdSet);

		// 5. 計算差集：目標有但當前沒有 → 需新增
		Set<Long> attendeesToAdd = Sets.difference(targetAttendeeIdSet, currentAttendeeIdSet);

		// 6. 執行刪除操作，如果 需刪除集合 中不為空，則開始刪除
		if (!attendeesToRemove.isEmpty()) {
			attendeeTagService.removeAttendeesFromTag(tagId, attendeesToRemove);
		}

		// 7. 執行新增操作，如果 需新增集合 中不為空，則開始新增
		if (!attendeesToAdd.isEmpty()) {
			attendeeTagService.addAttendeesToTag(tagId, attendeesToAdd);
		}

	}

}
