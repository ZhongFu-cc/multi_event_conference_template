package tw.com.conference.service;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.enums.RegistrationGroupRoleEnum;
import tw.com.conference.pojo.entity.RegistrationGroupMember;

public interface RegistrationGroupMemberService extends IService<RegistrationGroupMember> {

	/**
	 * 查詢某團體的所有成員 (含負責人)，依加入順序
	 *
	 * @param registrationGroupId
	 * @return
	 */
	List<RegistrationGroupMember> findByGroupId(Long registrationGroupId);

	List<RegistrationGroupMember> findByGroupIds(Collection<Long> registrationGroupIds);

	/**
	 * 查詢會員參與的所有團體成員紀錄
	 *
	 * @param memberId
	 * @return
	 */
	List<RegistrationGroupMember> findByMemberId(Long memberId);

	/**
	 * 查詢某會員在某團體的成員紀錄
	 *
	 * @param registrationGroupId
	 * @param memberId
	 * @return 不是成員則為 null
	 */
	RegistrationGroupMember getByGroupAndMember(Long registrationGroupId, Long memberId);

	long countByGroupId(Long registrationGroupId);

	/**
	 * 會員在某場活動是否已屬於任何團體
	 *
	 * @param memberId
	 * @param eventId
	 * @return
	 */
	boolean isInGroupForEvent(Long memberId, Long eventId);

	RegistrationGroupMember addMember(Long registrationGroupId, Long memberId, RegistrationGroupRoleEnum role);

	void remove(Long registrationGroupMemberId);

	void removeByGroupId(Long registrationGroupId);

}
