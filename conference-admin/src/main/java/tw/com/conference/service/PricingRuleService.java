package tw.com.conference.service;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddPricingRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPricingRuleDTO;
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
	 * @return
	 */
	boolean existAny();

	/**
	 * 獲取單一價格規則
	 * @param pricingRuleId
	 * @return
	 */
	PricingRule get(Long pricingRuleId);

	/**
	 * 新增價格規則
	 * @param addPricingRuleDTO
	 */
	PricingRule create(AddPricingRuleDTO addPricingRuleDTO);

	/**
	 * 修改價格規則
	 * @param putPricingRuleDTO
	 */
	void update(PutPricingRuleDTO putPricingRuleDTO);

	/**
	 * 刪除價格規則
	 * @param pricingRuleId
	 */
	void remove(Long pricingRuleId);
	
}
