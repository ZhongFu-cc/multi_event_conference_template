package tw.com.conference.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 使用情境Enum
 */
@Getter
@AllArgsConstructor
public enum UsageContextEnum {

	FRONTEND("frontend", "一般報名使用"), BACKEND("backend", "後台新增使用");

	@EnumValue
	@JsonValue // JSON 輸出用
	private final String value;
	
	private final String label;
	
}
