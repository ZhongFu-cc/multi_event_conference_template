package tw.com.conference.manager;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.service.DiscountPackageItemService;
import tw.com.conference.service.DiscountPackageRuleService;
import tw.com.conference.service.DiscountPackageService;

@Component
@RequiredArgsConstructor
public class DiscountPackageManager {

	private final DiscountPackageService discountPackageService;
	private final DiscountPackageItemService discountPackageItemService;
	private final DiscountPackageRuleService discountPackageRuleService;

	/**
	 * 移除優惠組合
	 * 
	 * @param discountPackageId
	 */
	public void removeDiscountPackage(Long discountPackageId) {

		// 刪除優惠組合價格規則
		discountPackageRuleService.removeByDiscountPackageId(discountPackageId);

		// 刪除優惠組合池內的item 

		// 最終刪除優惠組合池
		discountPackageService.remove(discountPackageId);
	}

}
