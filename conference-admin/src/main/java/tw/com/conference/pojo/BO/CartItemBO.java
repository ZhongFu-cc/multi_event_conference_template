package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemBO {
	private Long eventId;
	private BigDecimal originalPrice;
}
