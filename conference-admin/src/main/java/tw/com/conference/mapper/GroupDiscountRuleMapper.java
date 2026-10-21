package tw.com.conference.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.pojo.entity.GroupDiscountRule;

/**
 * <p>
 * 團體報名優惠規則 Mapper 接口
 * </p>
 */
public interface GroupDiscountRuleMapper extends BaseMapper<GroupDiscountRule> {

	/**
	 * 查詢某場活動的所有規則 (含停用)，依最低人數由小到大
	 *
	 * @param eventId
	 * @return
	 */
	default List<GroupDiscountRule> selectByEventId(Long eventId) {
		LambdaQueryWrapper<GroupDiscountRule> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(GroupDiscountRule::getEventId, eventId).orderByAsc(GroupDiscountRule::getMinCount);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢某場活動啟用中的規則，依最低人數由小到大
	 *
	 * @param eventId
	 * @return
	 */
	default List<GroupDiscountRule> selectActiveByEventId(Long eventId) {
		LambdaQueryWrapper<GroupDiscountRule> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(GroupDiscountRule::getEventId, eventId)
				.eq(GroupDiscountRule::getIsActive, CommonStatusEnum.YES)
				.orderByAsc(GroupDiscountRule::getMinCount);
		return this.selectList(queryWrapper);
	}

}
