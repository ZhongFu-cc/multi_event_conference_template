package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 報名時的單場活動金額明細<br>
 * <br>
 * 走優惠碼的「逐場型態」(free/amount/percent) 時 discount 為該場實際折抵；<br>
 * 走活動組合優惠、或優惠碼的「訂單層態」(all_amount/all_percent) 時 discount 為 0，
 * 折抵只出現在 EventOrderVO.appliedDiscounts，因為那兩種折扣是算在訂單層級、未分攤到單場。<br>
 * <br>
 * 前端要判斷「這場免費」用 payableAmount == 0 && discount > 0 即可，不另設旗標欄位。
 */
@Data
@NoArgsConstructor
public class EventPriceBO {

	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "活動主題")
	private String eventTitle;

	@Schema(description = "原價")
	private BigDecimal originalPrice;

	@Schema(description = "這場的折抵金額; 訂單層級的折扣不會分攤到此欄位, 為 0")
	private BigDecimal discount;

	@Schema(description = "這場應付金額 = originalPrice - discount")
	private BigDecimal payableAmount;

	/**
	 * 建立單場金額明細
	 *
	 * @param eventId       活動事件ID
	 * @param eventTitle    活動主題
	 * @param originalPrice 原價
	 * @param discount      這場的折抵金額 (無則傳 BigDecimal.ZERO)
	 * @return
	 */
	public static EventPriceBO of(Long eventId, String eventTitle, BigDecimal originalPrice, BigDecimal discount) {
		BigDecimal safeDiscount = discount == null ? BigDecimal.ZERO : discount;

		EventPriceBO bo = new EventPriceBO();
		bo.setEventId(eventId);
		bo.setEventTitle(eventTitle);
		bo.setOriginalPrice(originalPrice);
		bo.setDiscount(safeDiscount);
		bo.setPayableAmount(originalPrice.subtract(safeDiscount).max(BigDecimal.ZERO));
		return bo;
	}

}
