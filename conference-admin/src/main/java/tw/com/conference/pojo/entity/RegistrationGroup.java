package tw.com.conference.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import tw.com.conference.enums.RegistrationGroupStatusEnum;

/**
 * <p>
 * 團體報名的團體表<br>
 * 一團只報名一場活動、鎖定後只產生一張訂單 (由負責人付款)
 * </p>
 */
@Getter
@Setter
@TableName("registration_group")
@Schema(name = "RegistrationGroup", description = "團體報名的團體表")
public class RegistrationGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("registration_group_id")
	private Long registrationGroupId;

	@Schema(description = "報名的活動事件ID")
	@TableField("event_id")
	private Long eventId;

	@Schema(description = "負責人會員ID")
	@TableField("leader_member_id")
	private Long leaderMemberId;

	@Schema(description = "團體名稱; 系統自動產生, 格式為「負責人全名 - 活動名稱 - 團體報名」")
	@TableField("name")
	private String name;

	@Schema(description = "邀請碼")
	@TableField("invite_code")
	private String inviteCode;

	@Schema(description = "狀態; recruiting=招募中, locked=已鎖定待付款, paid=已付款")
	@TableField("status")
	private RegistrationGroupStatusEnum status;

	@Schema(description = "鎖定後產生的訂單ID")
	@TableField("orders_id")
	private Long ordersId;

	@Schema(description = "創建者")
	@TableField("create_by")
	private String createBy;

	@Schema(description = "創建時間")
	@TableField(value = "create_date", fill = FieldFill.INSERT)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createDate;

	@Schema(description = "最後修改者")
	@TableField("update_by")
	private String updateBy;

	@Schema(description = "最後修改時間")
	@TableField(value = "update_date", fill = FieldFill.UPDATE)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;

	@Schema(description = "邏輯刪除,預設為0活耀,1為刪除")
	@TableField("is_deleted")
	@TableLogic
	private Integer isDeleted;
}
