package tw.com.conference.pojo.VO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.OrderStatusEnum;

@Data
public class OrdersVO {

	@Schema(description = "主鍵ID")
	private Long ordersId;
	
	@Schema(description = "自然鍵")
	private byte[] natureId;
	
	@Schema(description = "訂單總金額")
	private BigDecimal totalAmount;

	@Schema(description = "訂單狀態: 未付款,已付款-待審核,付款成功,付款失敗")
	private OrderStatusEnum status;

}
