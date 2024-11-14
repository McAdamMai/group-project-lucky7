package ca.mcmaster.cas735.acme.parking_management.business;

import ca.mcmaster.cas735.acme.parking_management.model.TransponderInfo;
import ca.mcmaster.cas735.acme.parking_management.repository.TransponderRepository;
import ca.mcmaster.cas735.acme.parking_management.dtos.PaymentRespDto;
import ca.mcmaster.cas735.acme.parking_management.ports.TransponderOperationIF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransponderService implements TransponderOperationIF {
    private final TransponderRepository transponderRepository;

    @Override
    public void createTransponder(PaymentRespDto paymentRespDto) {
        if(paymentRespDto != null) {
            if(paymentRespDto.getPaymentStatus()){
                TransponderInfo transponderInfo = translate2Transponder(paymentRespDto);
                log.info("Creating transponder: {}", paymentRespDto);
                transponderRepository.save(transponderInfo);
            }else {
                log.error("Payment status is false");
            }
        }else{
            log.error("PaymentRespDto is null");
        }
    }

    private TransponderInfo translate2Transponder(PaymentRespDto paymentRespDto) {
        return TransponderInfo.builder()
                .macID(paymentRespDto.getMacID())
                .licensePlate(paymentRespDto.getLicensePlate())
                .registerTime(paymentRespDto.getTimeStamp())
                .expireTime(paymentRespDto.getTimeStamp())
                .build();
    }
}
