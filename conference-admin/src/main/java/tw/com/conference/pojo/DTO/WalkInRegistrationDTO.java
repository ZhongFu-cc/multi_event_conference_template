package tw.com.conference.pojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalkInRegistrationDTO {
	
	@Schema(description = "中文姓名，外國人非必填，台灣人必填")
	private String chineseName;
	
	@Schema(description = "英文-名字, 華人的名在後  , 外國人的名在前")
	private String firstName;

	@Schema(description = "英文-姓氏, 華人的姓氏在前, 外國人的姓氏在後")
	private String lastName;
	
	@NotBlank
	@Schema(description = "E-Mail")
	private String email;
	
	@NotNull
	@Schema(description = "會員類別ID")
	private Long memberTypeId;

	@NotBlank
	@Schema(description = "國家, 用於判斷本國/外國籍價格")
	private String country;

	@Schema(description = "要報名的活動ID列表; 未提供時預設報名主活動")
	private List<Long> eventIds;
	
	
	
}
