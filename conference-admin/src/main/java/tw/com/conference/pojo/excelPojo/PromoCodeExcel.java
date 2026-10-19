package tw.com.conference.pojo.excelPojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

/**
 * 優惠碼清單 Excel<br>
 * 供後台下載後發送給 VIP 客戶分送
 */
@Data
public class PromoCodeExcel {

	@ExcelProperty("優惠碼")
	private String code;

	@ExcelProperty("優惠碼活動")
	private String campaignName;

	@ExcelProperty("折扣內容")
	private String discountDescription;

	@ExcelProperty("使用狀態")
	private String status;

	@ExcelProperty("使用者姓名")
	private String memberName;

	@ExcelProperty("使用者E-Mail")
	private String memberEmail;

	@ExcelProperty("折抵金額")
	private BigDecimal discountAmount;

	@ExcelProperty("訂單ID")
	private String ordersId;

	@ExcelProperty("使用時間")
	private LocalDateTime usedDate;

}
