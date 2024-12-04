package ca.mcmaster.cas735.acme.parking_management.business;

import ca.mcmaster.cas735.acme.parking_management.dtos.*;
import ca.mcmaster.cas735.acme.parking_management.model.TransponderInfo;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2GateIF;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2MacSystemIF;
import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;
import ca.mcmaster.cas735.acme.parking_management.repository.TransponderRepository;
import ca.mcmaster.cas735.acme.parking_management.utils.PaymentStatus;
import ca.mcmaster.cas735.acme.parking_management.utils.TransponderType;
import ca.mcmaster.cas735.acme.parking_management.utils.UserStatus;
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
    private final Management2GateIF management2GateIF;

    @Override
    public UserStatus updateUserInfo(String macId){
        log.info("updateUserInfo start");
        if(transponderRepository.existsByMacID(macId)){
            log.info("{}{}",transponderRepository.getExpireTimeByMacId(macId), System.currentTimeMillis());
            if(transponderRepository.getExpireTimeByMacId(macId) > System.currentTimeMillis()){ //expire?
                return UserStatus.Valid;
            }else {
                return UserStatus.Expired;
            }
        }
        else{
            return UserStatus.New_User;
        }
    }
    
    @Override
    @Transactional
    public OrderResDto processRegistration(OrderReqDto orderReqDto){
        log.info("Processing register information {}", orderReqDto.getOrderId());
        // if ID repeats
        if (transponderRepository.existsByMacID(orderReqDto.getMacID())) {
            log.info("Mac user {} has registered a transponder", orderReqDto.getMacID());
            return new OrderResDto(orderReqDto.getTransponderID(), orderReqDto.getOrderId(), false, true);
        }
        if (transponderRepository.existsByOrderID(orderReqDto.getOrderId())) {
            log.info("Order {} has been created", orderReqDto.getOrderId());
            return new OrderResDto(orderReqDto.getTransponderID(), orderReqDto.getOrderId(),true, true);
        }
        TransponderInfo tInfo = translate2Transponder(orderReqDto);
        transponderRepository.save(tInfo);
        // only false & false will trigger a new registration
        Management2PaymentDto management2PaymentDto = translate2PaymentDto(orderReqDto);
        paymentIF.sendToPayment(management2PaymentDto);
        return new OrderResDto(tInfo.getTransponderID(), tInfo.getOrderID(), false, false);
    }

    @Override
    @Transactional
    public OrderResDto processRenewal(OrderReqDto orderReqDto) {

        // no existed transponder need a registration first
        if (!transponderRepository.existsByMacID(orderReqDto.getMacID())) {
            return new OrderResDto(orderReqDto.getTransponderID(), orderReqDto.getOrderId(), false, false);
        }
        if (transponderRepository.existsByOrderID(orderReqDto.getOrderId())) {
            return new OrderResDto(orderReqDto.getTransponderID(), orderReqDto.getOrderId(), true, true);
        }

        // occur a payment pending renewal or register order, return a non-existed code to client
        if (transponderRepository.getExpireTimeByMacId(orderReqDto.getMacID()) == -1L){ // TODO: need to be atomic
            return new OrderResDto(orderReqDto.getTransponderID(), orderReqDto.getOrderId(), true, false);
        }

        if(orderReqDto.getTimeStamp() < transponderRepository.getExpireTimeByMacId(orderReqDto.getMacID())){
            transponderRepository.updateTransponderRegisterTime(orderReqDto.getMacID());
        }else {
            transponderRepository.updateTransponderRegisterTimeEx(orderReqDto.getMacID(), orderReqDto.getTimeStamp());
        }
        transponderRepository.updateTransponderOrderId(orderReqDto.getMacID(), orderReqDto.getOrderId());
        //TODO: add current time to handle a situation where trans expired
        Management2PaymentDto management2PaymentDto = translate2PaymentDto(orderReqDto);
        paymentIF.sendToPayment(management2PaymentDto);
        return new OrderResDto(orderReqDto.getTransponderID(), orderReqDto.getOrderId(), false, true);
    }

    @Override
    @Transactional
    public boolean processDeletion(String orderId) {
        if (transponderRepository.existsByOrderID(orderId) &&
        transponderRepository.getExpireTimeByOrderId(orderId) == -1L) {
            log.info("Found matched unpaid order and deleted");
            transponderRepository.deleteByOrderID(orderId);
            return true;
        }else{
            return false;
        }
    }


    @Override
    @Transactional
    public void processPayment(Payment2ManagementDto payment2ManagementDto) {
        TransponderInfo tInfo = transponderRepository.findByMacID(payment2ManagementDto.getMacID());
        if(tInfo != null){
            if(payment2ManagementDto.getPaymentStatus() == PaymentStatus.Success){
                TransponderType transponderType = tInfo.getTransponderType();
                Long expireTime = tInfo.getRegisterTime() + transponderType.getNumberOfMonths() * 2678400L;
                // TODO: improve time calculation
                log.info("retrieve transponder {}, modify the expired time to {}", tInfo.getMacID(), expireTime);
                transponderRepository.updateTransponderExpiryTime(tInfo.getMacID(), expireTime);
                Management2MacDto management2MacDto = translate2MacDto(tInfo);
                management2MacSystemIF.updateTransponder(management2MacDto);
            }else{
                log.info("Order {} fail to pay", tInfo.getOrderID());
            }
        }else{
            log.error("Order not found!!!");
        }
    }

    @Override
    public void processGateRequest(Gate2PermitReqDto gate2PermitReqDto){
        Permit2GateResDto permit2GateResDto = new Permit2GateResDto();
        permit2GateResDto.setGateId(gate2PermitReqDto.getGateId()); //set the gate
        String transponderId = gate2PermitReqDto.getTransponderId();
        if(transponderRepository.existsByTransponderID(transponderId) &&
                transponderRepository.getExpireTimeByOrderTId(transponderId) > System.currentTimeMillis()){
            permit2GateResDto.setIsVerified(true);
            permit2GateResDto.setLicensePlate(transponderRepository.getLicensePlateByOrderTId(transponderId));
        }else {
            permit2GateResDto.setIsVerified(false);
            permit2GateResDto.setLicensePlate("");
        }
        management2GateIF.update2gate(permit2GateResDto);
    }

    private TransponderInfo translate2Transponder(OrderReqDto orderReqDto) {
        return TransponderInfo.builder()
                .macID(orderReqDto.getMacID())
                .orderID(orderReqDto.getOrderId())
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
