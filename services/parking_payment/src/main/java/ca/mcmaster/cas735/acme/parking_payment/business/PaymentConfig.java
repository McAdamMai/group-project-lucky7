package ca.mcmaster.cas735.acme.parking_payment.business;

import ca.mcmaster.cas735.acme.parking_payment.dto.Payment2AvailDTO;
import ca.mcmaster.cas735.acme.parking_payment.ports.Payment2Avl;
import ca.mcmaster.cas735.acme.parking_payment.repository.PaymentInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConfig {
    private final Payment2Avl payment2AvlIF;
    private final PaymentInfoRepository paymentInfoRepository;

    @PostConstruct
    void init() {
       payment2AvlIF.send2avl(new Payment2AvailDTO(paymentInfoRepository.countTransponderSales(),
                paymentInfoRepository.SumSales(),
                paymentInfoRepository.SumTransponderSales(),
                paymentInfoRepository.SumParkingSales()));
    }

}
