package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 折扣類別Enum
 */
@Getter
@AllArgsConstructor
public enum DiscountTypeEnum {

	PERCENT("percent", "百分比折扣"), AMOUNT("amount", "固定折抵");

	@EnumValue
	@JsonValue // JSON 輸出用
	private final String value;

	private final String label;

	/**
	 * 從value獲取 Enum
	 * 
	 * @param value
	 * @return
	 */
	@JsonCreator // JSON 輸入用
	public static DiscountTypeEnum fromValue(String value) {
		for (DiscountTypeEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的折扣類型: " + value);
	}

	/**
	 * 從label獲取 Enum
	 * 
	 * @param label
	 * @return
	 */
	public static DiscountTypeEnum fromLabel(String label) {
		for (DiscountTypeEnum object : values()) {
			if (object.label.equals(label))
				return object;
		}
		throw new IllegalArgumentException("無效的折扣欄位類型: " + label);
	}

}
