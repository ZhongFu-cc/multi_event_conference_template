package tw.com.conference.strategy.mail;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.AttendeeConvert;
import tw.com.conference.exception.EmailException;
import tw.com.conference.pojo.DTO.SendEmailDTO;
import tw.com.conference.pojo.VO.AttendeeVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeTag;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.service.AsyncService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.AttendeeTagService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.ScheduleEmailTaskService;

@Component
@RequiredArgsConstructor
public class AttendeeMailStrategy implements MailStrategy {

	@Value("${project.domain}")
	private String PROJECT_DOMAIN;
	
	@Qualifier("businessRedissonClient")
	private final RedissonClient redissonClient;

	private static final String DAILY_EMAIL_QUOTA_KEY = "email:dailyQuota";
	private final AttendeeConvert attendeeConvert;
	private final AttendeeService attendeeService;
	private final AttendeeTagService attendeeTagService;
	private final AsyncService asyncService;
	private final ScheduleEmailTaskService scheduleEmailTaskService;
	private final MemberService memberService;

	@Override
	public void batchSendEmail(List<Long> tagIdList, SendEmailDTO sendEmailDTO) {
		//從Redis中查看本日信件餘額
		RAtomicLong quota = redissonClient.getAtomicLong(DAILY_EMAIL_QUOTA_KEY);
		long currentQuota = quota.get();

		// 如果信件額度 小於等於 0，直接返回錯誤不要寄信
		if (currentQuota <= 0) {
			throw new EmailException("今日寄信配額已用完");
		}

		// 獲取本日預計要寄出的信件量, 為了保證排程任務順利被寄出
		int pendingExpectedEmailVolumeByToday = scheduleEmailTaskService.getPendingExpectedEmailVolumeByToday();

		//初始化 attendeeIdSet ，用於去重attendeeId
		Set<Long> attendeeIdSet = new HashSet<>();

		// 先判斷tagIdList是否為空數組 或者 null ，如果true 則是要寄給所有會員
		Boolean hasNoTag = tagIdList == null || tagIdList.isEmpty();

		//初始化要寄信的會員人數
		Long attendeeCount = 0L;

		if (hasNoTag) {
			attendeeCount = attendeeService.lambdaQuery().count();
		} else {

			// 拿到與會者ID列表
			attendeeIdSet = this.getAttendeeIdSet(tagIdList);

			if (attendeeIdSet.isEmpty()) {
				throw new EmailException("沒有符合資格的與會者");
			}

			// 如果attendeeIdSet 至少有一個，則開始搜尋Attendee
			attendeeCount = attendeeService.lambdaQuery().in(Attendee::getAttendeeId, attendeeIdSet).count();

		}

		//這邊都先排除沒信件額度，和沒有收信者的情況
		if (attendeeCount <= 0) {
			throw new EmailException("沒有符合資格的與會者");
		} else if (currentQuota - pendingExpectedEmailVolumeByToday < attendeeCount) {
			throw new EmailException("本日寄信額度無法寄送 " + attendeeCount + " 封信");
		}

		// 查收信者名單 + member
		List<AttendeeVO> attendeeVOList = buildAttendeeVOList(hasNoTag ? null : attendeeIdSet);

		//前面已排除null 和 0 的狀況，開 異步線程 直接開始遍歷寄信
		asyncService.batchSendEmail(attendeeVOList, sendEmailDTO, a -> a.getMember().getEmail(),
				this::replaceAttendeesMergeTag);

		// 額度直接扣除 查詢到的會員數量
		// 避免多用戶操作時，明明已經達到寄信額度，但異步線程仍未扣除完成
		quota.addAndGet(-attendeeCount);
	}

	@Override
	public void scheduleEmail(List<Long> tagIdList, SendEmailDTO sendEmailDTO) {
		// 1.拿到與會者ID 列表
		Set<Long> attendeeIdSet = this.getAttendeeIdSet(tagIdList);

		// 2.透過AttendeesID列表拿到 vo列表
		List<AttendeeVO> attendeeVOList = this.buildAttendeeVOList(attendeeIdSet);

		// 3.放入排程任務
		scheduleEmailTaskService.processScheduleEmailTask(sendEmailDTO, attendeeVOList, "attendee",
				a -> a.getMember().getEmail(), this::replaceAttendeesMergeTag);

	}

	/**
	 * 根據 tagIdList 獲取與會者 ID 集合
	 *
	 * @param tagIdList 標籤 ID 列表
	 * @return 與會者 ID 集合，若無標籤或無符合者，則返回空集合
	 */
	private Set<Long> getAttendeeIdSet(List<Long> tagIdList) {
		// 1.若 tagIdList 為空，則表示所有與會者，直接返回 null
		if (tagIdList == null || tagIdList.isEmpty()) {
			return null;
		}

		// 2.透過 tag 找到符合的 attendee 關聯
		List<AttendeeTag> attendeeTagList = attendeeTagService.getAttendeeTagsByTagIds(tagIdList);

		// 3.從關聯中取出 attendeeId，並使用 Set 去重
		return attendeeTagList.stream().map(AttendeeTag::getAttendeeId).collect(Collectors.toSet());
	}

	/**
	 * 返回 與會者的VO 對象
	 * 
	 * @param attendeeIdSet 與會者的ID集合
	 * @return
	 */
	private List<AttendeeVO> buildAttendeeVOList(Set<Long> attendeeIdSet) {
		List<Attendee> attendeeList;
		if (attendeeIdSet == null || attendeeIdSet.isEmpty()) {
			attendeeList = attendeeService.lambdaQuery().list();
		} else {
			attendeeList = attendeeService.lambdaQuery().in(Attendee::getAttendeeId, attendeeIdSet).list();
		}

		Map<Long, Member> memberMap = memberService.getMemberMapByAttendeeList(attendeeList);

		// 組裝 VO
		return attendeeList.stream().map(attendee -> {
			AttendeeVO vo = attendeeConvert.entityToVO(attendee);
			vo.setMember(memberMap.get(attendee.getMemberId()));
			return vo;
		}).collect(Collectors.toList());
	}

	private String replaceAttendeesMergeTag(String content, AttendeeVO attendeeVO) {

		String qrCodeUrl = String.format(PROJECT_DOMAIN + "/prod-api/attendee/qrcode?attendeeId=%s",
				attendeeVO.getAttendeeId());

		String newContent = content.replace("{{QRcode}}", "<img src=\"" + qrCodeUrl + "\" alt=\"QR Code\" />")
				.replace("{{name}}", Strings.nullToEmpty(attendeeVO.getMember().getChineseName()));

		return newContent;

	}

}
