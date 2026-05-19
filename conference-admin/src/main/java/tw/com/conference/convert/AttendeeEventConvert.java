package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddAttendeeEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutAttendeeEventDTO;
import tw.com.conference.pojo.entity.AttendeeEvent;

@Mapper(componentModel = "spring")
public interface AttendeeEventConvert {

	AttendeeEvent addDTOToEntity(AddAttendeeEventDTO addAttendeeEventDTO);

	AttendeeEvent putDTOToEntity(PutAttendeeEventDTO putAttendeeEventDTO);
	

}
