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
 * 與會者 與 標籤 的關聯表
 * </p>
 *
 * @author Joey
 * @since 2025-05-14
 */
@Getter
@Setter
@TableName("attendee_tag")
@Schema(name = "AttendeeTag", description = "與會者 與 標籤 的關聯表")
public class AttendeeTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵ID")
    @TableId("attendee_tag_id")
    private Long attendeeTagId;

    @Schema(description = "與會者ID")
    @TableField("attendee_id")
    private Long attendeeId;

    @Schema(description = "標籤ID")
    @TableField("tag_id")
    private Long tagId;
}
