package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 團體報名成員的角色Enum
 */
@Getter
@AllArgsConstructor
public enum RegistrationGroupRoleEnum {

	/** 負責人; 建立團體、鎖定名單並付款，一定出席且計入人數 */
	LEADER("leader", "負責人"),

	/** 透過邀請碼加入的成員 */
	MEMBER("member", "成員");

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
	public static RegistrationGroupRoleEnum fromValue(String value) {
		for (RegistrationGroupRoleEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的團體角色: " + value);
	}

}
