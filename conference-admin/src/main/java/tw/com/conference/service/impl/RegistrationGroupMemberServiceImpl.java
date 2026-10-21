package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import tw.com.conference.enums.RegistrationGroupRoleEnum;
import tw.com.conference.mapper.RegistrationGroupMemberMapper;
import tw.com.conference.pojo.entity.RegistrationGroupMember;
import tw.com.conference.service.RegistrationGroupMemberService;

@Service
public class RegistrationGroupMemberServiceImpl extends
		ServiceImpl<RegistrationGroupMemberMapper, RegistrationGroupMember> implements RegistrationGroupMemberService {

	@Override
	public List<RegistrationGroupMember> findByGroupId(Long registrationGroupId) {
		return baseMapper.selectByGroupId(registrationGroupId);
	}

	@Override
	public List<RegistrationGroupMember> findByGroupIds(Collection<Long> registrationGroupIds) {
		return baseMapper.selectByGroupIds(registrationGroupIds);
	}

	@Override
	public List<RegistrationGroupMember> findByMemberId(Long memberId) {
		return baseMapper.selectByMemberId(memberId);
	}

	@Override
	public RegistrationGroupMember getByGroupAndMember(Long registrationGroupId, Long memberId) {
		return baseMapper.selectByGroupAndMember(registrationGroupId, memberId);
	}

	@Override
	public long countByGroupId(Long registrationGroupId) {
		return baseMapper.countByGroupId(registrationGroupId);
	}

	@Override
	public boolean isInGroupForEvent(Long memberId, Long eventId) {
		return !baseMapper.selectGroupIdsByMemberAndEvent(memberId, eventId).isEmpty();
	}

	@Override
	public RegistrationGroupMember addMember(Long registrationGroupId, Long memberId,
			RegistrationGroupRoleEnum role) {
		RegistrationGroupMember groupMember = new RegistrationGroupMember();
		groupMember.setRegistrationGroupId(registrationGroupId);
		groupMember.setMemberId(memberId);
		groupMember.setRole(role);
		baseMapper.insert(groupMember);
		return groupMember;
	}

	@Override
	public void remove(Long registrationGroupMemberId) {
		baseMapper.deleteById(registrationGroupMemberId);
	}

	@Override
	public void removeByGroupId(Long registrationGroupId) {
		baseMapper.deleteByGroupId(registrationGroupId);
	}

}
