package tw.com.conference.pojo.VO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.RegistrationGroupRoleEnum;

/**
 * 團體成員 (僅負責人與後台看得到完整名單)
 */
@Data
public class RegistrationGroupMemberVO {

	@Schema(description = "會員ID")
	private Long memberId;

	@Schema(description = "姓名; 有中文姓名時優先顯示")
	private String name;

	@Schema(description = "E-Mail")
	private String email;

	@Schema(description = "會員身分 (中文)")
	private String memberTypeLabel;

	@Schema(description = "角色; leader=負責人, member=成員")
	private RegistrationGroupRoleEnum role;

	@Schema(description = "加入時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime joinDate;

}
