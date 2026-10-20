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

/**
 * <p>
 * 優惠碼表<br>
 * maxUses = 0 代表不限次數(通用碼), = 1 代表一次性碼
 * </p>
 */
@Getter
@Setter
@TableName("promo_code")
@Schema(name = "PromoCode", description = "優惠碼表")
public class PromoCode implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("promo_code_id")
	private Long promoCodeId;

	@Schema(description = "優惠碼活動ID")
	@TableField("promo_campaign_id")
	private Long promoCampaignId;

	@Schema(description = "優惠碼字串")
	@TableField("code")
	private String code;

	@Schema(description = "可使用次數上限, 0 為不限次數")
	@TableField("max_uses")
	private Integer maxUses;

	@Schema(description = "已使用次數")
	@TableField("used_count")
	private Integer usedCount;

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
