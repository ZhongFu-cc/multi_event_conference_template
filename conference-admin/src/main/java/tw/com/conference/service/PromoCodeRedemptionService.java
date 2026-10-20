package tw.com.conference.service;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.entity.PromoCodeRedemption;

public interface PromoCodeRedemptionService extends IService<PromoCodeRedemption> {

	/**
	 * 查詢某張訂單的優惠碼使用紀錄
	 *
	 * @param ordersId
	 * @return 該訂單沒用優惠碼則為 null
	 */
	PromoCodeRedemption getByOrdersId(Long ordersId);

	/**
	 * 查詢某優惠碼活動的所有使用紀錄 (Excel 匯出用)
	 *
	 * @param promoCampaignId
	 * @return
	 */
	List<PromoCodeRedemption> findByCampaignId(Long promoCampaignId);

	/**
	 * 建立使用紀錄 (狀態為 reserved)
	 *
	 * @param promoCodeId
	 * @param promoCampaignId
	 * @param memberId
	 * @param ordersId
	 * @param discountAmount
	 * @return
	 */
	PromoCodeRedemption createReserved(Long promoCodeId, Long promoCampaignId, Long memberId, Long ordersId,
			BigDecimal discountAmount);

	/**
	 * 將某張訂單的使用紀錄標記為已使用 (付款成功時)
	 *
	 * @param ordersId
	 */
	void markRedeemed(Long ordersId);

	/**
	 * 移除某張訂單的使用紀錄 (訂單被刪除時)
	 *
	 * @param ordersId
	 */
	void removeByOrdersId(Long ordersId);

}
