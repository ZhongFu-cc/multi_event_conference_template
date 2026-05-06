package tw.com.conference.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.MemberTypeConvert;
import tw.com.conference.mapper.MemberTypeMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberTypeDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutMemberTypeDTO;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.service.MemberTypeService;

/**
 * <p>
 * 會員身份列表 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Service
@RequiredArgsConstructor
public class MemberTypeServiceImpl extends ServiceImpl<MemberTypeMapper, MemberType> implements MemberTypeService {

	private final MemberTypeConvert memberTypeConvert;

	@Override
	public boolean existAny() {
		return baseMapper.existAnyMemberType();
	}

	@Override
	public MemberType get(Long memberTypeId) {
		MemberType memberType = baseMapper.selectById(memberTypeId);
		return memberType;
	}

	@Override
	public List<MemberType> list() {
		return baseMapper.selectList(null);
	}

	@Override
	public MemberType create(AddMemberTypeDTO addMemberTypeDTO) {
		MemberType memberType = memberTypeConvert.addDTOToEntity(addMemberTypeDTO);
		baseMapper.insert(memberType);
		return memberType;
	}

	@Override
	public void update(PutMemberTypeDTO putMemberTypeDTO) {
		MemberType memberType = memberTypeConvert.putDTOToEntity(putMemberTypeDTO);
		baseMapper.updateById(memberType);
	}

	@Override
	public void remove(Long memberTypeId) {
		baseMapper.deleteById(memberTypeId);
	}

}
