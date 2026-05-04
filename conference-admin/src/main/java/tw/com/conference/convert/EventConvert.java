package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutEventDTO;
import tw.com.conference.pojo.entity.Event;

@Mapper(componentModel = "spring")
public interface EventConvert {

	Event addDTOToEntity(AddEventDTO addEventDTO);

	Event putDTOToEntity(PutEventDTO putEventDTO);
	

}
