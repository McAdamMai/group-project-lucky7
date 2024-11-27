package ca.mcmaster.cas735.acme.parking_payment.business;

import ca.mcmaster.cas735.acme.parking_payment.dto.*;
import ca.mcmaster.cas735.acme.parking_payment.model.PaymentInfo;
import ca.mcmaster.cas735.acme.parking_payment.ports.Payment2MacSystemIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentConfirmation2GateMsgBusIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentRequest2BankIF;
import ca.mcmaster.cas735.acme.parking_payment.repository.PaymentInfoRepository;
import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfOrder;
import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentInfoProcessor implements ProcessPaymentInfo {

    private final PaymentRequest2BankIF paymentRequest2BankIF;
    private final PaymentInfoRepository paymentInfoRepository;
    private final PaymentConfirmation2GateMsgBusIF paymentConfirmation2GateMsgBusIF;
    private final Payment2MacSystemIF payment2MacSystemIF;

    //send to bank, store in db, response to gate
    @Override
    public void processPaymentFromGate(Gate2PaymentDto gate2PaymentDto){
        String paymentID = generatePaymentID(gate2PaymentDto.getLicensePlate(), gate2PaymentDto.getTimeStamp());
        //send to bank, simulate a successfully made payment
        paymentRequest2BankIF.sendPaymentRequest(paymentID, gate2PaymentDto.getBill());
        log.info("Payment request sent to the bank");
        //store in db
        PaymentInfo paymentInfo = buildParkingPaymentInfo(gate2PaymentDto, paymentID);
        paymentInfoRepository.save(paymentInfo);
        log.info("Payment information stored");
        //send back payment ID to the pos
        PaymentID2PosDto paymentID2PosDto = new PaymentID2PosDto();
        paymentID2PosDto.setPaymentID(paymentID);
        // need to know specific pos attached to the physical gate
        paymentID2PosDto.setGateID(gate2PaymentDto.getGateID());
        paymentConfirmation2GateMsgBusIF.sendPaymentIDPos(paymentID2PosDto);
        log.info("PaymentID sent back to the gate");
    }

    @Override
    public void processPaymentFromManagement(Management2PaymentDto management2PaymentDto){
        String paymentID = generatePaymentID(management2PaymentDto.getMacID(), management2PaymentDto.getTimestamp());
        //send to bank, simulate a successfully made payment
        paymentRequest2BankIF.sendPaymentRequest(paymentID, management2PaymentDto.getBill());
        log.info("Payment request sent to the bank");
        //store in db
        PaymentInfo paymentInfo = buildTransponderPaymentInfo(management2PaymentDto, paymentID);
        paymentInfoRepository.save(paymentInfo);
        log.info("Payment information stored");
    }

    @Override
    public void processConfirmationFromBank(Bank2PaymentDto bank2PaymentDto){
        PaymentInfo paymentInfo = paymentInfoRepository.findByPaymentId(bank2PaymentDto.getPaymentID());
        if(paymentInfo!=null){
            if(paymentInfo.getProductName() == TypeOfOrder.Parking){
                PaymentConfirmation2GateDto paymentConfirmation2GateDto = new PaymentConfirmation2GateDto();
                paymentConfirmation2GateDto
                        .setPaymentStatus(bank2PaymentDto.getAck()? TypeOfPaymentStatus.Success : TypeOfPaymentStatus.Failed);
                paymentConfirmation2GateDto
                        .setLicensePlate(paymentInfo.getProductID()); // parking service id is license plate
                paymentConfirmation2GateMsgBusIF.sendConfirmationToGate(paymentConfirmation2GateDto);
            }else if(paymentInfo.getProductName() == TypeOfOrder.Transponder){
                PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto = new PaymentConfirmation2ManagementDto();
                paymentConfirmation2ManagementDto
                        .setMacID(paymentInfo.getProductID()); // transponder id is index
                paymentConfirmation2ManagementDto
                        .setPaymentStatus(bank2PaymentDto.getAck()? TypeOfPaymentStatus.Success : TypeOfPaymentStatus.Failed);
                //confirmation and mac external? how to implement if failed
            }
        }else{
            log.error("Payment ID not found");
        }
    }

    // build a buildPaymentInfo model
    private  PaymentInfo buildTransponderPaymentInfo(Management2PaymentDto management2PaymentDto, String paymentID){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentId(paymentID);
        paymentInfo.setProductName(TypeOfOrder.Transponder);
        paymentInfo.setProductID(management2PaymentDto.getMacID());
        paymentInfo.setBill(management2PaymentDto.getBill());
        paymentInfo.setPaymentStatus(TypeOfPaymentStatus.Processing);
        return paymentInfo;
    }

    private  PaymentInfo buildParkingPaymentInfo(Gate2PaymentDto gate2PaymentDto, String paymentID){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentId(paymentID);
        paymentInfo.setProductName(TypeOfOrder.Parking);
        paymentInfo.setProductID(gate2PaymentDto.getLicensePlate());
        paymentInfo.setBill(gate2PaymentDto.getBill());
        paymentInfo.setPaymentStatus(TypeOfPaymentStatus.Processing);
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
            }
            // Get the first 16 or 20 characters (hexadecimal representation)
            String shortUUID = hexString.toString().substring(0, 16); //
            System.out.println(shortUUID);
            return shortUUID;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
