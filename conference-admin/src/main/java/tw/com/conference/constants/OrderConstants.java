package tw.com.conference.constants;

public final class OrderConstants {
	
	// 私有化構造函數,禁止被 new
	private OrderConstants() {}

	/** 訂單細項的產品類型 - 活動報名 */
	public static final String PRODUCT_TYPE_EVENT = "event";

	/** 訂單備註 - 後台人工審核通過 */
	public static final String REMARK_MANUAL_APPROVED = "手動審核";

	/** 訂單備註 - 後台建立的免費訂單 (後台新增會員 / 現場報到) */
	public static final String REMARK_FREE_BY_ADMIN = "後台免費建立";
}
