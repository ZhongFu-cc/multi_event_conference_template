package tw.com.conference.pojo.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import tw.com.conference.enums.UsageContextEnum;

/**
 * <p>
 * 會員身份列表
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Getter
@Setter
@TableName("member_type")
@Schema(name = "MemberType", description = "會員身份列表")
public class MemberType implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("member_type_id")
	private Long memberTypeId;

	@Schema(description = "身份的適用情境,通常是報名網頁 和 管理後台")
	@TableField("usage_context")
	private UsageContextEnum usageContext;

	@Schema(description = "英文小寫代號")
	@TableField("code")
	private String code;

	@Schema(description = "類型名稱-中文")
	@TableField("label_zh")
	private String labelZh;

	@Schema(description = "類型名稱-英文")
	@TableField("label_en")
	private String labelEn;

	@Schema(description = "邏輯刪除,預設為0活耀,1為刪除")
	@TableField("is_deleted")
	@TableLogic
	private Integer isDeleted;
}
