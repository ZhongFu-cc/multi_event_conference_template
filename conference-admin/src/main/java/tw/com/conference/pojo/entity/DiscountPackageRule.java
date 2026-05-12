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
import tw.com.conference.enums.DiscountTypeEnum;

/**
 * <p>
 * 活動組合優惠 - 規則
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Getter
@Setter
@TableName("discount_package_rule")
@Schema(name = "DiscountPackageRule", description = "活動組合優惠 - 規則")
public class DiscountPackageRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵ID")
    @TableId("discount_package_rule_id")
    private Long discountPackageRuleId;

    @Schema(description = "組合優惠ID")
    @TableField("discount_package_id")
    private Long discountPackageId;

    @Schema(description = "規則名稱")
    @TableField("name")
    private String name;

    @Schema(description = "折扣類型 ; percent=百分比折扣 amount=固定折抵")
    @TableField("discount_type")
    private DiscountTypeEnum discountType;

    @Schema(description = "折扣值 ; percent 填 15 表示 15%，amount 填折抵金額")
    @TableField("discount_value")
    private Integer discountValue;

    @Schema(description = "從群組中選中幾個event時觸發")
    @TableField("required_count")
    private Integer requiredCount;

    @Schema(description = "優先級,越大優先級越高 ; 同一批event命中多個折扣時，數字大的優先")
    @TableField("priority")
    private Integer priority;

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
    private Byte isDeleted;
}
