package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 贊助商等級Enum
 */
@Getter
@AllArgsConstructor
public enum SponsorTierEnum {

	DIAMOND("diamond", "鑽石級"),

	PLATINUM("platinum", "白金級"),

	GOLD("gold", "金級"),

	SILVER("silver", "銀級"),

	BRONZE("bronze", "銅級"),

	NONE("none", "無級別");

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
	public static SponsorTierEnum fromValue(String value) {
		for (SponsorTierEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的贊助等級: " + value);
	}

}
