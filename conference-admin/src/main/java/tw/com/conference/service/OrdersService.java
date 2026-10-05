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

	/**
	 * 會員是否還有尚未付款成功的訂單<br>
	 * (未付款、付款-待確認、付款失敗 皆視為尚未付清)
	 *
	 * @param memberId
	 * @return
	 */
	boolean hasUnpaidOrders(Long memberId);

	/**
	 * 拿到「有未付款訂單的會員」群體的 index<br>
	 * 用於寄信批次分組 (寄信對象是會員，所以以會員數分組，非訂單數)
	 *
	 * @param groupSize 一組的數量(人數)
	 * @return
	 */
	int getUnpaidMemberGroupIndex(int groupSize);

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
