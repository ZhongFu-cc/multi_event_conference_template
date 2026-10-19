package tw.com.conference.pojo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoCodeTypeEnum;
import tw.com.conference.enums.PromoDiscountTypeEnum;

/**
 * <p>
 * 優惠碼活動表<br>
 * 一次發放的定義。通用碼也視為一個只有 1 組碼的 campaign
 * </p>
 */
@Getter
@Setter
@TableName("promo_campaign")
@Schema(name = "PromoCampaign", description = "優惠碼活動表")
public class PromoCampaign implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("promo_campaign_id")
	private Long promoCampaignId;

	@Schema(description = "優惠碼活動名稱")
	@TableField("name")
	private String name;

	@Schema(description = "碼型態; general=單一通用碼不限次數, unique=批次產生每組限用一次")
	@TableField("code_type")
	private PromoCodeTypeEnum codeType;

	@Schema(description = "折扣型態; 逐場為 free/amount/percent (看適用活動), 訂單層為 all_amount/all_percent (忽略適用活動)")
	@TableField("discount_type")
	private PromoDiscountTypeEnum discountType;

	@Schema(description = "折扣值; free 為 null, amount/all_amount 為折抵金額, percent/all_percent 為減免百分比")
	@TableField("discount_value")
	private BigDecimal discountValue;

	@Schema(description = "可使用起始日, null 為不限")
	@TableField("date_from")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;

	@Schema(description = "可使用結束日, null 為不限")
	@TableField("date_to")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;

	@Schema(description = "是否啟用;0=否,1=是")
	@TableField("is_active")
	private CommonStatusEnum isActive;

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
