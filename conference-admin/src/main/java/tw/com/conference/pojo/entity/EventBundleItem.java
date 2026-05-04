package tw.com.conference.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 活動組合優惠 - 適用活動場次
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Getter
@Setter
@TableName("event_bundle_item")
@Schema(name = "EventBundleItem", description = "活動組合優惠 - 適用活動場次")
public class EventBundleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵ID")
    @TableId("event_bundle_item_id")
    private Long eventBundleItemId;

    @Schema(description = "組合優惠ID")
    @TableField("event_bundle_id")
    private Long eventBundleId;

    @Schema(description = "活動ID")
    @TableField("event_id")
    private Long eventId;
}
