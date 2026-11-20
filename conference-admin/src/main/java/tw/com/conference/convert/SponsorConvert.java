package tw.com.conference.convert;

import java.util.List;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddSponsorDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutSponsorDTO;
import tw.com.conference.pojo.VO.SponsorVO;
import tw.com.conference.pojo.entity.Sponsor;

@Mapper(componentModel = "spring")
public interface SponsorConvert {

	Sponsor addDTOToEntity(AddSponsorDTO addSponsorDTO);

	Sponsor putDTOToEntity(PutSponsorDTO putSponsorDTO);

	SponsorVO entityToVO(Sponsor sponsor);

	List<SponsorVO> entityListToVOList(List<Sponsor> sponsorList);

}
