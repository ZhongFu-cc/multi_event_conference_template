package tw.com.conference.pojo.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 優惠碼活動 - 適用活動事件<br>
 * 僅逐場型態 (free/amount/percent) 參考此表; 訂單層態 (all_amount/all_percent) 忽略
 * </p>
 */
@Getter
@Setter
@TableName("promo_code_event")
@Schema(name = "PromoCodeEvent", description = "優惠碼活動-適用活動事件表")
public class PromoCodeEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("promo_code_event_id")
	private Long promoCodeEventId;

	@Schema(description = "優惠碼活動ID")
	@TableField("promo_campaign_id")
	private Long promoCampaignId;

	@Schema(description = "活動事件ID")
	@TableField("event_id")
	private Long eventId;
}
