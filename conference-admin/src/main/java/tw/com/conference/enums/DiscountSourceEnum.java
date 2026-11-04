package tw.com.conference.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 折扣來源Enum<br>
 * 用於 AppliedDiscountBO，讓前端知道這筆折扣是活動組合優惠、優惠碼還是團體報名優惠。<br>
 * 三者為擇一使用，所以同一張訂單的 appliedDiscounts 只會有單一來源。
 */
@Getter
@AllArgsConstructor
public enum DiscountSourceEnum {

	/** 活動組合優惠 (discount_package) */
	BUNDLE("bundle", "活動組合優惠"),

	/** 優惠碼 */
	PROMO_CODE("promo-code", "優惠碼"),

	/** 團體報名優惠 (group_discount_rule) */
	GROUP("group", "團體報名優惠");

	/** JSON 中的代號 (存於 orders.applied_discounts) */
	@JsonValue // JSON 輸出用
	private final String value;

	/** 說明 */
	private final String label;

	/**
	 * 從value獲取 Enum
	 * 
	 * @param value
	 * @return
	 */
	@JsonCreator // JSON 輸入用
	public static DiscountSourceEnum fromValue(String value) {
		for (DiscountSourceEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的折扣來源: " + value);
	}

}
