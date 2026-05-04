package tw.com.conference.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 活動事件表
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Getter
@Setter
@TableName("event")
@Schema(name = "Event", description = "活動事件表")
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵ID")
    @TableId("event_id")
    private Long eventId;

    @Schema(description = "父ID , 允許活動內有子活動選項")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "活動主題")
    @TableField("title")
    private String title;

    @Schema(description = "活動描述")
    @TableField("description")
    private String description;

    @Schema(description = "活動開始時間")
    @TableField("start_at")
    private LocalDateTime startAt;

    @Schema(description = "活動結束時間")
    @TableField("end_at")
    private LocalDateTime endAt;

    @Schema(description = "活動地點")
    @TableField("location")
    private String location;

    @Schema(description = "人數限制,預設為0,0為不限人數")
    @TableField("capacity")
    private Integer capacity;

    @Schema(description = "活動啟用狀態,預設為0,啟用")
    @TableField("is_active")
    private Integer isActive;

    @Schema(description = "創建者")
    @TableField("create_by")
    private String createBy;

    @Schema(description = "創建時間")
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    @Schema(description = "最後修改者")
    @TableField("update_by")
    private String updateBy;

    @Schema(description = "最後修改時間")
    @TableField(value = "update_date", fill = FieldFill.UPDATE)
    private LocalDateTime updateDate;

    @Schema(description = "邏輯刪除,預設為0活耀,1為刪除")
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
