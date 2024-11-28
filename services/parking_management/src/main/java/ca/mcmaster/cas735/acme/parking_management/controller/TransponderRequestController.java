package ca.mcmaster.cas735.acme.parking_management.controller;

import ca.mcmaster.cas735.acme.parking_management.business.OrderProcessorIF;
import ca.mcmaster.cas735.acme.parking_management.dtos.OrderReqDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.ClientRawDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.OrderResDto;
import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;
import ca.mcmaster.cas735.acme.parking_management.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @PostMapping("/deleteOrder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<OrderResDto> deleteOrder(@RequestBody ClientRawDto clientRawDto) {
        try{
            OrderReqDto orderReqDto = addUUID(clientRawDto);
            OrderResDto orderResDto = orderProcessor.processDeletion(orderReqDto);
            if (orderResDto.isDuplicateOrderId()){
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(orderResDto);
            }else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(orderResDto);
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
        orderReqDto.setTransponderType(clientRawDto.getTransponderType());
        orderReqDto.setPaymentMethod(clientRawDto.getPaymentMethod());
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