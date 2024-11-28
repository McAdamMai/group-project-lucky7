package ca.mcmaster.cas735.acme.parking_management.business;

import ca.mcmaster.cas735.acme.parking_management.dtos.*;
import ca.mcmaster.cas735.acme.parking_management.model.TransponderInfo;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2MacSystemIF;
import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;
import ca.mcmaster.cas735.acme.parking_management.repository.TransponderRepository;
import ca.mcmaster.cas735.acme.parking_management.utils.TransponderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransponderService implements OrderProcessorIF {
    
    private final TransponderRepository transponderRepository;
    private final PaymentIF paymentIF;
    private final Management2MacSystemIF management2MacSystemIF;
    
    @Override
    @Transactional
    public OrderResDto processRegistration(OrderReqDto orderReqDto){
        TransponderInfo tInfo = translate2Transponder(orderReqDto);
        // if ID repeats
        if (transponderRepository.existsByOrderID(tInfo.getOrderID())) {
            return new OrderResDto(tInfo.getOrderID(), true, true);
        }
        if (transponderRepository.existsByMacID(tInfo.getMacID())) {
            return new OrderResDto(tInfo.getMacID(), false, true);
        }
        transponderRepository.save(tInfo);
        // only false & false will trigger a new registration
        Management2PaymentDto management2PaymentDto = translate2PaymentDto(orderReqDto);
        paymentIF.sendToPayment(management2PaymentDto);
        return new OrderResDto(tInfo.getOrderID(), false, false);
    }

    @Override
    @Transactional
    public OrderResDto processRenewal(OrderReqDto orderReqDto) {
        TransponderInfo tInfo = translate2Transponder(orderReqDto);
        if (transponderRepository.existsByOrderID(tInfo.getOrderID())) {
            return new OrderResDto(tInfo.getOrderID(), true, true);
        }
        // no existed transponder need a registration first
        if (!transponderRepository.existsByMacID(tInfo.getMacID())) {
            return new OrderResDto(tInfo.getOrderID(), false, false);
        }
        TransponderInfo existedTInfo = transponderRepository.findByMacID(tInfo.getMacID());
        // occur a payment pending renewal or register order, return a non-existed code to client
        if (existedTInfo.getExpireTime() == -1L){
            return new OrderResDto(tInfo.getOrderID(), true, false);
        }
        tInfo.setRegisterTime(tInfo.getRegisterTime() < existedTInfo.getExpireTime() ?
                existedTInfo.getExpireTime() : System.currentTimeMillis());
        transponderRepository.updateTransponderRegisterTime(tInfo.getMacID(), tInfo.getRegisterTime());
        Management2PaymentDto management2PaymentDto = translate2PaymentDto(orderReqDto);
        paymentIF.sendToPayment(management2PaymentDto);
        return new OrderResDto(tInfo.getOrderID(), false, true);
    }

    @Override
    @Transactional
    public OrderResDto processDeletion(OrderReqDto orderReqDto) {
        TransponderInfo tInfo = translate2Transponder(orderReqDto);
        if (transponderRepository.existsByOrderID(tInfo.getOrderID())) {
            transponderRepository.deleteByOrderID(tInfo.getOrderID());
            return new OrderResDto(tInfo.getOrderID(), true, true);
        }else{
            return new OrderResDto(tInfo.getOrderID(), false, false);
        }
    }

    @Override
    public void processPayment(Payment2ManagementDto payment2ManagementDto) {
        TransponderInfo tInfo = transponderRepository.findByMacID(payment2ManagementDto.getMacID());
        if(tInfo != null){
            TransponderType transponderType = tInfo.getTransponderType();
            Long expireTime = tInfo.getRegisterTime() + transponderType.getNumberOfMonths() * 2678400L;
            // TODO: improve time calculation
            transponderRepository.updateTransponderExpiryTime(tInfo.getMacID(), expireTime);
            Management2MacDto management2MacDto = translate2MacDto(tInfo);
            management2MacSystemIF.updateTransponder(management2MacDto);
        }else{
            log.error("Order not found!!!");
        }
    }

    private TransponderInfo translate2Transponder(OrderReqDto orderReqDto) {
        return TransponderInfo.builder()
                .macID(orderReqDto.getMacID())
                .transponderID(orderReqDto.getTransponderID())
                .transponderType(orderReqDto.getTransponderType())
                .licensePlate(orderReqDto.getLicensePlate())
                .registerTime(orderReqDto.getTimeStamp())
                .expireTime(-1L)
                .build();
    }
    private Management2PaymentDto translate2PaymentDto(OrderReqDto orderReqDto) {
        Management2PaymentDto management2PaymentDto = new Management2PaymentDto();
        management2PaymentDto.setMacID(orderReqDto.getMacID());
        management2PaymentDto.setTimestamp(orderReqDto.getTimeStamp());
        management2PaymentDto.setPaymentMethod(orderReqDto.getPaymentMethod());
        management2PaymentDto.setBill(10000); //TODO: build a hash for price
        return management2PaymentDto;
    }
    private Management2MacDto translate2MacDto(TransponderInfo tInfo){
        Management2MacDto management2MacDto = new Management2MacDto();
        management2MacDto.setMacID(tInfo.getMacID());
        management2MacDto.setTransponderID(tInfo.getTransponderID());
        management2MacDto.setLicensePlate(tInfo.getLicensePlate());
        management2MacDto.setTimeStamp(tInfo.getExpireTime());
        return management2MacDto;
    }
}
