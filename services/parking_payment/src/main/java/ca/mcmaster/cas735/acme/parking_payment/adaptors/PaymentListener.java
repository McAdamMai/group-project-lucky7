package ca.mcmaster.cas735.acme.parking_payment.adaptors;

import ca.mcmaster.cas735.acme.parking_payment.ports.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.mcmaster.cas735.acme.parking_payment.dto.EnforcementDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.BankRespDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.GateDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerConfirmationDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.GateConfirmationDto;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {
    private final UploadToMacSystemIF uploadToMacSystemIF;
    private final ReqToBankIF reqToBankIF;
    private final ConfirmationToManager confirmationToManager;
    private final ConfirmationToGateMsgBus confirmationToGateMsgBus;
    //private final ConfirmationToGateREST confirmationToGateREST; gate confirmation using REST

    private boolean paymentConfirmed = false;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_bank.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-bank}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*bank"))
    public void listenBank(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) { //listener for bank
        System.out.println(message + queue + 'a');
        // making sure messages from different exchanges will trigger different functions
        BankRespDto bankRespDto = translate(message, BankRespDto.class);
        log.info("Received response from bank: {}", bankRespDto);
        GateConfirmationDto gateConfirmationDto = new GateConfirmationDto();
        gateConfirmationDto.setPaymentStatus(bankRespDto.getAck()); // return ack from bank to gate, if payment fails after trials, officer can let go
        gateConfirmationDto.setLicensePlate(bankRespDto.getInfo());
        //confirmationToGateREST.sendConfirmationToGate(gateConfirmationDto); // gate confirmation using REST
    }

    //listener for the gate-------------------//change to Rest
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_gate.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-gate}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*gate2payment"))
    public void listenGate(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) { // generate payment ID
        System.out.println(message + queue + 'b');
        GateDto gateRespDto = translate(message, GateDto.class);
        GateConfirmationDto gateConfirmationDto = new GateConfirmationDto();
        gateConfirmationDto.setLicensePlate(gateRespDto.getLicensePlate());
        gateConfirmationDto.setPaymentStatus(false); //initialize confirmation dto
        log.info("Received response from bank: {}", gateRespDto);
        if(gateConfirmationDto !=null){
            reqToBankIF.sendPaymentRequest(gateRespDto.getLicensePlate(),gateRespDto.getBill());
            log.info("visitors pay transponder via bank");
            processPayment();
            gateConfirmationDto.setPaymentStatus(true);
            confirmationToGateMsgBus.sendConfirmationToGate(gateConfirmationDto);
        }else {
            log.info("Gate response is null");
        }
    }

    //listener for the parking manager-------------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_manager.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-manager}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*manager"))
    public void listenManager(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        System.out.println(message + queue + 'c');
        ManagerDto managerDto = translate(message, ManagerDto.class);
        ManagerConfirmationDto  managerConfirmationDto = new ManagerConfirmationDto();
        managerConfirmationDto.setMacID(managerDto.getMacID());
        managerConfirmationDto.setTimeStamp(managerDto.getTimeStamp());
        managerConfirmationDto.setLicensePlate(managerDto.getLicensePlate());
        managerConfirmationDto.setPaymentStatus(false); //initialize confirmation dto
        log.info("Received response from bank: {}", managerDto);
        if(managerDto != null) {
            if(Objects.equals(managerDto.getPaymentMethod(), "bank")) {
                reqToBankIF.sendPaymentRequest(managerDto.getMacID(), managerDto.getBill()); //paid via bank
                log.info("Users pay transponder via bank");
                processPayment(); // wait for payment to be processed
                uploadToMacSystemIF.updateTransponder(managerDto); // record the bill and transponder's start date
                managerConfirmationDto.setPaymentStatus(true); // if payment failed, code will not continue.
                confirmationToManager.sendConfirmationToManager(managerConfirmationDto);// sending confirmation to the parking manager

            }else if (Objects.equals(managerDto.getPaymentMethod(), "mac")) {
                log.info("Users pay transponder via payslip");
                uploadToMacSystemIF.updateTransponder(managerDto); // record the bill and transponder's start date
                managerConfirmationDto.setPaymentStatus(true);
                confirmationToManager.sendConfirmationToManager(managerConfirmationDto);
            }else{
                log.info("Unknown payment method");
            }
        }else {
            log.info("No valid message found from Parking Manager, please retry");
        }
    }

    //listener for enforcement---------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_enforcement.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-enforcement}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*enforcement"))
    public void listenEnforcement(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        System.out.println(message + queue + 'd');
        EnforcementDto enforcementDto = translate(message, EnforcementDto.class);
        log.info("Received response from bank: {}", enforcementDto);
        uploadToMacSystemIF.updateFine(enforcementDto); //update fine status on Mac system
    }

    private <T> T translate(String message, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(message, clazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void processPayment(){
        paymentConfirmed = false; // initialize payment status
        log.info("Processing payment, please hold on");
        while(!paymentConfirmed){
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                Thread.currentThread().interrupt(); // wait for payment
            }
        }
        log.info("Payment confirmed");
        paymentConfirmed = false; // reset payment status
    }
}
