package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 優惠碼折扣型態Enum<br>
 * <br>
 * 分兩群，作用層級不同：<br>
 * 逐場型態 (FREE / AMOUNT / PERCENT)：套用於「適用範圍 ∩ 報名場次」的每一場，單場折抵上限為該場原價<br>
 * 訂單層態 (ALL_AMOUNT / ALL_PERCENT)：忽略適用範圍，直接折抵原價總額<br>
 * <br>
 * 注意：此處的 PERCENT 是「**減免**百分比」(20 = 折 20%)，
 * 與 {@link DiscountTypeEnum#PERCENT} 的「支付百分比」(80 = 付 80% = 8折) 語意相反，
 * 刻意分成兩個 enum 以免混用。
 */
@Getter
@AllArgsConstructor
public enum PromoDiscountTypeEnum {

	/** 逐場 - 適用範圍內該場原價全免 */
	FREE("free", "適用活動免費", true),

	/** 逐場 - 適用範圍內每場各折抵 min(discountValue, 該場原價) */
	AMOUNT("amount", "適用活動各折抵固定金額", true),

	/** 逐場 - 適用範圍內每場各折抵 該場原價 × discountValue/100 */
	PERCENT("percent", "適用活動各折抵百分比", true),

	/** 訂單層 - 對原價總額折抵 min(discountValue, 原價總額) */
	ALL_AMOUNT("all_amount", "整張訂單折抵固定金額", false),

	/** 訂單層 - 對原價總額折抵 原價總額 × discountValue/100 */
	ALL_PERCENT("all_percent", "整張訂單折抵百分比", false);

	/** DB table 中的代號 */
	@EnumValue
	@JsonValue // JSON 輸出用
	private final String value;

	/** 說明 */
	private final String label;

	/**
	 * 是否為「逐場型態」<br>
	 * true = 需要參考 promo_code_event 的適用範圍<br>
	 * false = 訂單層態，忽略適用範圍
	 */
	private final boolean perEvent;

	/**
	 * 此型態是否需要 discountValue
	 *
	 * @return FREE 不需要，其餘皆需要
	 */
	public boolean isDiscountValueRequired() {
		return this != FREE;
	}

	/**
	 * 此型態的 discountValue 是否為百分比 (需限制 1~100)
	 *
	 * @return
	 */
	public boolean isPercentBased() {
		return this == PERCENT || this == ALL_PERCENT;
	}

	/**
	 * 從value獲取 Enum
	 *
	 * @param value
	 * @return
	 */
	@JsonCreator // JSON 輸入用
	public static PromoDiscountTypeEnum fromValue(String value) {
		for (PromoDiscountTypeEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的優惠碼折扣型態: " + value);
	}

}
