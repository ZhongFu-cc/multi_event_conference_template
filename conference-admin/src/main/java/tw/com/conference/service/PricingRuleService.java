package tw.com.conference.service;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.pojo.DTO.addEntityDTO.AddPricingRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPricingRuleDTO;
import tw.com.conference.pojo.VO.PriceDashboardVO;
import tw.com.conference.pojo.entity.PricingRule;

/**
 * <p>
 * 價格規則表 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface PricingRuleService extends IService<PricingRule> {

	/**
	 * 判斷是否有任何價格規則
	 * 
	 * @return
	 */
	boolean existAny();

	/**
	 * 獲取單一價格規則
	 * 
	 * @param pricingRuleId
	 * @return
	 */
	PricingRule get(Long pricingRuleId);

	/**
	 * 查詢該活動所有規則
	 * 
	 * @param eventId
	 * @return
	 */
	List<PricingRule> findPricingRulesByEventId(Long eventId);

	/**
	 * 根據條件解析適用的價格規則
	 * 
	 * @param eventId      事件ID
	 * @param memberTypeId 身分類別ID
	 * @param nationality  國籍
	 * @param targetTime   目標時間
	 * @return
	 */
	PricingRule resolvePricingRule(Long eventId, Long memberTypeId, NationalityEnum nationality,
			LocalDateTime targetTime);

	/**
	 * 新增價格規則
	 * 
	 * @param addPricingRuleDTO
	 */
	PricingRule create(AddPricingRuleDTO addPricingRuleDTO);

	/**
	 * 修改價格規則
	 * 
	 * @param putPricingRuleDTO
	 */
	void update(PutPricingRuleDTO putPricingRuleDTO);

	/**
	 * 刪除價格規則
	 * 
	 * @param pricingRuleId
	 */
	void remove(Long pricingRuleId);

}
