package tw.com.conference.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.pojo.entity.PricingRule;

/**
 * <p>
 * 價格規則表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface PricingRuleMapper extends BaseMapper<PricingRule> {

	/**
	 * 查詢該活動所有的價格規則
	 * 
	 * @param eventId
	 * @return
	 */
	default List<PricingRule> selectAllRulesByEvnetId(Long eventId) {
		return this.selectList(new LambdaQueryWrapper<PricingRule>().eq(PricingRule::getEventId, eventId)
				.orderByDesc(PricingRule::getMemberTypeId));

	}

	/**
	 * 查詢條件查詢符合的價格策略
	 * 
	 * @param eventId      事件ID
	 * @param memberTypeId 身分類別ID
	 * @param nationality  國籍
	 * @param targetTime   目標時間
	 * @return
	 */
	default PricingRule selectPricingRuleByQuery(Long eventId, Long memberTypeId, NationalityEnum nationality,
			LocalDateTime targetTime) {

		LambdaQueryWrapper<PricingRule> queryWrapper = new LambdaQueryWrapper<>();

		queryWrapper
				// 1. 必備固定條件
				.eq(PricingRule::getEventId, eventId)
				.eq(PricingRule::getNationality, nationality.getValue())
				.le(PricingRule::getDateFrom, targetTime)
				.ge(PricingRule::getDateTo, targetTime)

				// 2. 身份判定：(身份 = 傳入的 ID) OR (身份 IS NULL)
				.and(wrapper -> wrapper.eq(PricingRule::getMemberTypeId, memberTypeId)
						.or()
						.isNull(PricingRule::getMemberTypeId))

				// 3. 優先級排序：特定身分 (有數字) 會排在通用身分 (NULL) 之前
				// 在 MySQL 中，DESC 排序會讓數字在前，NULL 在最後
				.orderByDesc(PricingRule::getMemberTypeId)

				// 4. 只取符合優先順序的第一筆
				.last("LIMIT 1");

		return this.selectOne(queryWrapper);
	}

	/**
	 * 判斷 價格規則 表內是否有任何資料<br>
	 * 使用 MySQL 的 EXISTS 語法，只要找到第一筆就會停止掃描，效能最優。
	 */
	@Select("SELECT EXISTS(SELECT 1 FROM pricing_rule WHERE is_deleted = 0 LIMIT 1)")
	boolean existAnyPricingRule();

	/**
	 * 根據 EventId 刪除符合的項目
	 * 
	 * @param eventId
	 */
	default void deleteByEventId(Long eventId) {
		LambdaQueryWrapper<PricingRule> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PricingRule::getEventId, eventId);
		this.delete(queryWrapper);
	};

}
