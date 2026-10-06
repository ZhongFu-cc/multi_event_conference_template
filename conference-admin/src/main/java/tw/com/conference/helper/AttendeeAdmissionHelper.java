package tw.com.conference.helper;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.TagTypeEnum;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.AttendeeTagService;
import tw.com.conference.service.MemberTagService;
import tw.com.conference.service.OrdersService;
import tw.com.conference.service.TagService;

/**
 * 會員 付款狀態 → 與會者資格 的共用流程<br>
 * 由 報名(前台/後台新增/現場報到)、綠界回調、後台人工審核 共同呼叫<br>
 * 只要任一活動付款完成，就具備與會者身分，後續報到/分組再透過 attendee_event 區分場次<br>
 * <br>
 * 「未付款」標籤的 新增({@link #markUnpaid}) 與 移除({@link #admitAfterPayment}) 一併放在這裡，
 * 確保兩邊共用同一組 tagType / 名稱前綴，不會各自寫死而不同步
 */
@Component
@RequiredArgsConstructor
public class AttendeeAdmissionHelper {

	private final TagAssignmentHelper tagAssignmentHelper;
	private final AttendeeService attendeeService;
	private final AttendeeEventService attendeeEventService;
	private final AttendeeTagService attendeeTagService;
	private final MemberTagService memberTagService;
	private final OrdersService ordersService;
	private final TagService tagService;

	/**
	 * 標記會員為「未付款」<br>
	 * 只要會員有任一張未繳費訂單，他就該有且只有一個 未付款-group-NN 標籤<br>
	 * 分組是為了寄催款信的單次寄送上限，以會員數分組
	 *
	 * @param memberId
	 */
	public void markUnpaid(Long memberId) {

		// 1.該會員已經有未付款標籤就不重複掛
		// (會員可能分多次報名產生多張未付訂單；且 group index 會隨未付款會員總數成長，
		// 不擋的話第二次報名可能掛上 -group-02，變成同時持有兩個未付款標籤)
		Set<Long> unpaidTagIds = tagAssignmentHelper.getGroupTagIdsByPrefix(TagTypeEnum.MEMBER.getType(),
				TagService.UNPAID_TAG_PATTERN, tagService::getTagIdsByTypeAndNamePattern);
		if (!unpaidTagIds.isEmpty()) {
			Set<Long> ownedTagIds = memberTagService.getTagIdsByMemberId(memberId);
			if (unpaidTagIds.stream().anyMatch(ownedTagIds::contains)) {
				return;
			}
		}

		// 2.取得當下未付款會員群體的 index，掛上對應的分組標籤
		tagAssignmentHelper.assignTag(memberId, ordersService::getUnpaidMemberGroupIndex,
				tagService::getOrCreateNotPaidGroupTag, memberTagService::addMemberTag);
	}

	/**
	 * 會員付清任一活動後的後續處理：<br>
	 * 1. 已無任何未付訂單時，移除「未付款」標籤<br>
	 * 2. 若尚未是與會者，新增進與會者名單並分配標籤<br>
	 * 3. 回填該會員所有報名紀錄的 attendee_id
	 *
	 * @param member 會員
	 * @return 與會者 (新建或既有)
	 */
	public Attendee admitAfterPayment(Member member) {

		// 1.只有在「已無任何未付訂單」時，才移除會員 未付款 Tag
		// 一個會員可能同時有多張訂單 (多場活動各一張)，付清其中一張時其他場次仍欠款，標籤要留著
		if (!ordersService.hasUnpaidOrders(member.getMemberId())) {
			tagAssignmentHelper.removeGroupTagsByPattern(member.getMemberId(), TagTypeEnum.MEMBER.getType(),
					TagService.UNPAID_TAG_PATTERN, tagService::getTagIdsByTypeAndNamePattern,
					memberTagService::removeTagsFromMember);
		}

		// 2.已經是與會者就不重複建立
		Attendee attendee = attendeeService.getAttendeeByMemberId(member.getMemberId());
		if (attendee == null) {
			// 2-1 新增進與會者名單
			attendee = attendeeService.addAttendee(member);

			// 2-2 獲取當下與會者群體的Index,進行與會者標籤分組
			tagAssignmentHelper.assignTag(attendee.getAttendeeId(), attendeeService::getAttendeeGroupIndex,
					tagService::getOrCreateAttendeesGroupTag, attendeeTagService::addAttendeeTag);
		}

		// 3.回填報名紀錄的 attendee_id，讓報到/分組可直接關聯
		attendeeEventService.linkAttendee(member.getMemberId(), attendee.getAttendeeId());

		return attendee;
	}
}
