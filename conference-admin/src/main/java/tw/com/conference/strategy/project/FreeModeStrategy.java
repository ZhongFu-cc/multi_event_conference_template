package tw.com.conference.strategy.project;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.pojo.DTO.EmailBodyContent;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.Tag;
import tw.com.conference.service.AsyncService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.AttendeeTagService;
import tw.com.conference.service.NotificationService;
import tw.com.conference.service.OrdersService;
import tw.com.conference.service.TagService;

@Component
@RequiredArgsConstructor
public class FreeModeStrategy implements ProjectModeStrategy {

	@Value("${project.name}")
	private String PROJECT_NAME;

	@Value("${project.banner-url}")
	private String BANNER_PHOTO_URL;

	@Value("${project.group-size}")
	private int GROUP_SIZE;

	private final OrdersService ordersService;
	private final AttendeeService attendeeService;
	private final TagService tagService;
	private final AttendeeTagService attendeeTagService;
	private final NotificationService notificationService;
	private final AsyncService asyncService;

	@Override
	public void handleRegistration(Member member) {

//		// 1.創建「免費」註冊費訂單，狀態為 「已付款」
//		ordersService.createFreeRegistrationOrder(member);
//
//		// 2.創建註冊成功通知信件內容
//		EmailBodyContent registrationSuccessContent = notificationService.generateRegistrationSuccessContent(member,
//				BANNER_PHOTO_URL);
//
//		// 3.異步寄送信件
//		asyncService.sendCommonEmail(member.getEmail(), PROJECT_NAME + " Registration Successful",
//				registrationSuccessContent.getHtmlContent(), registrationSuccessContent.getPlainTextContent());
//
//		// 4.新增進與會者名單
//		Attendee attendee = attendeeService.addAttendee(member);
//
//		// 5.獲取當下 Attendee 群體的Index,用於後續標籤分組
//		int attendeeGroupIndex = attendeeService.getAttendeeGroupIndex(GROUP_SIZE);
//
//		// 6.與會者標籤分組
//		// 拿到 Tag（不存在則新增Tag）
//		Tag attendeeGroupTag = tagService.getOrCreateAttendeesGroupTag(attendeeGroupIndex);
//		// 關聯 Attendee 與 Tag
//		attendeeTagService.addAttendeeTag(attendee.getAttendeeId(), attendeeGroupTag.getTagId());

	}

	@Override
	public void handleGroupRegistration(Member member, boolean isMaster, BigDecimal totalFee) {

//		// 1.創建 「免費」 團體註冊費訂單，狀態為 「已付款」
//		ordersService.createFreeGroupRegistrationPaidOrder(member);
//
//		// 2.產生系統團體報名通知信
//		EmailBodyContent groupRegistrationSuccessContent = notificationService
//				.generateGroupRegistrationSuccessContent(member, BANNER_PHOTO_URL);
//
//		// 3.寄信個別通知會員，團體報名成功
//		asyncService.sendCommonEmail(member.getEmail(), PROJECT_NAME + " GROUP Registration Successful",
//				groupRegistrationSuccessContent.getHtmlContent(),
//				groupRegistrationSuccessContent.getPlainTextContent());
//
//		// 4.新增進與會者名單
//		Attendee attendee = attendeeService.addAttendee(member);
//
//		// 5.獲取當下 Attendee 群體的Index,用於後續標籤分組
//		int attendeeGroupIndex = attendeeService.getAttendeeGroupIndex(GROUP_SIZE);
//
//		// 6.與會者標籤分組
//		// 拿到 Tag（不存在則新增Tag）
//		Tag attendeeGroupTag = tagService.getOrCreateAttendeesGroupTag(attendeeGroupIndex);
//		// 關聯 Attendee 與 Tag
//		attendeeTagService.addAttendeeTag(attendee.getAttendeeId(), attendeeGroupTag.getTagId());

	}

	@Override
	public void handlePaperSubmission(Long memberId) {
		// Free 模式,不用去攔截他投稿
		
		
	}

}
