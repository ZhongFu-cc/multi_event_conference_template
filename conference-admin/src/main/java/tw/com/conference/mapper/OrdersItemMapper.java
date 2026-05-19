package tw.com.conference.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.OrdersItem;

/**
 * <p>
 * 訂單細項表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2025-02-05
 */
public interface OrdersItemMapper extends BaseMapper<OrdersItem> {

	default List<OrdersItem> selectByOrdersId(Long ordersId){
		LambdaQueryWrapper<OrdersItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(OrdersItem::getOrdersId,ordersId);
		return this.selectList(queryWrapper);
	}
	
}
