package tw.com.conference.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tw.com.conference.convert.PaymentConvert;
import tw.com.conference.mapper.PaymentMapper;
import tw.com.conference.pojo.DTO.ECPayDTO.ECPayResponseDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPaymentDTO;
import tw.com.conference.pojo.entity.Payment;
import tw.com.conference.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

	private final PaymentConvert paymentConvert;

	@Override
	public Payment getPayment(Long paymentId) {
		Payment payment = baseMapper.selectById(paymentId);
		return payment;
	}

	@Override
	public List<Payment> getPaymentList() {
		List<Payment> paymentList = baseMapper.selectList(null);
		return paymentList;
	}

	@Override
	public IPage<Payment> getPaymentPage(Page<Payment> page) {
		Page<Payment> paymentPage = baseMapper.selectPage(page, null);
		return paymentPage;
	}

	@Override
	public void updatePayment(PutPaymentDTO putPaymentDTO) {
		Payment payment = paymentConvert.putDTOToEntity(putPaymentDTO);
		baseMapper.updateById(payment);

	}

	@Override
	public void deletePayment(Long paymentId) {
		baseMapper.deleteById(paymentId);

	}

	@Override
	public void deletePaymentList(List<Long> paymentIds) {
		baseMapper.deleteBatchIds(paymentIds);
	}

	@Override
	public Payment addPayment(ECPayResponseDTO ECPayResponseDTO) {
		// 1.轉換綠界金流official Data 轉換 自己這邊的Entity
		Payment payment = paymentConvert.officialDataToEntity(ECPayResponseDTO);
		log.info("綠界金流回傳付款確認後資料：　" + payment);

		// 2.新增響應回來的交易紀錄
		baseMapper.insert(payment);

		return payment;
	}

}
