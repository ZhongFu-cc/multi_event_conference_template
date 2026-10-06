package tw.com.conference.manager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.RequiredArgsConstructor;
import tw.com.conference.helper.TagAssignmentHelper;
import tw.com.conference.pojo.DTO.AddMemberForAdminDTO;
import tw.com.conference.pojo.DTO.EmailBodyContent;
import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberDTO;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.service.AsyncService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.MemberTagService;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.service.NotificationService;
import tw.com.conference.service.TagService;

@Component
@RequiredArgsConstructor
public class MemberRegistrationManager {

	@Value("${project.name}")
	private String PROJECT_NAME;

	private final TagAssignmentHelper tagAssignmentHelper;
	private final MemberService memberService;
	private final MemberTagService memberTagService;
	private final MemberTypeService memberTypeService;
	private final TagService tagService;
	private final NotificationService notificationService;
	private final AsyncService asyncService;
	private final RegistrationEventManager registrationEventManager;

	/**
	 * 註冊功能,新增網站會員<br>
	 * 註冊本身不產生訂單，會員之後再自行選擇活動報名 (見 {@link RegistrationEventManager})
	 *
	 * @param addMemberDTO
	 * @return
	 */
	@Transactional
	public SaTokenInfo addMember(AddMemberDTO addMemberDTO) {

		// 1.新增會員
		Member member = memberService.addMember(addMemberDTO);

		// 拿到會員身分
		MemberType memberType = memberTypeService.get(addMemberDTO.getMemberTypeId());

		// 2.獲取當下Member群體的Index,進行會員標籤分組
		tagAssignmentHelper.assignTag(member.getMemberId(), memberService::getMemberGroupIndex,
				tagService::getOrCreateMemberGroupTag, memberTagService::addMemberTag);

		// 3.獲取當下Member Category群體的Index,進行會員身份標籤分組
		tagAssignmentHelper.assignMemberCategoryTag(member.getMemberId(), memberType,
				memberService::getMemberCategoryGroupIndex, tagService::getOrCreateMemberCategoryGroupTag,
				memberTagService::addMemberTag);

		// 4.創建註冊成功通知信件內容
		EmailBodyContent registrationSuccessContent = notificationService.generateRegistrationSuccessContent(member,
				memberType);

		// 5.異步寄送信件
		asyncService.sendCommonEmail(member.getEmail(), PROJECT_NAME + " Registration Successful",
				registrationSuccessContent.getHtmlContent(), registrationSuccessContent.getPlainTextContent());

		// 6.返回token , 讓用戶於註冊後登入
		return memberService.login(member);
	}

	/**
	 * 後台新增會員功能<br>
	 * 走與一般報名相同的流程，但訂單為「免費」且直接付款完成，並直接成為與會者
	 *
	 * @param addMemberForAdminDTO
	 */
	@Transactional
	public void addMemberForAdmin(AddMemberForAdminDTO addMemberForAdminDTO) {

		// 1.判斷Email是否被註冊，如果沒有新增會員
		Member member = memberService.addMemberForAdmin(addMemberForAdminDTO);

		// 拿到會員身分
		MemberType memberType = memberTypeService.get(addMemberForAdminDTO.getMemberTypeId());

		// 2.獲取當下Member群體的Index,進行會員標籤分組
		tagAssignmentHelper.assignTag(member.getMemberId(), memberService::getMemberGroupIndex,
				tagService::getOrCreateMemberGroupTag, memberTagService::addMemberTag);

		// 3.獲取當下Member Category群體的Index,進行會員身份標籤分組
		tagAssignmentHelper.assignMemberCategoryTag(member.getMemberId(), memberType,
				memberService::getMemberCategoryGroupIndex, tagService::getOrCreateMemberCategoryGroupTag,
				memberTagService::addMemberTag);

		// 4.免費報名管理員指定的活動 (產生 0 元已付款訂單、報名紀錄，並成為與會者)
		registrationEventManager.freeRegistration(member, addMemberForAdminDTO.getEventIds());

	}

}
