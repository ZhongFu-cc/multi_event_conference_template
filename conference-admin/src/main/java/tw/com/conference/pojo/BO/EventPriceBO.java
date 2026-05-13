package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventPriceBO {
	private String eventTitle;
	private BigDecimal originalPrice;
	
}
