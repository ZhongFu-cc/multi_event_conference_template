package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 優惠碼使用紀錄的狀態Enum
 */
@Getter
@AllArgsConstructor
public enum PromoRedemptionStatusEnum {

	/** 建立訂單時即占用額度，尚未付款 */
	RESERVED("reserved", "已占用,待付款"),

	/** 訂單付款成功 */
	REDEEMED("redeemed", "已使用");

	/** DB table 中的代號 */
	@EnumValue
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
	public static PromoRedemptionStatusEnum fromValue(String value) {
		for (PromoRedemptionStatusEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的優惠碼使用狀態: " + value);
	}

}
