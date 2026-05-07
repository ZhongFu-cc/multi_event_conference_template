package tw.com.conference.mapper;

import tw.com.conference.pojo.entity.DiscountPackageRule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
