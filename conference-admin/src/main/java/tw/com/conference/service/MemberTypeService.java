package tw.com.conference.service;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberTypeDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutMemberTypeDTO;
import tw.com.conference.pojo.entity.MemberType;

/**
 * <p>
 * 會員身份列表 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface MemberTypeService extends IService<MemberType> {

	/**
	 * 判斷是否有任何會員身份類別
	 * @return
	 */
	boolean existAny();

	/**
	 * 獲取單一會員身份類別
	 * @param memberTypeId
	 * @return
	 */
	MemberType get(Long memberTypeId);

	/**
	 * 新增會員身份類別
	 * @param addMemberTypeDTO
	 */
	MemberType create(AddMemberTypeDTO addMemberTypeDTO);

	/**
	 * 修改會員身份類別
	 * @param putMemberTypeDTO
	 */
	void update(PutMemberTypeDTO putMemberTypeDTO);

	/**
	 * 刪除會員身份類別
	 * @param memberTypeId
	 */
	void remove(Long memberTypeId);
	
}
