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
import tw.com.conference.enums.RegistrationGroupRoleEnum;

/**
 * <p>
 * 團體報名的成員表 (含負責人)
 * </p>
 */
@Getter
@Setter
@TableName("registration_group_member")
@Schema(name = "RegistrationGroupMember", description = "團體報名的成員表")
public class RegistrationGroupMember implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("registration_group_member_id")
	private Long registrationGroupMemberId;

	@Schema(description = "團體ID")
	@TableField("registration_group_id")
	private Long registrationGroupId;

	@Schema(description = "會員ID")
	@TableField("member_id")
	private Long memberId;

	@Schema(description = "角色; leader=負責人, member=成員")
	@TableField("role")
	private RegistrationGroupRoleEnum role;

	@Schema(description = "創建者")
	@TableField("create_by")
	private String createBy;

	@Schema(description = "加入時間")
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
