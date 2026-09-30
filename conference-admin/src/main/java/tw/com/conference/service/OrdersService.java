package tw.com.conference.service;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersDTO;
import tw.com.conference.pojo.entity.Orders;

public interface OrdersService extends IService<Orders> {

	/**
	 * 查詢 指定會員 且 符合訂單狀態 的訂單
	 *
	 * @param memberIds
	 * @param statuses  null 或 空 為不限
	 * @return
	 */
	List<Orders> findOrdersByMemberIdsAndStatus(Collection<Long> memberIds, Collection<OrderStatusEnum> statuses);

	/**
	 * 查詢會員 已付款成功 的訂單
	 *
	 * @param memberId
	 * @return
	 */
	List<Orders> findPaidOrdersByMemberId(Long memberId);

	Orders getOrders(Long OrdersId);

	Orders getOrders(Long memberId, Long OrdersId);

	List<Orders> getOrdersList();

	List<Orders> getOrdersList(Long memberId);

	IPage<Orders> getOrdersPage(Page<Orders> page);

	Long addOrder(AddOrderDTO addOrderDTO);

	void updateOrders(PutOrdersDTO putOrdersDTO);

	void updateOrders(Long memberId, PutOrdersDTO putOrdersDTO);

	void deleteOrders(Long ordersId);

	void deleteOrders(Long memberId, Long ordersId);

	void deleteOrdersList(List<Long> OrdersIds);

}
