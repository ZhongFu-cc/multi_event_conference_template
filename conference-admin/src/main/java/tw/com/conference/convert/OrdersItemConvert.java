package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderItemDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersItemDTO;
import tw.com.conference.pojo.VO.OrdersItemVO;
import tw.com.conference.pojo.entity.OrdersItem;

@Mapper(componentModel = "spring")
public interface OrdersItemConvert {

	OrdersItem addDTOToEntity(AddOrderItemDTO addOrderItemDTO);

	OrdersItem putDTOToEntity(PutOrdersItemDTO putOrdersItemDTO);
	
	OrdersItemVO entityToVO(OrdersItem ordersItem);
	
	List<OrdersItemVO> entityListToVOList(List<OrdersItem> ordersItemList);
	
}
