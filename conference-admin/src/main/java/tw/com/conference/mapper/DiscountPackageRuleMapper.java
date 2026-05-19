package tw.com.conference.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.DiscountPackageRule;

/**
 * <p>
 * 活動組合優惠 - 規則 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
public interface DiscountPackageRuleMapper extends BaseMapper<DiscountPackageRule> {

	/**
	 * 根據packageId,查尋符合的條件
	 * 
	 * @param discountPackageId 優惠組合ID
	 * @return
	 */
	default List<DiscountPackageRule> selectByPackageId(Long discountPackageId) {
		LambdaQueryWrapper<DiscountPackageRule> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DiscountPackageRule::getDiscountPackageId, discountPackageId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 根據packageId和requiredCount,查尋符合的條件
	 * 
	 * @param discountPackageId 優惠組合ID
	 * @param requiredCount     觸發的事件數量
	 * @return
	 */
	default List<DiscountPackageRule> selectByPackageIdAndRequiredCountLessThanEqual(Long discountPackageId,
			int requiredCount) {
		LambdaQueryWrapper<DiscountPackageRule> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DiscountPackageRule::getDiscountPackageId, discountPackageId)
				.le(DiscountPackageRule::getRequiredCount, requiredCount);

		return this.selectList(queryWrapper);
	}

	/**
	 * 根據優惠組合ID ,刪除優惠組合規格
	 * 
	 * @param discountPackageId
	 */
	default void deleteByDiscountPackageId(Long discountPackageId) {
		LambdaQueryWrapper<DiscountPackageRule> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DiscountPackageRule::getDiscountPackageId, discountPackageId);

		this.delete(queryWrapper);
	}

}
