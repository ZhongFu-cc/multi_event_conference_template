package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 優惠碼的碼型態Enum
 */
@Getter
@AllArgsConstructor
public enum PromoCodeTypeEnum {

	/** 單一通用碼，不限使用次數 (max_uses = 0) */
	GENERAL("general", "通用碼,不限使用次數"),

	/** 批次產生，每組限用一次 (max_uses = 1) */
	UNIQUE("unique", "一次性碼,每組限用一次");

	/** DB table 中的代號 */
	@EnumValue
	@JsonValue // JSON 輸出用
	private final String value;

	/** 說明 */
	private final String label;

	/**
	 * 此型態對應的 promo_code.max_uses 預設值
	 * 
	 * @return GENERAL = 0 (不限), UNIQUE = 1
	 */
	public int getDefaultMaxUses() {
		return this == GENERAL ? 0 : 1;
	}

	/**
	 * 從value獲取 Enum
	 * 
	 * @param value
	 * @return
	 */
	@JsonCreator // JSON 輸入用
	public static PromoCodeTypeEnum fromValue(String value) {
		for (PromoCodeTypeEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的優惠碼型態: " + value);
	}

}
