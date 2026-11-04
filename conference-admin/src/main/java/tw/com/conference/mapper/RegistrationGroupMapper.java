package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.RegistrationGroupStatusEnum;
import tw.com.conference.pojo.entity.RegistrationGroup;

/**
 * <p>
 * 團體報名的團體 Mapper 接口
 * </p>
 */
public interface RegistrationGroupMapper extends BaseMapper<RegistrationGroup> {

	/**
	 * 轉換團體狀態並同時寫入訂單ID<br>
	 * <br>
	 * 以「條件式 UPDATE」確保狀態只會從 fromStatus 轉出一次：<br>
	 * 影響列數 = 1 → 轉換成功；= 0 → 狀態已被其他請求改變
	 *
	 * @param registrationGroupId
	 * @param fromStatus          目前應處於的狀態
	 * @param toStatus            要轉換成的狀態
	 * @param ordersId            要寫入的訂單ID，可為 null (退回招募中時清空)
	 * @return 影響的列數
	 */
	// Enum 參數由 MyBatis-Plus 的 CompositeEnumTypeHandler 依 @EnumValue 轉成 DB 代號
	@Update("UPDATE registration_group SET status = #{toStatus}, orders_id = #{ordersId}, update_date = NOW() "
			+ "WHERE registration_group_id = #{registrationGroupId} AND status = #{fromStatus} AND is_deleted = 0")
	int transitStatus(@Param("registrationGroupId") Long registrationGroupId,
			@Param("fromStatus") RegistrationGroupStatusEnum fromStatus,
			@Param("toStatus") RegistrationGroupStatusEnum toStatus, @Param("ordersId") Long ordersId);

	/**
	 * 以邀請碼查詢 (invite_code 有 UNIQUE 約束)
	 *
	 * @param inviteCode
	 * @return 查不到則為 null
	 */
	default RegistrationGroup selectByInviteCode(String inviteCode) {
		LambdaQueryWrapper<RegistrationGroup> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroup::getInviteCode, inviteCode);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 以訂單ID查詢團體 (判斷訂單是否為團體訂單)
	 *
	 * @param ordersId
	 * @return 不是團體訂單則為 null
	 */
	default RegistrationGroup selectByOrdersId(Long ordersId) {
		LambdaQueryWrapper<RegistrationGroup> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroup::getOrdersId, ordersId);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 查詢某場活動的所有團體 (新到舊)
	 *
	 * @param eventId
	 * @return
	 */
	default List<RegistrationGroup> selectByEventId(Long eventId) {
		LambdaQueryWrapper<RegistrationGroup> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(RegistrationGroup::getEventId, eventId).orderByDesc(RegistrationGroup::getRegistrationGroupId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢指定的邀請碼中，哪些已經存在 (產生邀請碼時用來避免碰撞)
	 *
	 * @param inviteCodes
	 * @return
	 */
	default List<RegistrationGroup> selectExistingInviteCodes(Collection<String> inviteCodes) {
		if (inviteCodes == null || inviteCodes.isEmpty()) {
			return List.of();
		}
		LambdaQueryWrapper<RegistrationGroup> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(RegistrationGroup::getInviteCode, inviteCodes);
		return this.selectList(queryWrapper);
	}

}
