package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddSettingDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutSettingDTO;
import tw.com.conference.pojo.VO.SettingVO;
import tw.com.conference.pojo.entity.Setting;

@Mapper(componentModel = "spring")
public interface SettingConvert {

	Setting addDTOToEntity(AddSettingDTO addSettingDTO);

	Setting putDTOToEntity(PutSettingDTO putSettingDTO);
	
	SettingVO entityToVO(Setting setting);
	
	List<SettingVO> entityListToVOList(List<Setting> settingList);
	
}
