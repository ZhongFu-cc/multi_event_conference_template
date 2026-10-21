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
 * 團體報名優惠規則表<br>
 * 一場活動可設多條階梯 (例: 5人9折、10人8折)，鎖定名單時取「人數符合中折扣最多」的那條
 * </p>
 */
@Getter
@Setter
@TableName("group_discount_rule")
@Schema(name = "GroupDiscountRule", description = "團體報名優惠規則表")
public class GroupDiscountRule implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("group_discount_rule_id")
	private Long groupDiscountRuleId;

	@Schema(description = "活動事件ID")
	@TableField("event_id")
	private Long eventId;

	@Schema(description = "規則名稱; 例: 5人以上9折")
	@TableField("name")
	private String name;

	@Schema(description = "最低人數 (含負責人)")
	@TableField("min_count")
	private Integer minCount;

	@Schema(description = "減免百分比; 10 表示折抵 10% (即 9 折)")
	@TableField("discount_value")
	private Integer discountValue;

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
