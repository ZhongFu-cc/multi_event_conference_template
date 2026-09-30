package tw.com.conference.service;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderItemDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersItemDTO;
import tw.com.conference.pojo.entity.OrdersItem;

public interface OrdersItemService extends IService<OrdersItem> {

	/**
	 * 查詢某活動的所有訂單細項
	 *
	 * @param eventId
	 * @return
	 */
	List<OrdersItem> findOrderItemsByEventId(Long eventId);

	/**
	 * 查詢多張訂單的所有細項
	 *
	 * @param orderIds
	 * @return
	 */
	List<OrdersItem> findOrderItemsByOrderIds(Collection<Long> orderIds);

	OrdersItem getOrdersItem(Long oredersItemId);
	
	List<OrdersItem> getOrdersItemList();
	
	/**
	 * 根據 orderId 拿到訂單細項 
	 * @param orderId
	 * @return
	 */
	List<OrdersItem> findOrderItemsByOrderId(Long orderId);

	IPage<OrdersItem> getOrdersItemPage(Page<OrdersItem> page);

	void addOrderItem(AddOrderItemDTO addOrderItemDTO);

	void updateOrdersItem(PutOrdersItemDTO putOrdersItemDTO);
	
	void deleteOrdersItem(Long oredersItemId);

	void deleteOrdersItemList(List<Long> oredersItemIds);
	
	/**
	 * 根據訂單ID , 刪除其訂單細項
	 * 
	 * @param orderId
	 */
	void deleteOrdersItemByOrderId(Long orderId);

}
