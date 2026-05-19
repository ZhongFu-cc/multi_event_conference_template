package tw.com.conference.pojo.BO;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CalculateResultBO {

	private BigDecimal originalTotal; // 原價總和
	private BigDecimal totalDiscount; // 折扣總額
	private BigDecimal finalPrice; // 實付金額
	private List<AppliedDiscountBO> appliedDiscounts; // 應用的折扣明細
}
