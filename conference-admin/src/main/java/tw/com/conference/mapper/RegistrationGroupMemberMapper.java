package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.RegistrationGroupMember;

/**
 * <p>
 * 團體報名的成員 Mapper 接口
 * </p>
 */
public interface RegistrationGroupMemberMapper extends BaseMapper<RegistrationGroupMember> {

	/**
	 * 查詢會員在某場活動所屬的團體ID<br>
	 * 一個會員對同一場活動只能屬於一個團體
	 *
	 * @param memberId
	 * @param eventId
	 * @return 不屬於任何團體則為空
	 */
	@Select("SELECT m.registration_group_id FROM registration_group_member m "
			+ "JOIN registration_group g ON g.registration_group_id = m.registration_group_id "
			+ "WHERE m.member_id = #{memberId} AND g.event_id = #{eventId} "
			+ "  AND m.is_deleted = 0 AND g.is_deleted = 0")
	List<Long> selectGroupIdsByMemberAndEvent(@Param("memberId") Long memberId, @Param("eventId") Long eventId);

	/**
	 * 查詢某團體的所有成員，負責人在前，其餘依加入順序
	 *
	 * @param registrationGroupId
	 * @return
	 */
	default List<RegistrationGroupMember> selectByGroupId(Long registrationGroupId) {
		LambdaQueryWrapper<RegistrationGroupMember> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroupMember::getRegistrationGroupId, registrationGroupId)
				.orderByAsc(RegistrationGroupMember::getRegistrationGroupMemberId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 批次查詢多個團體的成員
	 *
	 * @param registrationGroupIds
	 * @return
	 */
	default List<RegistrationGroupMember> selectByGroupIds(Collection<Long> registrationGroupIds) {
		if (registrationGroupIds == null || registrationGroupIds.isEmpty()) {
			return List.of();
		}
		LambdaQueryWrapper<RegistrationGroupMember> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(RegistrationGroupMember::getRegistrationGroupId, registrationGroupIds)
				.orderByAsc(RegistrationGroupMember::getRegistrationGroupMemberId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢會員參與的所有團體成員紀錄
	 *
	 * @param memberId
	 * @return
	 */
	default List<RegistrationGroupMember> selectByMemberId(Long memberId) {
		LambdaQueryWrapper<RegistrationGroupMember> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroupMember::getMemberId, memberId)
				.orderByDesc(RegistrationGroupMember::getRegistrationGroupMemberId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢某會員在某團體的成員紀錄
	 *
	 * @param registrationGroupId
	 * @param memberId
	 * @return 不是成員則為 null
	 */
	default RegistrationGroupMember selectByGroupAndMember(Long registrationGroupId, Long memberId) {
		LambdaQueryWrapper<RegistrationGroupMember> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroupMember::getRegistrationGroupId, registrationGroupId)
				.eq(RegistrationGroupMember::getMemberId, memberId);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 統計某團體的成員數 (含負責人)
	 *
	 * @param registrationGroupId
	 * @return
	 */
	default long countByGroupId(Long registrationGroupId) {
		LambdaQueryWrapper<RegistrationGroupMember> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroupMember::getRegistrationGroupId, registrationGroupId);
		return this.selectCount(queryWrapper);
	}

	/**
	 * 刪除某團體的所有成員紀錄 (解散團體時)
	 *
	 * @param registrationGroupId
	 * @return
	 */
	default int deleteByGroupId(Long registrationGroupId) {
		LambdaQueryWrapper<RegistrationGroupMember> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroupMember::getRegistrationGroupId, registrationGroupId);
		return this.delete(queryWrapper);
	}

}
