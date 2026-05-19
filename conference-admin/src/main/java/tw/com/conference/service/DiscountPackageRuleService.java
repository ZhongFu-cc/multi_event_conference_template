package tw.com.conference.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageRuleDTO;
import tw.com.conference.pojo.entity.DiscountPackageRule;

/**
 * <p>
 * 活動組合優惠 - 規則 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
public interface DiscountPackageRuleService extends IService<DiscountPackageRule> {

	
	/**
	 * 查詢觸發的組合優惠規則
	 * @param packageId 優惠組合ID
	 * @param hitCount 觸發數量
	 * @return
	 */
	List<DiscountPackageRule> findTriggerRule(Long packageId, int hitCount);
	
	/**
	 * 
	 * @param packageId
	 * @return
	 */
	List<DiscountPackageRule> findByPackageId(Long packageId);
	
	/**
	 * 獲取優惠組合規則
	 * 
	 * @param discountPackageRuleId
	 * @return
	 */
	DiscountPackageRule get(Long discountPackageRuleId);

	/**
	 * 創建優惠組合規則
	 * 
	 * @param addDiscountPackageRuleDTO
	 * @return
	 */
	DiscountPackageRule create(AddDiscountPackageRuleDTO addDiscountPackageRuleDTO);

	/**
	 * 更新優惠組合規則
	 * 
	 * @param putDiscountPackageRuleDTO
	 */
	void update(PutDiscountPackageRuleDTO putDiscountPackageRuleDTO);

	/**
	 * 刪除優惠組合規則
	 * 
	 * @param discountPackageRuleId
	 */
	void remove(Long discountPackageRuleId);

	/**
	 * 根據優惠組合ID刪除規則
	 * 
	 * @param discountPackageId
	 */
	void removeByDiscountPackageId(Long discountPackageId);



}
