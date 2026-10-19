package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.PromoCode;

/**
 * <p>
 * 優惠碼 Mapper 接口
 * </p>
 */
public interface PromoCodeMapper extends BaseMapper<PromoCode> {

	/**
	 * 占用一次使用額度<br>
	 * <br>
	 * 以「條件式 UPDATE」保證原子性，不需要分散鎖：<br>
	 * 影響列數 = 1 → 占用成功；= 0 → 額度已用完 (或碼已被邏輯刪除)<br>
	 * <br>
	 * max_uses = 0 代表不限次數，永遠占用成功
	 *
	 * @param promoCodeId
	 * @return 影響的列數
	 */
	@Update("UPDATE promo_code SET used_count = used_count + 1 "
			+ "WHERE promo_code_id = #{promoCodeId} AND is_deleted = 0 "
			+ "  AND (max_uses = 0 OR used_count < max_uses)")
	int reserveQuota(@Param("promoCodeId") Long promoCodeId);

	/**
	 * 釋放一次使用額度 (訂單被刪除時)
	 *
	 * @param promoCodeId
	 * @return 影響的列數
	 */
	@Update("UPDATE promo_code SET used_count = used_count - 1 "
			+ "WHERE promo_code_id = #{promoCodeId} AND used_count > 0")
	int releaseQuota(@Param("promoCodeId") Long promoCodeId);

	/**
	 * 以碼字串查詢 (code 有 UNIQUE 約束)
	 *
	 * @param code
	 * @return 查不到則為 null
	 */
	default PromoCode selectByCode(String code) {
		LambdaQueryWrapper<PromoCode> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCode::getCode, code);
		return this.selectOne(queryWrapper);
	}

	/**
	 * 查詢某個優惠碼活動底下的所有碼
	 *
	 * @param promoCampaignId
	 * @return
	 */
	default List<PromoCode> selectByCampaignId(Long promoCampaignId) {
		LambdaQueryWrapper<PromoCode> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCode::getPromoCampaignId, promoCampaignId).orderByAsc(PromoCode::getPromoCodeId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 查詢指定的碼字串中，哪些已經存在 (批次產生時用來避免碰撞)
	 *
	 * @param codes
	 * @return 已存在的碼
	 */
	default List<PromoCode> selectExistingCodes(Collection<String> codes) {
		if (codes == null || codes.isEmpty()) {
			return List.of();
		}
		LambdaQueryWrapper<PromoCode> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(PromoCode::getCode, codes);
		return this.selectList(queryWrapper);
	}

	/**
	 * 統計某個優惠碼活動的已使用次數總和
	 *
	 * @param promoCampaignId
	 * @return
	 */
	default int sumUsedCountByCampaignId(Long promoCampaignId) {
		return this.selectByCampaignId(promoCampaignId)
				.stream()
				.mapToInt(promoCode -> promoCode.getUsedCount() == null ? 0 : promoCode.getUsedCount())
				.sum();
	}

}
