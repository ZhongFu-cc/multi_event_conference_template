package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddScheduleEmailRecordDTO;
import tw.com.conference.pojo.entity.ScheduleEmailRecord;

@Mapper(componentModel = "spring")
public interface ScheduleEmailRecordConvert {

	ScheduleEmailRecord addDTOToEntity(AddScheduleEmailRecordDTO addScheduleEmailRecordDTO);

	ScheduleEmailRecord copyEntity(ScheduleEmailRecord scheduleEmailRecord);
	
}
