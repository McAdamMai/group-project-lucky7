package ca.mcmaster.cas735.acme.parking_enforcement.ports;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineLicenseDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.MemberDTO;

public interface FineFilter {

    void findMember(FineLicenseDTO message);
    void sendFine(MemberDTO member);

}