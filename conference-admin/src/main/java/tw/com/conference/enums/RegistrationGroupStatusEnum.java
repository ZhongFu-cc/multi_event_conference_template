package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 團體報名的團體狀態Enum<br>
 * <br>
 * recruiting → (負責人鎖定, 建立訂單) → locked → (付款成功) → paid<br>
 * locked 狀態下訂單被刪除時，會退回 recruiting
 */
@Getter
@AllArgsConstructor
public enum RegistrationGroupStatusEnum {

	/** 招募中，成員可透過邀請碼加入 / 自行退出 */
	RECRUITING("recruiting", "招募中"),

	/** 名單已鎖定並建立訂單，待負責人付款 */
	LOCKED("locked", "已鎖定,待付款"),

	/** 訂單付款成功 */
	PAID("paid", "已付款");

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
	public static RegistrationGroupStatusEnum fromValue(String value) {
		for (RegistrationGroupStatusEnum object : values()) {
			if (object.value.equals(value))
				return object;
		}
		throw new IllegalArgumentException("無效的團體狀態: " + value);
	}

}
