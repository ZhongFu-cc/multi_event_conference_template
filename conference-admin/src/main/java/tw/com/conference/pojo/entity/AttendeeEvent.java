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
import tw.com.conference.enums.CommonStatusEnum;

/**
 * <p>
 * 與會者-參加活動 表
 * </p>
 *
 * @author Joey
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("attendee_event")
@Schema(name = "AttendeeEvent", description = "與會者-參加活動 表")
public class AttendeeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主鍵")
    @TableId("attendee_event_id")
    private Long attendeeEventId;

    @Schema(description = "與會者ID")
    @TableField("attendee_id")
    private Long attendeeId;
    
    @Schema(description = "會者ID")
    private Long memberId;

    @Schema(description = "活動事件ID")
    @TableField("event_id")
    private Long eventId;

    @Schema(description = "流水序號")
    @TableField("sequence_no")
    private Integer sequenceNo;

    @Schema(description = "收據編號")
    @TableField("receipt_no")
    private String receiptNo;

    @Schema(description = "是否付款;0=否,1=是")
    @TableField("is_paid")
    private CommonStatusEnum isPaid;

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
