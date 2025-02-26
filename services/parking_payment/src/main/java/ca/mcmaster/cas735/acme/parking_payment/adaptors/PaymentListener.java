package ca.mcmaster.cas735.acme.parking_payment.adaptors;

import ca.mcmaster.cas735.acme.parking_payment.business.ProcessPaymentInfo;
import ca.mcmaster.cas735.acme.parking_payment.dto.*;
import ca.mcmaster.cas735.acme.parking_payment.ports.*;
import ca.mcmaster.cas735.acme.parking_payment.repository.PaymentInfoRepository;
import ca.mcmaster.cas735.acme.parking_payment.utils.PaymentStatus;
import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final Payment2MacSystemIF payment2MacSystemIF;
    private final PaymentConfirmation2ManagementIF paymentConfirmation2ManagementIF;
    private final ProcessPaymentInfo processPaymentInfo;
    private final PaymentInfoRepository paymentInfoRepository;
    private final Payment2Avl payment2Avl;


    //listener for the bank-------------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_bank.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-bank}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*bank2payment"))
    public void listenBank(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) { //listener for bank
        System.out.println(message + queue + 'a');
        // making sure messages from different exchanges will trigger different functions
        Bank2PaymentDto bank2PaymentDto = translate(message, Bank2PaymentDto.class);
        log.info("Received response from bank: {}", bank2PaymentDto);
            processPaymentInfo.processConfirmationFromBank(bank2PaymentDto);
    }

    //listener for the gate-------------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_gate.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-gate}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*gate2payment"))
    public void listenGate(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) { // generate payment ID
        System.out.println(message + queue + 'b');
        Gate2PaymentDto gate2PaymentDto = translate(message, Gate2PaymentDto.class);
        processPaymentInfo.processPaymentFromGate(gate2PaymentDto); // handle gateDto to processor
    }

    //listener for the parking manager-------------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_manager.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-manager}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*manager2payment"))
    public void listenManager(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        System.out.println(message + queue + 'c');
        Management2PaymentDto management2PaymentDto = translate(message, Management2PaymentDto.class);
        if(management2PaymentDto != null) {
            if(Objects.equals(management2PaymentDto.getPaymentMethod(), TypeOfPaymentMethod.Bank)) {
                log.info("Users pay transponder via bank");
                processPaymentInfo.processPaymentFromManagement(management2PaymentDto);
            }else if (Objects.equals(management2PaymentDto.getPaymentMethod(), TypeOfPaymentMethod.Mac)) {
                log.info("Users pay transponder via payslip");
                PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto = new PaymentConfirmation2ManagementDto();
                paymentConfirmation2ManagementDto.setMacID(management2PaymentDto.getMacID());
                paymentConfirmation2ManagementDto.setPaymentStatus(PaymentStatus.Success);
                // keep record in Mac system(external), bill will be paid through slip
                payment2MacSystemIF.updateTransponder(paymentConfirmation2ManagementDto);
                paymentConfirmation2ManagementIF.sendConfirmationToManager(paymentConfirmation2ManagementDto);
            }else {
                log.error("Unknown payment method");
            }
        }else {
            log.error("No valid message found from Parking Manager");
        }
    }

    //listener for enforcement---------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_enforcement.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-enforcement}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*enforcement2payment"))
    public void listenEnforcement(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        System.out.println(message + queue + 'd');
        Enforcement2PaymentDto enforcement2PaymentDto = translate(message, Enforcement2PaymentDto.class);
        log.info("Received response from bank: {}", enforcement2PaymentDto);
        payment2MacSystemIF.updateFine(enforcement2PaymentDto); //update fine status on Mac system
    }
    //listener for avl---------------
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_avl.queue", durable = "true"),
            exchange = @Exchange(value = "${app.messaging.inbound-exchange-avl}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*avl2payment"))
    public void listenAvl(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        System.out.println(message + queue + 'e');
        if(translate(message, AvailabilityRequest.class).getKey()){
            log.info("Users pay transponder via avl");
            payment2Avl.send2avl(new Payment2AvailDTO(paymentInfoRepository.countTransponderSales(),
                    paymentInfoRepository.SumSales(),
                    paymentInfoRepository.SumTransponderSales(),
                    paymentInfoRepository.SumParkingSales()));
        }
    }

    private <T> T translate(String message, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(message, clazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
