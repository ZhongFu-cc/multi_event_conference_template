package tw.com.conference.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.MemberConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.pojo.VO.MemberTagVO;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.Tag;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.MemberTagService;
import tw.com.conference.service.TagService;

@Component
@RequiredArgsConstructor
public class MemberTagManager {

	private final MemberConvert memberConvert;
	private final MemberService memberService;
	private final MemberTagService memberTagService;
	private final TagService tagService;
	private final AttendeeEventService attendeeEventService;

	/**
	 * 根據 memberId, 獲取MemberTagVO 對象
	 *
	 * @param memberId
	 * @return
	 */
	public MemberTagVO getMemberTagVOByMember(Long memberId) {

		// 1.獲取基本的 memberTagVO 對象
		MemberTagVO memberTagVO = memberService.getMemberTagVOByMember(memberId);

		// 2.獲取已報名活動及各自繳費狀態，放入VO中
		memberTagVO.setEventStatusList(attendeeEventService.findEventStatusByMemberId(memberId));

		// 3.查詢該member所有關聯的tagId Set
		Set<Long> tagIdSet = memberTagService.getTagIdsByMemberId(memberId);

		// 4.如果沒有任何關聯,就可以直接返回了
		if (tagIdSet.isEmpty()) {
			return memberTagVO;
		}

		// 5.去Tag表中查詢實際的Tag資料，並轉換成Set集合
		List<Tag> tagList = tagService.getTagListByIds(tagIdSet);

		// 6.最後填入memberTagVO對象並返回
		memberTagVO.setTagList(tagList);

		return memberTagVO;
	};

	/**
	 * 根據搜尋條件 獲取會員資料及持有的tag集合(分頁)
	 *
	 * @param page
	 * @param queryText
	 * @param eventId   選填; 有帶時只列出報名此活動的會員，並填入該活動的繳費狀態
	 * @param isPaid    選填; 需搭配 eventId, null 為不限
	 * @return
	 */
	public IPage<MemberTagVO> getMemberTagVOByQuery(Page<Member> page, String queryText, Long eventId,
			CommonStatusEnum isPaid) {

		// 初始化返回對象
		IPage<MemberTagVO> voPage = new Page<>(page.getCurrent(), page.getSize());

		// 初始化,符合活動 + 繳費狀態 條件的memberIds
		List<Long> memberIdsByEvent = Collections.emptyList();

		// 1.有指定活動時，先抽出報名此活動且符合繳費狀態的會員
		if (eventId != null) {
			memberIdsByEvent = attendeeEventService.findMemberIdsByEventAndPaid(eventId, isPaid);
			// 1-1 沒有任何符合的會員,直接返回空VO
			if (memberIdsByEvent.isEmpty()) {
				return voPage;
			}
		}

		// 2.放入條件,找到符合的Member 分頁對象，如果沒有會員成返回空對象
		IPage<Member> memberPage = memberService.getMemberPageByQuery(page, queryText, memberIdsByEvent);
		if (memberPage.getRecords().isEmpty()) {
			return voPage;
		}

		// 3.獲取 memberTagMap
		Map<Long, List<Tag>> groupTagsByMemberId = memberTagService.groupTagsByMemberId(memberPage.getRecords());

		// 4.有指定活動時，獲取這頁會員對該活動的繳費狀態
		Map<Long, CommonStatusEnum> paidMapByMemberId = Collections.emptyMap();
		if (eventId != null) {
			Set<Long> pageMemberIds = memberPage.getRecords().stream().map(Member::getMemberId)
					.collect(Collectors.toSet());
			paidMapByMemberId = attendeeEventService.getPaidMapByEventAndMemberIds(eventId, pageMemberIds);
		}
		final Map<Long, CommonStatusEnum> finalPaidMap = paidMapByMemberId;

		// 5.遍歷memberPage時組裝
		List<MemberTagVO> memberTagVOList = memberPage.getRecords().stream().map(member -> {

			// 5-1 member轉換成vo對象
			MemberTagVO vo = memberConvert.entityToMemberTagVO(member);

			// 5-2 有指定活動時，填入該活動的繳費狀態 (沿用訂單狀態的標籤)
			if (eventId != null) {
				boolean paid = CommonStatusEnum.YES.equals(finalPaidMap.get(member.getMemberId()));
				vo.setStatus(paid ? OrderStatusEnum.PAYMENT_SUCCESS.getLabelZh() : OrderStatusEnum.UNPAID.getLabelZh());
			}

			// 5-3 查詢到並將tag放入
			List<Tag> tagList = groupTagsByMemberId.getOrDefault(member.getMemberId(), Collections.emptyList());
			vo.setTagList(tagList);

			return vo;
		}).collect(Collectors.toList());

		// 6.最後組裝分頁對象返回
		voPage = new Page<>(page.getCurrent(), page.getSize(), memberPage.getTotal());
		voPage.setRecords(memberTagVOList);

		return voPage;
	}

	/**
	 * 為用戶新增/更新/刪除 複數tag
	 *
	 * @param targetTagIdList
	 * @param memberId
	 */
	@Transactional
	public void assignTagToMember(List<Long> targetTagIdList, Long memberId) {

		// 1.拿到目標 TagIdSet
		Set<Long> targetTagIdSet = new HashSet<>(targetTagIdList);

		// 2.查詢該member所有關聯的tagId Set
		Set<Long> currentTagIdSet = memberTagService.getTagIdsByMemberId(memberId);

		// 3.拿到該移除的集合 和 該新增的集合
		Set<Long> tagsToRemove = Sets.difference(currentTagIdSet, targetTagIdSet);
		Set<Long> tagsToAdd = Sets.difference(targetTagIdSet, currentTagIdSet);

		// 4. 執行刪除操作，如果 需刪除集合 中不為空，則開始刪除
		if (!tagsToRemove.isEmpty()) {
			memberTagService.removeTagsFromMember(memberId, tagsToRemove);
		}

		// 5.執行新增操作
		if (!tagsToAdd.isEmpty()) {
			memberTagService.addTagsToMember(memberId, tagsToAdd);
		}

	}

}
