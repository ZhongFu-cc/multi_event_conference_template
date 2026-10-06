package tw.com.conference.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.Orders;

/**
 * <p>
 * 訂單表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2025-02-05
 */
public interface OrdersMapper extends BaseMapper<Orders> {

	/**
	 * 查詢「有未付款訂單」的會員總數<br>
	 * 以會員為單位，一個會員有幾張未付訂單都只算一人<br>
	 * 原生 SQL 不會套用 @TableLogic，所以要自己帶 is_deleted = 0
	 *
	 * @param paidStatus 付款成功的狀態值 (OrderStatusEnum.PAYMENT_SUCCESS.getValue())
	 * @return
	 */
	@Select("SELECT COUNT(DISTINCT member_id) FROM orders "
			+ "WHERE is_deleted = 0 AND status <> #{paidStatus}")
	Long countUnpaidMembers(@Param("paidStatus") String paidStatus);

}
