package tw.com.conference.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.DiscountTypeEnum;
import tw.com.conference.pojo.BO.AppliedDiscountBO;
import tw.com.conference.pojo.BO.CalculateResultBO;
import tw.com.conference.pojo.BO.CartItemBO;
import tw.com.conference.pojo.entity.DiscountPackage;
import tw.com.conference.pojo.entity.DiscountPackageItem;
import tw.com.conference.pojo.entity.DiscountPackageRule;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.service.DiscountPackageItemService;
import tw.com.conference.service.DiscountPackageRuleService;
import tw.com.conference.service.DiscountPackageService;

@Component
@RequiredArgsConstructor
public class CalculateDiscountHelper {

	private final DiscountPackageService discountPackageService;
	private final DiscountPackageItemService discountPackageItemService;
	private final DiscountPackageRuleService discountPackageRuleService;

	/**
	 * 計算最終價格結果
	 * 
	 * @param pricingRuleByEventId 每場活動對應的定價規則（含價格）
	 * @param eventIds             購物車活動 ID 清單
	 * @return 計算結果 BO (需付款金額、原始金額、折扣金額、應用折扣、Event項目)
	 */
	public CalculateResultBO calculateFinalPrices(Map<Long, PricingRule> pricingRuleByEventId, List<Long> eventIds) {
		// 1. 計算原始總價 ----
		BigDecimal originalTotal = eventIds.stream()
				.map(id -> pricingRuleByEventId.get(id).getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// 2. 找出所有相關的 DiscountPackageItem（package 與 event 的關聯）
		List<DiscountPackageItem> allItems = discountPackageItemService.findByEventIds(eventIds);

		// 3. 如果沒有任何折扣組合就可以直接返回結果了
		if (allItems.isEmpty()) {
			CalculateResultBO calculateResult = new CalculateResultBO();
			calculateResult.setOriginalTotal(originalTotal);
			calculateResult.setFinalPrice(originalTotal);
			calculateResult.setTotalDiscount(BigDecimal.ZERO);
			calculateResult.setAppliedDiscounts(Collections.emptyList());
			return calculateResult;
		}

		// 4.以 packageId 分組，建立每個 package 包含哪些 eventId
		Map<Long, List<Long>> packageEventMap = allItems.stream()
				.collect(Collectors.groupingBy(DiscountPackageItem::getDiscountPackageId,
						Collectors.mapping(DiscountPackageItem::getEventId, Collectors.toList())));

		// 5. 對每個 bundle 單獨計算最優折扣，並收集結果 ----
		List<AppliedDiscountBO> appliedDiscounts = new ArrayList<>();
		BigDecimal totalDiscount = BigDecimal.ZERO;

		for (Map.Entry<Long, List<Long>> entry : packageEventMap.entrySet()) {
			Long packageId = entry.getKey();
			List<Long> bundleEventIds = entry.getValue();

			// 組合優惠資訊
			DiscountPackage discountPackage = discountPackageService.get(packageId);

			// 找出此 bundle 中，購物車裡有的商品，並取得它們的價格
			List<CartItemBO> bundleCart = eventIds.stream()
					.filter(bundleEventIds::contains)
					.map(id -> new CartItemBO(id, pricingRuleByEventId.get(id).getAmount()))
					.collect(Collectors.toList());

			if (bundleCart.isEmpty())
				continue;

			// 查詢此 bundle 的所有有效折扣規則（按 priority 降序）
			List<DiscountPackageRule> rules = discountPackageRuleService.findByPackageId(packageId);

			// 用回溯找出此 bundle 的最大折扣
			BundleBestResult best = solveBundleBest(bundleCart, rules);

			if (best.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
				// 折扣總額
				totalDiscount = totalDiscount.add(best.getDiscount());
				// 應用折扣列表
				appliedDiscounts.add(new AppliedDiscountBO(packageId, discountPackage.getName(), best.getDiscount()));
			}
		}

		// 6. 組合最終結果
		BigDecimal finalPrice = originalTotal.subtract(totalDiscount);
		// 最終價格不得低於 0
		if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
		    finalPrice = BigDecimal.ZERO;
		}

		CalculateResultBO result = new CalculateResultBO();
		result.setOriginalTotal(originalTotal);
		result.setTotalDiscount(totalDiscount);
		result.setFinalPrice(finalPrice);
		result.setAppliedDiscounts(appliedDiscounts);
		// 也可放 event 明細，此處略
		return result;
	}

	/**
	 * 回溯求解單一 bundle 的最優折扣
	 * 
	 * @param items
	 * @param rules
	 * @return
	 */
	private BundleBestResult solveBundleBest(List<CartItemBO> items, List<DiscountPackageRule> rules) {
		List<ItemWrapper> wrappers = items.stream()
				.map(i -> new ItemWrapper(i.getEventId(), i.getOriginalPrice()))
				.collect(Collectors.toList());

		List<BigDecimal> maxDiscount = new ArrayList<>();
		maxDiscount.add(BigDecimal.ZERO);

		// 回溯求解，折扣結果返回賦值給maxDiscount
		backtrack(wrappers, rules, BigDecimal.ZERO, maxDiscount);

		return new BundleBestResult(maxDiscount.get(0), "");
	}

