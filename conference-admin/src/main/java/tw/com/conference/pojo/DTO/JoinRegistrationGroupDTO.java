package tw.com.conference.pojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 以邀請碼加入團體
 */
@Data
public class JoinRegistrationGroupDTO {

	@NotBlank
	@Schema(description = "負責人提供的邀請碼, 不分大小寫")
	private String inviteCode;

}
