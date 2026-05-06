package tw.com.conference.pojo.VO;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PriceDashboardVO {
	// 本國人表格
	private PriceTableVO domesticTable;
	// 外籍人士表格
	private PriceTableVO internationalTable;

	@Data
	public static class PriceTableVO {
		// 橫軸：["5/1~5/30", "6/1~9/30"]
		private List<String> dateRangeHeaders;
		// 縱軸資料
		private List<PriceRowVO> rows;
	}

	@Data
	@AllArgsConstructor
	public static class PriceRowVO {
		// 會員身分名稱：醫師、護理人員...
		private String memberTypeName;
		// 該列對應每個日期的價格清單
		private List<BigDecimal> amounts;
	}
}