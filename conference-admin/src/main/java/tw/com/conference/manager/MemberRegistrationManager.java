package tw.com.conference.manager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.RequiredArgsConstructor;
import tw.com.conference.config.RegistrationFeeConfig;
import tw.com.conference.context.ProjectModeContext;
import tw.com.conference.enums.MemberCategoryEnum;
import tw.com.conference.helper.MessageHelper;
import tw.com.conference.helper.TagAssignmentHelper;
import tw.com.conference.pojo.DTO.AddMemberForAdminDTO;
import tw.com.conference.pojo.DTO.EmailBodyContent;
import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberDTO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.service.AsyncService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.AttendeeTagService;
import tw.com.conference.service.InvitedSpeakerService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.MemberTagService;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.service.NotificationService;
import tw.com.conference.service.OrdersService;
import tw.com.conference.service.SettingService;
import tw.com.conference.service.TagService;

@Component
@RequiredArgsConstructor
public class MemberRegistrationManager {

	@Value("${project.name}")
	private String PROJECT_NAME;

	@Value("${project.banner-url}")
	private String BANNER_PHOTO_URL;

	// 團體折扣 , 從application.yml 進行修改 
	@Value("${project.group-discount}")
	private Double GROUP_DISCOUNT;

	private final RegistrationFeeConfig registrationFeeConfig;

	private final ProjectModeContext projectModeContext;

	private final MessageHelper messageHelper;
	private final TagAssignmentHelper tagAssignmentHelper;
	private final MemberService memberService;
	private final MemberTagService memberTagService;
	private final MemberTypeService memberTypeService;
	private final OrdersService ordersService;
	private final AttendeeService attendeesService;
	private final TagService tagService;

	private final AttendeeTagService attendeesTagService;
	private final NotificationService notificationService;
	private final AsyncService asyncService;

	/**
	 * 註冊功能,新增會員,產生「付費」訂單
	 * 
	 * @param addMemberDTO
	 * @return
	 */
	@Transactional
	public SaTokenInfo addMember(AddMemberDTO addMemberDTO) {

		// 未來應該不限制其註冊網站會員，而是在Event過了報名時間時關閉

		// 2.新增會員
		Member member = memberService.addMember(addMemberDTO);

		// 拿到會員身分
		MemberType memberType = memberTypeService.get(addMemberDTO.getMemberTypeId());

		// 3.以當前模式策略,執行註冊流程 (計算金額=>產生訂單=>產生通知信並寄出)
		// projectModeContext.getStrategy().handleRegistration(member);

		// 4.獲取當下Member群體的Index,進行會員標籤分組
		tagAssignmentHelper.assignTag(member.getMemberId(), memberService::getMemberGroupIndex,
				tagService::getOrCreateMemberGroupTag, memberTagService::addMemberTag);

		// 5.獲取當下Member Category群體的Index,進行會員身份標籤分組
		tagAssignmentHelper.assignMemberCategoryTag(member.getMemberId(), memberType,
				memberService::getMemberCategoryGroupIndex, tagService::getOrCreateMemberCategoryGroupTag,
				memberTagService::addMemberTag);
		
		// 6.創建註冊成功通知信件內容
		EmailBodyContent registrationSuccessContent = notificationService.generateRegistrationSuccessContent(member,
				memberType);

		// 7.異步寄送信件
		asyncService.sendCommonEmail(member.getEmail(), PROJECT_NAME + " Registration Successful",
				registrationSuccessContent.getHtmlContent(), registrationSuccessContent.getPlainTextContent());

		// 6.返回token , 讓用戶於註冊後登入
		return memberService.login(member);
	}

	/**
	 * 後台新增會員功能,產生「免費」訂單
	 * 
	 * @param addMemberForAdminDTO
	 */
	@Transactional
	public void addMemberForAdmin(AddMemberForAdminDTO addMemberForAdminDTO) {

		// 1.判斷Email是否被註冊，如果沒有新增會員
		Member member = memberService.addMemberForAdmin(addMemberForAdminDTO);

		// 拿到會員身分
		MemberType memberType = memberTypeService.get(addMemberForAdminDTO.getMemberTypeId());

		// 2.新增「免費」的訂單,並標註 「已付款」
		ordersService.createFreeRegistrationOrder(member);

		// 3.獲取當下Member群體的Index,進行會員標籤分組
		tagAssignmentHelper.assignTag(member.getMemberId(), memberService::getMemberGroupIndex,
				tagService::getOrCreateMemberGroupTag, memberTagService::addMemberTag);

		// 4.獲取當下Member Category群體的Index,進行會員身份標籤分組
		tagAssignmentHelper.assignMemberCategoryTag(member.getMemberId(), memberType,
				memberService::getMemberCategoryGroupIndex, tagService::getOrCreateMemberCategoryGroupTag,
				memberTagService::addMemberTag);

		// 5.由後台新增的Member , 自動付款完成，新增進與會者名單
		Attendee attendee = attendeesService.addAttendee(member);

		// 6.獲取當下與會者群體的Index,進行與會者標籤分組
		tagAssignmentHelper.assignTag(attendee.getAttendeeId(), attendeesService::getAttendeeGroupIndex,
				tagService::getOrCreateAttendeesGroupTag, attendeesTagService::addAttendeeTag);

		// 7.如果是講者身分,則新增到invited-speaker, 這個也再考慮, 可能違反SRP


	}

}
