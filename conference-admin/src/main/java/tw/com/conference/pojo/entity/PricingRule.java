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
import tw.com.conference.enums.NationalityEnum;

/**
 * <p>
 * 價格規則表
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Getter
@Setter
@TableName("pricing_rule")
@Schema(name = "PricingRule", description = "價格規則表")
public class PricingRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵ID")
    @TableId("pricing_rule_id")
    private Long pricingRuleId;

    @Schema(description = "規則別名；例:早鳥-本國-會員")
    @TableField("alias")
    private String alias;

    @Schema(description = "活動事件ID")
    @TableField("event_id")
    private Long eventId;

    @Schema(description = "會員身份ID，NULL = 適用所有身分")
    @TableField("member_type_id")
    private Long memberTypeId;

    @Schema(description = "國籍，影響價格維度之一")
    @TableField("nationality")
    private NationalityEnum nationality;

    @Schema(description = "價格適用起始日")
    @TableField("date_from")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @Schema(description = "優惠適用結束日")
    @TableField("date_to")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    @Schema(description = "金額")
    @TableField("amount")
    private BigDecimal amount;

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
