package tw.com.conference.pojo.VO;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.RegistrationGroupStatusEnum;
import tw.com.conference.pojo.entity.GroupDiscountRule;

/**
 * 團體報名的團體資訊<br>
 * <br>
 * 負責人 (與後台) 才看得到 inviteCode、ordersId、members；
 * 一般成員只看得到團體概況與自己的加入狀態。
 */
@Data
public class RegistrationGroupVO {

	@Schema(description = "團體ID")
	private Long registrationGroupId;

	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "活動主題")
	private String eventTitle;

	@Schema(description = "團體名稱; 系統自動產生, 格式為「負責人全名 - 活動名稱 - 團體報名」")
	private String name;

	@Schema(description = "狀態; recruiting=招募中, locked=已鎖定待付款, paid=已付款")
	private RegistrationGroupStatusEnum status;

	@Schema(description = "目前登入者是否為負責人")
	private Boolean isLeader;

	@Schema(description = "負責人姓名 (全名)")
	private String leaderName;

	@Schema(description = "目前人數 (含負責人)")
	private Integer memberCount;

	@Schema(description = "邀請碼; 僅負責人可見")
	private String inviteCode;

	@Schema(description = "鎖定後產生的訂單ID; 僅負責人可見")
	private Long ordersId;

	@Schema(description = "目前人數可套用的規則; 未達門檻為 null")
	private GroupDiscountRule currentRule;

	@Schema(description = "下一個可升級的規則; 已是最高優惠為 null")
	private GroupDiscountRule nextRule;

	@Schema(description = "此活動啟用中的所有規則, 依最低人數由小到大")
	private List<GroupDiscountRule> rules;

	@Schema(description = "成員名單 (含負責人); 僅負責人可見")
	private List<RegistrationGroupMemberVO> members;

	@Schema(description = "建立時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createDate;

}
