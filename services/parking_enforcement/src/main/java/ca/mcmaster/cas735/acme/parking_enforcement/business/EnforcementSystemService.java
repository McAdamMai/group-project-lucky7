package ca.mcmaster.cas735.acme.parking_enforcement.business;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.MemberDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineLicenseDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FinePaymentDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineGateDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.ports.FineFilter;
import ca.mcmaster.cas735.acme.parking_enforcement.ports.FinePayment;
import ca.mcmaster.cas735.acme.parking_enforcement.ports.FineGate;
import ca.mcmaster.cas735.acme.parking_enforcement.adapters.AMQPSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service @Slf4j
public class EnforcementSystemService implements FineFilter, FineGate, FinePayment {

    private final AMQPSender sender;

    @Autowired
    public EnforcementSystemService(AMQPSender sender) {
        this.sender = sender;
    }

    @Override
    public void findMember(FineLicenseDTO message) {
        sender.sendManagement(message);
    }

    @Override
    public void sendFine(MemberDTO member) {
        if (member.getFound()) {
            FinePaymentDTO fineP = new FinePaymentDTO();
            fineP.setMacID(member.getMacID());
            fineP.setLicensePlate(member.getLicense());
            fineP.setBill(15);
            fineP.setTimeStamp(member.getTimeStamp());
            fineP.setFineReason(member.getReason());
            sendFinePayment(fineP);
        } else {
            FineGateDTO fineG = new FineGateDTO();
            fineG.setLicensePlate(member.getLicense());
            fineG.setBill(15);
            fineG.setTimeStamp(member.getTimeStamp());
            fineG.setFineReason(member.getReason());
            sendFineGate(fineG);
        }
    }

    @Override
    public void sendFinePayment(FinePaymentDTO fine) {
        sender.sendPayment(fine);
        System.out.println("Sent fine to payment for license: " + fine.getLicensePlate());
    }

    @Override
    public void sendFineGate(FineGateDTO fine) {
        sender.sendGate(fine);
        System.out.println("Sent fine to gate for license: " + fine.getLicensePlate());
    }
}
