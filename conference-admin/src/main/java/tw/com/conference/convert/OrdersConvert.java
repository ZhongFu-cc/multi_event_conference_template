package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersDTO;
import tw.com.conference.pojo.VO.OrdersVO;
import tw.com.conference.pojo.entity.Orders;

@Mapper(componentModel = "spring")
public interface OrdersConvert {

	Orders addDTOToEntity(AddOrderDTO addOrderDTO);

	Orders putDTOToEntity(PutOrdersDTO putOrdersDTO);
	
	OrdersVO entityToVO(Orders orders);
	
	List<OrdersVO> entityListToVOList(List<Orders> ordersList);
	
}
