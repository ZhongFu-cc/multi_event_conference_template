package tw.com.conference.helper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.TagTypeEnum;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.AttendeeTagService;
import tw.com.conference.service.MemberTagService;
import tw.com.conference.service.TagService;

/**
 * 會員「付款完成 → 成為與會者」的共用流程<br>
 * 由 免費報名(後台新增/現場報到)、綠界回調、後台人工審核 共同呼叫<br>
 * 只要任一活動付款完成，就具備與會者身分，後續報到/分組再透過 attendee_event 區分場次
 */
@Component
@RequiredArgsConstructor
public class AttendeeAdmissionHelper {

	private static final String UNPAID_TAG_PATTERN = "註冊費未付款";

	private final TagAssignmentHelper tagAssignmentHelper;
	private final AttendeeService attendeeService;
	private final AttendeeEventService attendeeEventService;
	private final AttendeeTagService attendeeTagService;
	private final MemberTagService memberTagService;
	private final TagService tagService;

	/**
	 * 會員付清任一活動後的後續處理：<br>
	 * 1. 移除「註冊費未付款」標籤<br>
	 * 2. 若尚未是與會者，新增進與會者名單並分配標籤<br>
	 * 3. 回填該會員所有報名紀錄的 attendee_id
	 *
	 * @param member 會員
	 * @return 與會者 (新建或既有)
	 */
	public Attendee admitAfterPayment(Member member) {

		// 1.移除會員 註冊費未付款 Tag
		tagAssignmentHelper.removeGroupTagsByPattern(member.getMemberId(), TagTypeEnum.MEMBER.getType(),
				UNPAID_TAG_PATTERN, tagService::getTagIdsByTypeAndNamePattern, memberTagService::removeTagsFromMember);

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
