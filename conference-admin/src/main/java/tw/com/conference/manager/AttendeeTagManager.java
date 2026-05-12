package tw.com.conference.manager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.AttendeeConvert;
import tw.com.conference.enums.CheckinActionTypeEnum;
import tw.com.conference.pojo.VO.AttendeeTagVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.CheckinRecord;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.Tag;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.AttendeeTagService;
import tw.com.conference.service.CheckinRecordService;
import tw.com.conference.service.MemberService;

@Component
@RequiredArgsConstructor
public class AttendeeTagManager {

	private final MemberService memberService;
	private final AttendeeService attendeeService;
	private final AttendeeTagService attendeeTagService;
	private final AttendeeConvert attendeeConvert;
	private final CheckinRecordService checkinRecordService;

	/**
	 * 根據 attendeeId ， 獲取與會者資訊 和 Tag標籤
	 * 
	 * @param attendeeId
	 * @return
	 */
	public AttendeeTagVO getAttendeesTagVO(Long attendeeId) {
		// 1.獲取attendee 資料並轉換成 attendeeTagVO
		Attendee attendee = attendeeService.getAttendee(attendeeId);
		AttendeeTagVO attendeeTagVO = attendeeConvert.entityToAttendeeTagVO(attendee);

		// 2.查詢attendee 的基本資料，並放入Member屬性
		Member member = memberService.getMember(attendee.getMemberId());
		attendeeTagVO.setMember(member);

		// 3.根據 attendeeId 找到與會者所有簽到/退紀錄，並放入CheckinRecord屬性
		List<CheckinRecord> checkinRecordList = checkinRecordService.getCheckinRecordByAttendeeId(attendeeId);
		attendeeTagVO.setCheckinRecordList(checkinRecordList);

		// 4.isCheckedIn屬性預設是false, 所以只要判斷最新的資料是不是已簽到,如果是再進行更改就好
		CheckinRecord latest = checkinRecordList.stream()
				// ID 為雪花算法，等於時間序
				.max(Comparator.comparing(CheckinRecord::getCheckinRecordId))
				.orElse(null);

		if (latest != null && CheckinActionTypeEnum.CHECKIN.getValue().equals(latest.getActionType())) {
			attendeeTagVO.setIsCheckedIn(true);
		}

		// 5.查找 與會者 的tags，放入VO
		List<Tag> tags = attendeeTagService.getTagsByAttendeeId(attendeeId);
		attendeeTagVO.setTagList(tags);

		return attendeeTagVO;
	}

	/**
	 * 組裝AttendeesTagVO對象
	 * 
	 * @param attendeePage
	 * @return
	 */
	private List<AttendeeTagVO> buildAttendeeTagVO(IPage<Attendee> attendeePage) {
		// 2.獲取 會員 映射對象
		Map<Long, Member> memberMap = memberService.getMemberMapByAttendeeList(attendeePage.getRecords());

		// 3.獲取 簽到記錄 映射對象
		Map<Long, List<CheckinRecord>> checkinMap = checkinRecordService
				.getCheckinMapByAttendeeList(attendeePage.getRecords());

		// 4.獲取 最後簽到紀錄 映射對象
		Map<Long, Boolean> checkinStatusMap = checkinRecordService.getCheckinStatusMap(checkinMap);

		// 5.獲取 標籤 映射對象
		Map<Long, List<Tag>> tagMapByAttendeeId = attendeeTagService.getTagMapByAttendeeId(attendeePage.getRecords());

		// 6.遍歷與會者分頁對象 並組裝VOPage
		List<AttendeeTagVO> attendeeTagVOList = attendeePage.getRecords().stream().map(attendee -> {
			AttendeeTagVO vo = attendeeConvert.entityToAttendeeTagVO(attendee);
			vo.setMember(memberMap.get(attendee.getMemberId()));
			vo.setCheckinRecordList(checkinMap.getOrDefault(attendee.getAttendeeId(), Collections.emptyList()));
			vo.setIsCheckedIn(checkinStatusMap.getOrDefault(attendee.getAttendeeId(), false));
			vo.setTagList(tagMapByAttendeeId.getOrDefault(attendee.getAttendeeId(), Collections.emptyList()));

			return vo;
		}).toList();

		// 7.返回voList
		return attendeeTagVOList;

	}

	/**
	 * 根據條件參數,獲取所有與會者資訊 和 Tag標籤(分頁)
	 * 
	 * @param pageInfo
	 * @param queryText
	 * @return
	 */
	public IPage<AttendeeTagVO> getAttendeesTagVOPageByQuery(Page<Attendee> pageInfo, String queryText) {

		// 初始化分頁對象
		IPage<AttendeeTagVO> voPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize());

		// 1.根據條件查詢符合的會員(與會者的資訊在會員表內)
		List<Member> memberList = memberService.getMembersByQuery(queryText);
		// 如果放上條件查無數據,直接返回
		if (memberList.isEmpty()) {
			return voPage;
		}

		// 2.獲取與會者分頁對象
		IPage<Attendee> attendeePage = attendeeService.getAttendeePageByMemberList(pageInfo, memberList);

		// 3.組裝AttendeesTagVOList
		List<AttendeeTagVO> attendeeTagVOList = this.buildAttendeeTagVO(attendeePage);

		// 4.回傳分頁物件
		voPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), attendeePage.getTotal());
		voPage.setRecords(attendeeTagVOList);
		return voPage;

	}

	/**
	 * 為與會者新增/更新/刪除 複數tag
	 * 
	 * @param targetTagIdList
	 * @param memberId
	 */
	public void assignTagToAttendees(List<Long> targetTagIdList, Long attendeeId) {

		// 1.拿到目標 TagIdSet
		Set<Long> targetTagIdSet = new HashSet<>(targetTagIdList);

		// 2.查詢該attendee所有關聯的tagId Set
		Set<Long> currentTagIdSet = attendeeTagService.getTagIdsByAttendeeId(attendeeId);

		// 3.拿到該移除的集合 和 該新增的集合
		Set<Long> tagsToRemove = Sets.difference(currentTagIdSet, targetTagIdSet);
		Set<Long> tagsToAdd = Sets.difference(targetTagIdSet, currentTagIdSet);

		// 4. 執行刪除操作，如果 需刪除集合 中不為空，則開始刪除
		if (!tagsToRemove.isEmpty()) {
			attendeeTagService.removeTagsFromAttendee(attendeeId, tagsToRemove);
		}

		// 5.執行新增操作
		if (!tagsToAdd.isEmpty()) {
			attendeeTagService.addTagsToAttendee(attendeeId, tagsToAdd);
		}

	}

}
