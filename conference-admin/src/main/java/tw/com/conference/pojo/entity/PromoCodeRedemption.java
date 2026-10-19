package tw.com.conference.pojo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
import tw.com.conference.enums.PromoRedemptionStatusEnum;

/**
 * <p>
 * 優惠碼使用紀錄表<br>
 * ordersId 有 UNIQUE 約束，保證一張訂單只能用一個優惠碼
 * </p>
 */
@Getter
@Setter
@TableName("promo_code_redemption")
@Schema(name = "PromoCodeRedemption", description = "優惠碼使用紀錄表")
public class PromoCodeRedemption implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("promo_code_redemption_id")
	private Long promoCodeRedemptionId;

	@Schema(description = "優惠碼ID")
	@TableField("promo_code_id")
	private Long promoCodeId;

	@Schema(description = "優惠碼活動ID")
	@TableField("promo_campaign_id")
	private Long promoCampaignId;

	@Schema(description = "使用者會員ID")
	@TableField("member_id")
	private Long memberId;

	@Schema(description = "訂單ID")
	@TableField("orders_id")
	private Long ordersId;

	@Schema(description = "本次折抵金額")
	@TableField("discount_amount")
	private BigDecimal discountAmount;

	@Schema(description = "狀態; reserved=已占用待付款, redeemed=已使用")
	@TableField("status")
	private PromoRedemptionStatusEnum status;

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
