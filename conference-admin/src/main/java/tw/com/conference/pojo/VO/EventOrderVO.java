package tw.com.conference.pojo.VO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import tw.com.conference.pojo.BO.AppliedDiscountBO;
import tw.com.conference.pojo.BO.EventPriceBO;

@Data
public class EventOrderVO {

	// 原價總和
	private BigDecimal originalTotal;
	// 折扣總額
	private BigDecimal totalDiscount;
	// 實付金額
	private BigDecimal finalPrice;
	
	// 參與的活動 及 價格
	private List<EventPriceBO> eventPrices = new ArrayList<>();
	// 應用的折扣明細
	private List<AppliedDiscountBO> appliedDiscounts ;
}
