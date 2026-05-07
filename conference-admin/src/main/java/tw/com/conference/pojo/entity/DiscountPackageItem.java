package tw.com.conference.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 活動組合優惠 - 適用活動場次
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Getter
@Setter
@TableName("discount_package_item")
@Schema(name = "DiscountPackageItem", description = "活動組合優惠 - 適用活動場次")
public class DiscountPackageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵ID")
    @TableId("discount_package_item_id")
    private Long discountPackageItemId;

    @Schema(description = "組合優惠ID")
    @TableField("discount_package_id")
    private Long discountPackageId;

    @Schema(description = "活動ID")
    @TableField("event_id")
    private Long eventId;
}