	/**
	 * 
	 * @param items           商品組合及價格
	 * @param rules           套用折扣規則
	 * @param currentDiscount 起始折扣金額
	 * @param maxDiscount     最大折扣金額
	 */
	private void backtrack(List<ItemWrapper> items, List<DiscountPackageRule> rules, BigDecimal currentDiscount,
			List<BigDecimal> maxDiscount) {
		// 是否應用任何規則
		boolean anyApplied = false;

		for (DiscountPackageRule rule : rules) {
			int required = rule.getRequiredCount();

			// 收集未使用商品索引
			List<Integer> unusedIndices = new ArrayList<>();
			for (int i = 0; i < items.size(); i++) {
				if (!items.get(i).used) {
					unusedIndices.add(i);
				}
			}

			// 如果未使用商品索引數量 小於 需要的達成此優惠的最低數量跳過
			if (unusedIndices.size() < required)
				continue;

			// 得到所有可能組合
			List<List<Integer>> combinations = combinations(unusedIndices, required);
			for (List<Integer> comb : combinations) {
				// 標記已用
				for (int idx : comb)
					items.get(idx).used = true;

				// 計算此組合原價
				BigDecimal groupTotal = comb.stream()
						.map(idx -> items.get(idx).price)
						.reduce(BigDecimal.ZERO, BigDecimal::add);

				// 計算折扣金額
				BigDecimal discount = calcDiscount(groupTotal, rule);

				// 遞迴 + 回朔
				backtrack(items, rules, currentDiscount.add(discount), maxDiscount);

				// 回溯,讓下comb還能繼續使用
				for (int idx : comb)
					items.get(idx).used = false;

				anyApplied = true;
			}
		}

		// 當所有組合 和 規則都被應用完，累積的折扣金額就是最好的折扣
		if (!anyApplied) {
			if (currentDiscount.compareTo(maxDiscount.get(0)) > 0) {
				maxDiscount.set(0, currentDiscount);
			}
		}
	}

	/**
	 * 計算折扣
	 * 
	 * @param total 組合金額總額
	 * @param rule  折扣規則
	 * @return
	 */
	private BigDecimal calcDiscount(BigDecimal total, DiscountPackageRule rule) {
		// 折扣額度轉換成BigDecimal型式
		BigDecimal discountValue = BigDecimal.valueOf(rule.getDiscountValue());

		if (DiscountTypeEnum.PERCENT.equals(rule.getDiscountType())) {

			// 若 discountValue=80 代表支付 80%（8折）
			// 即：80/100 = 0.8 , 取小數點0位,四捨五入
			BigDecimal payPercent = discountValue.divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
			// 1 - 減免百分比 = 折扣金額%數
			BigDecimal discountPercent = BigDecimal.ONE.subtract(payPercent);
			// 當前金額 * 折扣百分比 = 折扣金額,把計算結果的小數部分處理掉，四捨五入到整數（元）
			return total.multiply(discountPercent).setScale(0, RoundingMode.HALF_UP);

		} else { // amount
			// 直接返回折扣金額
			return discountValue;
		}
	}

	/**
	 * 組合生成套用優惠的可能性
	 * 
	 * @param list 未使用商品索引集合
	 * @param k    需要觸發組合的優惠數量
	 * @return
	 */
	private List<List<Integer>> combinations(List<Integer> list, int k) {
		// 準備一個空盒子，裝所有組合
		List<List<Integer>> result = new ArrayList<>();
		// 叫 Helper 開始遞迴生成
		combineHelper(list, k, 0, new ArrayList<>(), result);
		return result;
	}

	/**
	 * 
	 * @param list    可以選的索引清單，例如 [0, 1, 2]（代表商品 P, Q, R）
	 * @param k       需要選幾個，例如 2
	 * @param start   從 list 的哪個位置開始挑（避免重複選）
	 * @param current 目前正在建構中的組合
	 * @param result  最終存放所有完整組合的容器
	 */
	private void combineHelper(List<Integer> list, int k, int start, List<Integer> current,
			List<List<Integer>> result) {

		// 終止條件：目前組合的數量已達 k 個
		if (current.size() == k) {
			// 複製一份，存入結果
			result.add(new ArrayList<>(current));
			// 這個分支結束
			return;
		}
		// 從 start 開始嘗試每個可挑的索引
		for (int i = start; i < list.size(); i++) {
			// 挑選 list[i] 加入組合
			current.add(list.get(i));
			// 遞歸，往下繼續挑下一個
			combineHelper(list, k, i + 1, current, result);
			// 還原，換下一個選擇（回溯）
			current.remove(current.size() - 1);
		}
	}

	// 內部輔助類
	private static class ItemWrapper {
		Long eventId;
		BigDecimal price;
		boolean used = false;

		ItemWrapper(Long eventId, BigDecimal price) {
			this.eventId = eventId;
			this.price = price;
		}
	}

	@Data
	@AllArgsConstructor
	private static class BundleBestResult {
		private BigDecimal discount;
		private String ruleName;
	}

}
