package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberTypeDTO {

	@NotBlank
	@Schema(description = "英文小寫代號")
	private String code;

	@NotBlank
	@Schema(description = "類型名稱-中文")
	private String labelZh;

	@NotBlank
	@Schema(description = "類型名稱-英文")
	private String labelEn;

}
