package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.enums.CommonStatusEnum;
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

	// 開關類欄位 Entity 為 CommonStatusEnum, VO 為 Boolean
	default Boolean map(CommonStatusEnum value) {
		return value != null && value.getBooleanValue();
	}
	
}
