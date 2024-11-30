package ca.mcmaster.cas735.acme.parking_management.controller;

import ca.mcmaster.cas735.acme.parking_management.business.OrderProcessorIF;
import ca.mcmaster.cas735.acme.parking_management.dtos.OrderReqDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.ClientRawDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.OrderResDto;
import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;
import ca.mcmaster.cas735.acme.parking_management.utils.IdGenerator;
import ca.mcmaster.cas735.acme.parking_management.utils.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/transponder")
@RequiredArgsConstructor
public class TransponderRequestController {

    private final OrderProcessorIF orderProcessor;
    private final PaymentIF paymentIF;

    @PostMapping("/register") //add an update for everytime this client login
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResDto> createPaymentRequest(@RequestBody ClientRawDto clientRawDto) {
        try{
            OrderReqDto orderReqDto = addUUID(clientRawDto);
            OrderResDto orderResDto = orderProcessor.processRegistration(orderReqDto);
            //only non-repeated order plus non-repeated transponder
            if (!orderResDto.isDuplicateOrderId() && !orderResDto.isDuplicateMacId()){
                return ResponseEntity.status(HttpStatus.CREATED).body(orderResDto);
            }else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(orderResDto); // trans existed
            }
            // 00 means new order added
            // 01 means transponder existed, please try renewal
            // 11 means order repeated
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/renew")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResDto> renewPaymentRequest(@RequestBody ClientRawDto clientRawDto) {
        try{
            OrderReqDto orderReqDto = addUUID(clientRawDto);
            OrderResDto orderResDto = orderProcessor.processRenewal(orderReqDto);
            //only non-repeated order plus repeated transponder id plus paid
            if (!orderResDto.isDuplicateOrderId() && orderResDto.isDuplicateMacId()){
                return ResponseEntity.status(HttpStatus.CREATED).body(orderResDto);
            }else{
                return ResponseEntity.status(HttpStatus.CONFLICT).body(orderResDto);
            }
            // 00 means no existed transponder, please try register a new one
            // 01 means transponder existed, can be renewed
            // 10 is manually set, means an existed unpaid transponder order, need to be paid first
            // 11 means order repeated
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/deleteOrder/{orderID}") //change it to orderID
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteOrder(@PathVariable String orderID) {
        try{
            boolean resp = orderProcessor.processDeletion(orderID);
            if (resp){
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("unpaid order deleted");
            }else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("paid orders cannot be deleted!" );
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no matched orders");
        }
    }

    @PostMapping("/update/{macID}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserStatus> updateUserInfo(@PathVariable String macID){
        try{
            UserStatus userStatus = orderProcessor.updateUserInfo(macID);
            if (userStatus ==UserStatus.Valid){
                return ResponseEntity.status(HttpStatus.OK).body(userStatus);
            }else if(userStatus ==UserStatus.Expired){
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(userStatus);
            }else{
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(userStatus);
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private OrderReqDto addUUID(ClientRawDto clientRawDto){
        String macID = clientRawDto.getMacID();
        String clientID = clientRawDto.getClientOrderId();
        // add an uniqueID as transponder ID to avoid repetition of orders
        OrderReqDto orderReqDto = new OrderReqDto();
        orderReqDto.setMacID(macID);
        orderReqDto.setOrderId(IdGenerator.generateUUID(macID+clientID));
        orderReqDto.setLicensePlate(clientRawDto.getLicensePlate());
        orderReqDto.setTransponderID(IdGenerator.generateUUID(macID));
        orderReqDto.setTimeStamp(clientRawDto.getTimeStamp());
        orderReqDto.setTransponderType(clientRawDto.getTransponderType());
        orderReqDto.setPaymentMethod(clientRawDto.getPaymentMethod());
        log.info(orderReqDto.toString());
        return orderReqDto;
    }
}
//post:
// http://localhost:9080/transponder/register/
// body:
// {
//        "macID": "400608194",
//        "bill":10,
//        "licensePlate": "U57U1",
//        "timeStamp": 1731425099,
//        "paymentMethod": "bank"
//}