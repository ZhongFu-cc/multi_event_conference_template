package tw.com.conference.pojo.VO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoCodeTypeEnum;
import tw.com.conference.enums.PromoDiscountTypeEnum;

/**
 * 優惠碼活動 (含碼的使用統計與適用活動)
 */
@Data
public class PromoCampaignVO {

	@Schema(description = "主鍵ID")
	private Long promoCampaignId;

	@Schema(description = "優惠碼活動名稱")
	private String name;

	@Schema(description = "碼型態")
	private PromoCodeTypeEnum codeType;

	@Schema(description = "折扣型態")
	private PromoDiscountTypeEnum discountType;

	@Schema(description = "折扣型態說明")
	private String discountTypeLabel;

	@Schema(description = "折扣值")
	private BigDecimal discountValue;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "可使用起始日, null 為不限")
	private LocalDate dateFrom;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "可使用結束日, null 為不限")
	private LocalDate dateTo;

	@Schema(description = "是否啟用")
	private CommonStatusEnum isActive;

	@Schema(description = "碼總組數")
	private Integer totalCodeCount;

	@Schema(description = "已使用次數總和")
	private Integer usedCount;

	@Schema(description = "尚可使用的組數; 通用碼(不限次數)為 null")
	private Integer remainingCodeCount;

	@Schema(description = "適用的活動事件IDs; 空列表代表全場適用。訂單層態(all_amount/all_percent)不使用此欄位")
	private List<Long> eventIds;

	@Schema(description = "適用的活動標題; 與 eventIds 對應")
	private List<String> eventTitles;

}
