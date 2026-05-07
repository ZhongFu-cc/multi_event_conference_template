package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageDTO;
import tw.com.conference.pojo.entity.DiscountPackage;

@Mapper(componentModel = "spring")
public interface DiscountPackageConvert {

	DiscountPackage addDTOToEntity(AddDiscountPackageDTO addDiscountPackageDTO);

	DiscountPackage putDTOToEntity(PutDiscountPackageDTO putDiscountPackageDTO);
	

}
