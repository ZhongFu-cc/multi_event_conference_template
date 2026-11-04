package tw.com.conference.pojo.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.RegistrationGroupStatusEnum;

/**
 * 輸入邀請碼後、確認加入前看到的團體概況<br>
 * 刻意不含成員名單，避免拿到邀請碼的人就能看到其他成員資料
 */
@Data
public class GroupInvitePreviewVO {

	@Schema(description = "團體ID")
	private Long registrationGroupId;

	@Schema(description = "團體名稱; 系統自動產生, 格式為「負責人全名 - 活動名稱 - 團體報名」")
	private String groupName;

	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "活動主題")
	private String eventTitle;

	@Schema(description = "負責人姓名 (全名)")
	private String leaderName;

	@Schema(description = "目前人數 (含負責人)")
	private Integer memberCount;

	@Schema(description = "狀態; 只有 recruiting 可以加入")
	private RegistrationGroupStatusEnum status;

	@Schema(description = "目前登入者是否已是此團體成員")
	private Boolean alreadyJoined;

}
