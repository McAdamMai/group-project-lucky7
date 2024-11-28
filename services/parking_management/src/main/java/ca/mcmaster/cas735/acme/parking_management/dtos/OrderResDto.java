package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderResDto {
    private String transponderId;
    private boolean duplicateOrderId;
    private boolean duplicateMacId;

    public OrderResDto(String transponderID, boolean duplicateOrderId, boolean duplicateMacId) {
        this.transponderId = transponderID;
        this.duplicateOrderId = duplicateOrderId;
        this.duplicateMacId = duplicateMacId;
    }
}
