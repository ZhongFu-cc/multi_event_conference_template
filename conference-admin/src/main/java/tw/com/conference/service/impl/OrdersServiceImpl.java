package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.OrdersConvert;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.mapper.OrdersMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersDTO;
import tw.com.conference.pojo.entity.Orders;
import tw.com.conference.service.OrdersItemService;
import tw.com.conference.service.OrdersService;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

	private final OrdersConvert ordersConvert;
	private final OrdersItemService ordersItemService;

	@Override
	public List<Orders> findOrdersByMemberIdsAndStatus(Collection<Long> memberIds,
			Collection<OrderStatusEnum> statuses) {
		if (memberIds == null || memberIds.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(Orders::getMemberId, memberIds)
				.in(statuses != null && !statuses.isEmpty(), Orders::getStatus, statuses);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public List<Orders> findPaidOrdersByMemberId(Long memberId) {
		LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(Orders::getMemberId, memberId).eq(Orders::getStatus, OrderStatusEnum.PAYMENT_SUCCESS);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public boolean hasUnpaidOrders(Long memberId) {
		LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(Orders::getMemberId, memberId).ne(Orders::getStatus, OrderStatusEnum.PAYMENT_SUCCESS);
		return baseMapper.selectCount(queryWrapper) > 0;
	}

	@Override
	public int getUnpaidMemberGroupIndex(int groupSize) {
		// 與 hasUnpaidOrders 同一判斷基準：status != PAYMENT_SUCCESS 即視為未付清
		Long unpaidMemberCount = baseMapper.countUnpaidMembers(OrderStatusEnum.PAYMENT_SUCCESS.getValue());
		return (int) Math.ceil(unpaidMemberCount / (double) groupSize);
	}

	@Override
	public Orders getOrders(Long ordersId) {
		Orders orders = baseMapper.selectById(ordersId);
		return orders;
	}

	@Override
	public Orders getOrders(Long memberId, Long ordersId) {
		LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
		ordersQueryWrapper.eq(Orders::getMemberId, memberId).eq(Orders::getOrdersId, ordersId);

		Orders orders = baseMapper.selectOne(ordersQueryWrapper);

		return orders;
	}

	@Override
	public List<Orders> getOrdersList() {
		return baseMapper.selectList(null);
	}

	@Override
	public List<Orders> getOrdersList(Long memberId) {
		LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
		ordersQueryWrapper.eq(Orders::getMemberId, memberId);
		return baseMapper.selectList(ordersQueryWrapper);
	}

	@Override
	public IPage<Orders> getOrdersPage(Page<Orders> page) {
		Page<Orders> ordersPage = baseMapper.selectPage(page, null);
		return ordersPage;
	}

	@Override
	@Transactional
	public Long addOrder(AddOrderDTO addOrderDTO) {
		// 新增訂單本身
		Orders order = ordersConvert.addDTOToEntity(addOrderDTO);
		baseMapper.insert(order);

		return order.getOrdersId();
	}

	@Override
	public void updateOrders(PutOrdersDTO putOrdersDTO) {
		Orders orders = ordersConvert.putDTOToEntity(putOrdersDTO);
		baseMapper.updateById(orders);
	}

	@Override
	public void updateOrders(Long memberId, PutOrdersDTO putOrdersDTO) {
		Orders orders = ordersConvert.putDTOToEntity(putOrdersDTO);

		LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
		ordersQueryWrapper.eq(Orders::getMemberId, memberId).eq(Orders::getOrdersId, orders.getOrdersId());
		baseMapper.update(orders, ordersQueryWrapper);
	}

	@Override
	public void deleteOrders(Long ordersId) {
		// 1.刪除訂單的細項
		ordersItemService.deleteOrdersItemByOrderId(ordersId);
		// 2.刪除訂單
		baseMapper.deleteById(ordersId);
	}

	@Override
	public void deleteOrders(Long memberId, Long ordersId) {
		// 1.查詢memberId 和 orderId符合的訂單
		LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
		ordersQueryWrapper.eq(Orders::getMemberId, memberId).eq(Orders::getOrdersId, ordersId);
		Orders order = baseMapper.selectOne(ordersQueryWrapper);

		// 2.刪除訂單及其細項
		this.deleteOrders(order.getOrdersId());

	}

	@Override
	public void deleteOrdersList(List<Long> ordersIds) {
		for (Long orderId : ordersIds) {
			this.deleteOrders(orderId);
		}
	}

}
