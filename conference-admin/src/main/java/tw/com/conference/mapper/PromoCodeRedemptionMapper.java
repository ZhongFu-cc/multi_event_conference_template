package tw.com.conference.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.PromoRedemptionStatusEnum;
import tw.com.conference.pojo.entity.PromoCodeRedemption;

/**
 * <p>
 * 優惠碼使用紀錄 Mapper 接口
 * </p>
 */
public interface PromoCodeRedemptionMapper extends BaseMapper<PromoCodeRedemption> {

	/**
	 * 查詢某張訂單的優惠碼使用紀錄 (ordersId 有 UNIQUE 約束，最多一筆)
	 *
	 * @param ordersId
	 * @return 該訂單沒使用優惠碼則為 null
	 */
	default PromoCodeRedemption selectByOrdersId(Long ordersId) {
		LambdaQueryWrapper<PromoCodeRedemption> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeRedemption::getOrdersId, ordersId);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 查詢某優惠碼活動底下所有的使用紀錄 (Excel 匯出用)
	 *
	 * @param promoCampaignId
	 * @return
	 */
	default List<PromoCodeRedemption> selectByCampaignId(Long promoCampaignId) {
		LambdaQueryWrapper<PromoCodeRedemption> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeRedemption::getPromoCampaignId, promoCampaignId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 將某張訂單的使用紀錄標記為已使用 (付款成功時)
	 *
	 * @param ordersId
	 */
	default void markRedeemedByOrdersId(Long ordersId) {
		LambdaUpdateWrapper<PromoCodeRedemption> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(PromoCodeRedemption::getStatus, PromoRedemptionStatusEnum.REDEEMED)
				.eq(PromoCodeRedemption::getOrdersId, ordersId);
		this.update(updateWrapper);
	}

	/**
	 * 移除某張訂單的使用紀錄 (訂單被刪除時)
	 *
	 * @param ordersId
	 */
	default void deleteByOrdersId(Long ordersId) {
		LambdaQueryWrapper<PromoCodeRedemption> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeRedemption::getOrdersId, ordersId);
		this.delete(queryWrapper);
	}

}
