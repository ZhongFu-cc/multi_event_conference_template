package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 國籍Enum
 */
@Getter
@AllArgsConstructor
public enum NationalityEnum {

	DOMESTIC("domestic", "本國籍"), INTERNATIONAL("international", "外國籍");

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
	public static NationalityEnum fromValue(String value) {
		for (NationalityEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的國籍類型: " + value);
	}

	/**
	 * 從label獲取 Enum
	 * 
	 * @param label
	 * @return
	 */
	public static NationalityEnum fromLabel(String label) {
		for (NationalityEnum object : values()) {
			if (object.label.equals(label))
				return object;
		}
		throw new IllegalArgumentException("無效的國籍欄位類型: " + label);
	}

}
