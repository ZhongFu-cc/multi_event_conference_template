package tw.com.conference.service.impl;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.RegistrationGroupStatusEnum;
import tw.com.conference.exception.RegistrationGroupException;
import tw.com.conference.mapper.RegistrationGroupMapper;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.pojo.entity.RegistrationGroup;
import tw.com.conference.pojo.entity.RegistrationGroupMember;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.RegistrationGroupMemberService;
import tw.com.conference.service.RegistrationGroupService;

@Service
@RequiredArgsConstructor
public class RegistrationGroupServiceImpl extends ServiceImpl<RegistrationGroupMapper, RegistrationGroup>
		implements RegistrationGroupService {

	/** 邀請碼字元集; 排除 0/O、1/I/L 等易誤認字元 */
	private static final String INVITE_CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

	/** 邀請碼長度 */
	private static final int INVITE_CODE_LENGTH = 8;

	/** 產生邀請碼的最大重試次數 (碰撞極罕見，僅作為保險) */
	private static final int MAX_GENERATE_ATTEMPTS = 10;

	private static final SecureRandom RANDOM = new SecureRandom();

	private final RegistrationGroupMemberService registrationGroupMemberService;
	private final AttendeeEventService attendeeEventService;

	@Override
	public RegistrationGroup get(Long registrationGroupId) {
		return baseMapper.selectById(registrationGroupId);
	}

	@Override
	public RegistrationGroup getByInviteCode(String inviteCode) {
		if (inviteCode == null || inviteCode.isBlank()) {
			return null;
		}
		// 邀請碼一律以大寫比對，避免會員輸入小寫而比不到
		return baseMapper.selectByInviteCode(inviteCode.trim().toUpperCase());
	}

	@Override
	public RegistrationGroup getByOrdersId(Long ordersId) {
		if (ordersId == null) {
			return null;
		}
		return baseMapper.selectByOrdersId(ordersId);
	}

	@Override
	public List<RegistrationGroup> findByIds(Collection<Long> registrationGroupIds) {
		if (registrationGroupIds == null || registrationGroupIds.isEmpty()) {
			return List.of();
		}
		return baseMapper.selectBatchIds(registrationGroupIds);
	}

	@Override
	public List<RegistrationGroup> findByEventId(Long eventId) {
		return baseMapper.selectByEventId(eventId);
	}

	@Override
	public RegistrationGroup create(Long eventId, Long leaderMemberId, String name) {
		RegistrationGroup registrationGroup = new RegistrationGroup();
		registrationGroup.setEventId(eventId);
		registrationGroup.setLeaderMemberId(leaderMemberId);
		registrationGroup.setName(name);
		registrationGroup.setInviteCode(this.generateUniqueInviteCode());
		registrationGroup.setStatus(RegistrationGroupStatusEnum.RECRUITING);
		baseMapper.insert(registrationGroup);
		return registrationGroup;
	}

	@Override
	public String regenerateInviteCode(RegistrationGroup registrationGroup) {
		String inviteCode = this.generateUniqueInviteCode();

		RegistrationGroup update = new RegistrationGroup();
		update.setRegistrationGroupId(registrationGroup.getRegistrationGroupId());
		update.setInviteCode(inviteCode);
		baseMapper.updateById(update);

		return inviteCode;
	}

	@Override
	public boolean transitStatus(Long registrationGroupId, RegistrationGroupStatusEnum from,
			RegistrationGroupStatusEnum to, Long ordersId) {
		return baseMapper.transitStatus(registrationGroupId, from, to, ordersId) == 1;
	}

	@Override
	@Transactional
	public void releaseByOrdersId(Long ordersId) {

		RegistrationGroup registrationGroup = this.getByOrdersId(ordersId);
		if (registrationGroup == null) {
			return;
		}

		// 1.刪除成員在該活動的報名紀錄，否則重新鎖定時會被「已報名過此活動」擋下
		for (RegistrationGroupMember groupMember : registrationGroupMemberService
				.findByGroupId(registrationGroup.getRegistrationGroupId())) {
			AttendeeEvent attendeeEvent = attendeeEventService.getByMemberAndEvent(groupMember.getMemberId(),
					registrationGroup.getEventId());
			if (attendeeEvent != null) {
				attendeeEventService.remove(attendeeEvent.getAttendeeEventId());
			}
		}

		// 2.團體退回招募中並清空訂單ID
		baseMapper.transitStatus(registrationGroup.getRegistrationGroupId(), registrationGroup.getStatus(),
				RegistrationGroupStatusEnum.RECRUITING, null);
	}

	@Override
	public void remove(Long registrationGroupId) {
		baseMapper.deleteById(registrationGroupId);
	}

	/**
	 * 產生 DB 內不存在的邀請碼
	 *
	 * @return
	 */
	private String generateUniqueInviteCode() {
		for (int attempt = 0; attempt < MAX_GENERATE_ATTEMPTS; attempt++) {
			String candidate = this.composeInviteCode();
			if (baseMapper.selectExistingInviteCodes(List.of(candidate)).isEmpty()) {
				return candidate;
			}
		}
		throw new RegistrationGroupException("邀請碼產生失敗，請稍後再試");
	}

	private String composeInviteCode() {
		StringBuilder builder = new StringBuilder(INVITE_CODE_LENGTH);
		for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
			builder.append(INVITE_CODE_ALPHABET.charAt(RANDOM.nextInt(INVITE_CODE_ALPHABET.length())));
		}
		return builder.toString();
	}

}
