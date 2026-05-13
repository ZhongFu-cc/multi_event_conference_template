package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppliedDiscountBO {

	private Long packageId; // 哪一個組合包
	private String packageName; // 組合包名稱
	private BigDecimal discount; // 折扣金額
}
