package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddCheckinRecordDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutCheckinRecordDTO;
import tw.com.conference.pojo.VO.CheckinRecordVO;
import tw.com.conference.pojo.entity.CheckinRecord;
import tw.com.conference.pojo.excelPojo.AttendeesExcel;
import tw.com.conference.pojo.excelPojo.CheckinRecordExcel;

@Mapper(componentModel = "spring")
public interface CheckinRecordConvert {

	CheckinRecord addDTOToEntity(AddCheckinRecordDTO addCheckinRecordDTO);

	CheckinRecord putDTOToEntity(PutCheckinRecordDTO putCheckinRecordDTO);

	CheckinRecordVO entityToVO(CheckinRecord checkinRecord);

	List<CheckinRecordVO> entityListToVOList(List<CheckinRecord> checkinRecordList);

	CheckinRecordExcel attendeesExcelToCheckinRecordExcel(AttendeesExcel attendeesExcel);
	
}
