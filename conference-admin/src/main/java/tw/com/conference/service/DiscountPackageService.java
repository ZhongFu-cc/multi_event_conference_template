package tw.com.conference.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageDTO;
import tw.com.conference.pojo.entity.DiscountPackage;

/**
 * <p>
 * 活動優惠組合表 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
public interface DiscountPackageService extends IService<DiscountPackage> {

	/**
	 * 獲取優惠組合
	 * 
	 * @param discountPackageId
	 * @return
	 */
	DiscountPackage get(Long discountPackageId);

	/**
	 * 獲取目前所有的優惠組合,架構設計上不會有很多item
	 */
	List<DiscountPackage> list();

	/**
	 * 創建優惠組合
	 * 
	 * @param addDiscountPackageDTO
	 * @return
	 */
	DiscountPackage create(AddDiscountPackageDTO addDiscountPackageDTO);

	/**
	 * 更新優惠組合
	 * 
	 * @param putDiscountPackageDTO
	 */
	void update(PutDiscountPackageDTO putDiscountPackageDTO);

	/**
	 * 刪除優惠組合
	 * 
	 * @param discountPackageId
	 */
	void remove(Long discountPackageId);

}
