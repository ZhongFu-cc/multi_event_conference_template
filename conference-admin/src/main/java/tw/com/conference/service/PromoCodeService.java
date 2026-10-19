package tw.com.conference.service;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.entity.PromoCode;

public interface PromoCodeService extends IService<PromoCode> {

	PromoCode get(Long promoCodeId);

	/**
	 * 以碼字串查詢
	 *
	 * @param code
	 * @return 查不到則為 null
	 */
	PromoCode getByCode(String code);

	/**
	 * 查詢某優惠碼活動底下的所有碼
	 *
	 * @param promoCampaignId
	 * @return
	 */
	List<PromoCode> findByCampaignId(Long promoCampaignId);

	/**
	 * 批次產生碼<br>
	 * 碼字串以 SecureRandom 產生，排除易誤認字元，並以 unique index 擋碰撞
	 *
	 * @param promoCampaignId
	 * @param codePrefix      前綴，可為 null
	 * @param quantity        要產生幾組
	 * @param maxUses         每組的使用次數上限 (0 = 不限)
	 * @return 產生出的碼
	 */
	List<PromoCode> batchGenerate(Long promoCampaignId, String codePrefix, int quantity, int maxUses);

	/**
	 * 以指定字串建立單一碼 (通用碼由後台自訂碼字串時使用)
	 *
	 * @param promoCampaignId
	 * @param code
	 * @param maxUses
	 * @return
	 */
	PromoCode createWithCode(Long promoCampaignId, String code, int maxUses);

	/**
	 * 占用一次使用額度<br>
	 * 以條件式 UPDATE 保證原子性
	 *
	 * @param promoCodeId
	 * @return true = 占用成功, false = 額度已用完
	 */
	boolean reserveQuota(Long promoCodeId);

	/**
	 * 釋放一次使用額度 (訂單被刪除時)
	 *
	 * @param promoCodeId
	 */
	void releaseQuota(Long promoCodeId);

	/**
	 * 判斷此碼是否還有可用額度 (僅查詢，不占用)<br>
	 * 供試算端點使用；真正結帳時仍需以 reserveQuota 的回傳值為準
	 *
	 * @param promoCode
	 * @return
	 */
	boolean hasQuota(PromoCode promoCode);

	/**
	 * 移除某優惠碼活動底下的所有碼
	 *
	 * @param promoCampaignId
	 */
	void removeByCampaignId(Long promoCampaignId);

	/**
	 * 查詢指定碼字串中已存在的部分 (批次產生時避免碰撞)
	 *
	 * @param codes
	 * @return
	 */
	List<String> findExistingCodes(Collection<String> codes);

}
