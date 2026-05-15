package tw.com.conference.pojo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.pojo.BO.AppliedDiscountBO;

/**
 * <p>
 * 訂單表
 * </p>
 *
 * @author Joey
 * @since 2025-02-05
 */
@Getter
@Setter
@TableName(value = "orders",autoResultMap = true)
@Schema(name = "Orders", description = "訂單表")
public class Orders implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("orders_id")
	private Long ordersId;

	@Schema(description = "會員ID")
	@TableField("member_id")
	private Long memberId;

	@Schema(description = "這筆訂單商品的統稱- 因為綠界沒有辦法顯示 明細 對應 細項金額,只能統整Total金額")
	@TableField("items_summary")
	private String itemsSummary;

	@Schema(description = "原價訂單價格總額")
	@TableField("original_total_amount")
	private BigDecimal originalTotalAmount;
	
	@Schema(description = "總折扣金額")
	@TableField("total_discount_amount")
	private BigDecimal totalDiscountAmount;
	
	@Schema(description = "訂單總金額")
	@TableField("total_amount")
	private BigDecimal totalAmount;
	
	@Schema(description = "應用的折扣明細")
	@TableField(value = "applied_discounts",typeHandler = JacksonTypeHandler.class)
	private List<AppliedDiscountBO> appliedDiscounts ;

	@Schema(description = "訂單狀態: 未付款,已付款-待審核,付款成功,付款失敗")
	@TableField("status")
	private OrderStatusEnum status;

	@Schema(description = "創建者")
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private String createBy;

	@Schema(description = "創建時間")
	@TableField(value = "create_date", fill = FieldFill.INSERT)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createDate;

	@Schema(description = "最後修改者")
	@TableField(value = "update_by", fill = FieldFill.UPDATE)
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
