package tw.com.conference.pojo.DTO.putEntityDTO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.OrderStatusEnum;

@Data
public class PutOrdersDTO {

	@Schema(description = "主鍵ID")
	private Long ordersId;
	
	@Schema(description = "訂單總金額")
	private BigDecimal totalAmount;

	@Schema(description = "訂單狀態: 未付款,已付款-待審核,付款成功,付款失敗")
	private OrderStatusEnum status;

}
