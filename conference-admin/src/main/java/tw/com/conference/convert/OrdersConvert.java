package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddOrdersDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutOrdersDTO;
import tw.com.conference.pojo.VO.OrdersVO;
import tw.com.conference.pojo.entity.Orders;

@Mapper(componentModel = "spring")
public interface OrdersConvert {

	Orders addDTOToEntity(AddOrdersDTO addOrdersDTO);

	Orders putDTOToEntity(PutOrdersDTO putOrdersDTO);
	
	OrdersVO entityToVO(Orders orders);
	
	List<OrdersVO> entityListToVOList(List<Orders> ordersList);
	
}
