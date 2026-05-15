package tw.com.conference.pojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupRegistrationDTO {

	@NotNull
	@Schema(description = "eventId , 活動ID")
	private Long eventId;

	@NotNull
	@Schema(description = "memberIds , 會員Ids , 可能會再替換")
	private List<Long> memberIds;

	@NotBlank
	private String verificationCode;

	@NotBlank
	private String verificationKey;
}
