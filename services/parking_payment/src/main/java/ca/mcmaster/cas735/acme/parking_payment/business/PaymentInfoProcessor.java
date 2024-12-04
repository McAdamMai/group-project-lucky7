package ca.mcmaster.cas735.acme.parking_payment.business;

import ca.mcmaster.cas735.acme.parking_payment.dto.*;
import ca.mcmaster.cas735.acme.parking_payment.model.PaymentInfo;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentConfirmation2GateMsgBusIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentConfirmation2ManagementIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentRequest2BankIF;
import ca.mcmaster.cas735.acme.parking_payment.repository.PaymentInfoRepository;
import ca.mcmaster.cas735.acme.parking_payment.utils.PaymentStatus;
import ca.mcmaster.cas735.acme.parking_payment.utils.ProductName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class    PaymentInfoProcessor implements ProcessPaymentInfo {

    private final PaymentRequest2BankIF paymentRequest2BankIF;
    private final PaymentInfoRepository paymentInfoRepository;
    private final PaymentConfirmation2GateMsgBusIF paymentConfirmation2GateMsgBusIF;
    private final PaymentConfirmation2ManagementIF paymentConfirmation2ManagementIF;

    //send to bank, store in db, response to gate
    @Override
    public void processPaymentFromGate(Gate2PaymentDto gate2PaymentDto){
        String paymentID = generatePaymentID(gate2PaymentDto.getLicensePlate(), gate2PaymentDto.getTimeStamp());
        //store in db
        PaymentInfo paymentInfo = buildParkingPaymentInfo(gate2PaymentDto, paymentID);
        log.info("Payment information stored {} {}", paymentInfo.getPaymentStatus(), paymentInfo.getPaymentStatus().getClass());
        paymentInfoRepository.save(paymentInfo);
        //send back payment ID to the pos
        PaymentID2PosDto paymentID2PosDto = new PaymentID2PosDto();
        // need to know specific pos attached to the physical gate
        paymentID2PosDto.setDeviceID(gate2PaymentDto.getGateID());
        paymentID2PosDto.setBill(paymentInfo.getBill());
        paymentConfirmation2GateMsgBusIF.sendPaymentIDPos(paymentID2PosDto);
        //send to bank, simulate a successfully made payment
        log.info("Payment request sent to the bank");
        paymentRequest2BankIF.sendPaymentRequest(paymentID, gate2PaymentDto.getBill());
        log.info("PaymentID sent back to the gate");
    }

    @Override
    public void processPaymentFromManagement(Management2PaymentDto management2PaymentDto){
        String paymentID = generatePaymentID(management2PaymentDto.getMacID(), management2PaymentDto.getTimestamp());
        //send to bank, simulate a successfully made payment
        //store in db
        PaymentInfo paymentInfo = buildTransponderPaymentInfo(management2PaymentDto, paymentID);
        paymentInfoRepository.save(paymentInfo);
        log.info("Payment information stored");
        paymentRequest2BankIF.sendPaymentRequest(paymentID, management2PaymentDto.getBill());
        log.info("Payment request sent to the bank");
    }

    @Override
    @Transactional
    public void processConfirmationFromBank(Bank2PaymentDto bank2PaymentDto){
        int status = bank2PaymentDto.getAck()? PaymentStatus.Success : PaymentStatus.Failed;
        paymentInfoRepository.updatePaymentStatus(bank2PaymentDto.getPaymentID(), status);
        PaymentInfo paymentInfo = paymentInfoRepository.findByPaymentId(bank2PaymentDto.getPaymentID());
        if(paymentInfo!=null){
            if(Objects.equals(paymentInfo.getProductName(), ProductName.Parking)){
                PaymentConfirmation2GateDto paymentConfirmation2GateDto = new PaymentConfirmation2GateDto();
                paymentConfirmation2GateDto.
                        setGateId(paymentInfo.getGateId());
                paymentConfirmation2GateDto
                        .setPaymentStatus(paymentInfo.getPaymentStatus());
                paymentConfirmation2GateDto
                        .setLicensePlate(paymentInfo.getProductId()); // parking service id is license plate
                paymentConfirmation2GateMsgBusIF.sendConfirmationToGate(paymentConfirmation2GateDto);
            }else if(Objects.equals(paymentInfo.getProductName(), ProductName.Transponder)){
                PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto = new PaymentConfirmation2ManagementDto();
                paymentConfirmation2ManagementDto
                        .setMacID(paymentInfo.getProductId()); // transponder id is index
                paymentConfirmation2ManagementDto
                        .setPaymentStatus(paymentInfo.getPaymentStatus());
                paymentConfirmation2ManagementIF.sendConfirmationToManager(paymentConfirmation2ManagementDto);
            }
        }else{
            log.error("Payment ID not found");
        }
    }

    // build a buildPaymentInfo model
    private  PaymentInfo buildTransponderPaymentInfo(Management2PaymentDto management2PaymentDto, String paymentID){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentId(paymentID);
        paymentInfo.setProductName(ProductName.Transponder);
        paymentInfo.setProductId(management2PaymentDto.getMacID());
        paymentInfo.setBill(management2PaymentDto.getBill());
        paymentInfo.setPaymentStatus(PaymentStatus.Processing);
        paymentInfo.setGateId("");
        return paymentInfo;
    }

    private  PaymentInfo buildParkingPaymentInfo(Gate2PaymentDto gate2PaymentDto, String paymentID){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentId(paymentID);
        paymentInfo.setProductName(ProductName.Parking);
        paymentInfo.setProductId(gate2PaymentDto.getLicensePlate());
        paymentInfo.setBill(gate2PaymentDto.getBill());
        paymentInfo.setPaymentStatus(PaymentStatus.Processing);
        paymentInfo.setGateId(gate2PaymentDto.getGateID());
        return paymentInfo;
    }

    // get paymentID using UUID
    private String generatePaymentID(String ID, Long timestamp){
        String combined = ID + timestamp;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(combined.getBytes());
            // convert to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash){
                String hex = Integer.toHexString(0xff & b); // signed to unsigned
                if (hex.length() == 1) hexString.append('0'); // 0-15 extend to 2-digits hexadecimal
                hexString.append(hex);
            }
            // Get the first 16 or 20 characters (hexadecimal representation)
            String shortUUID = hexString.toString().substring(0, 20);
            //String shortUUID = hexString.toString();
            log.info("hex:{}, UUID: {}, combined: {}", hexString, shortUUID, combined);
            return shortUUID;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
