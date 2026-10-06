package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.OrdersItemConvert;
import tw.com.conference.mapper.OrdersItemMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderItemDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersItemDTO;
import tw.com.conference.pojo.entity.OrdersItem;
import tw.com.conference.service.OrdersItemService;

@Service
@RequiredArgsConstructor
public class OrdersItemServiceImpl extends ServiceImpl<OrdersItemMapper, OrdersItem> implements OrdersItemService {

	private final OrdersItemConvert ordersItemConvert;

	@Override
	public List<OrdersItem> findOrderItemsByEventId(Long eventId) {
		LambdaQueryWrapper<OrdersItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(OrdersItem::getEventId, eventId);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public List<OrdersItem> findOrderItemsByOrderIds(Collection<Long> orderIds) {
		if (orderIds == null || orderIds.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<OrdersItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(OrdersItem::getOrdersId, orderIds);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public OrdersItem getOrdersItem(Long ordersItemId) {
		OrdersItem ordersItem = baseMapper.selectById(ordersItemId);
		return ordersItem;
	}

	@Override
	public List<OrdersItem> getOrdersItemList() {
		List<OrdersItem> ordersItemList = baseMapper.selectList(null);
		return ordersItemList;
	}
	
	@Override
	public List<OrdersItem> findOrderItemsByOrderId(Long orderId) {
		return baseMapper.selectByOrdersId(orderId);
	}

	@Override
	public IPage<OrdersItem> getOrdersItemPage(Page<OrdersItem> page) {
		Page<OrdersItem> ordersItemPage = baseMapper.selectPage(page, null);
		return ordersItemPage;
	}

	@Override
	public void addOrderItem(AddOrderItemDTO addOrderItemDTO) {
		OrdersItem orderItem = ordersItemConvert.addDTOToEntity(addOrderItemDTO);
		baseMapper.insert(orderItem);
	}

	@Override
	public void updateOrdersItem(PutOrdersItemDTO putOrdersItemDTO) {
		OrdersItem ordersItem = ordersItemConvert.putDTOToEntity(putOrdersItemDTO);
		baseMapper.updateById(ordersItem);

	}

	@Override
	public void deleteOrdersItem(Long ordersItemId) {
		baseMapper.deleteById(ordersItemId);
	}

	@Override
	public void deleteOrdersItemList(List<Long> ordersItemIds) {
		baseMapper.deleteBatchIds(ordersItemIds);
	}

	@Override
	public void deleteOrdersItemByOrderId(Long orderId) {
		LambdaQueryWrapper<OrdersItem> ordersItemWrapper = new LambdaQueryWrapper<>();
		ordersItemWrapper.eq(OrdersItem::getOrdersId, orderId);
		baseMapper.delete(ordersItemWrapper);

	}



}
