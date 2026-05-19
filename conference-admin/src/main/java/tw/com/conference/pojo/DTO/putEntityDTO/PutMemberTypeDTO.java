package tw.com.conference.pojo.DTO.putEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.UsageContextEnum;

@Data
public class PutMemberTypeDTO {

	@Schema(description = "主鍵ID")
	@NotNull
	private Long memberTypeId;

	@NotBlank
	@Schema(description = "英文小寫代號")
	private String code;
	
	@NotNull
	@Schema(description = "身份的適用情境,通常是報名網頁 和 管理後台")
	private UsageContextEnum usageContext;

	@NotBlank
	@Schema(description = "類型名稱-中文")
	private String labelZh;

	@NotBlank
	@Schema(description = "類型名稱-英文")
	private String labelEn;
	
}
