package ca.mcmaster.cas735.acme.gate_system.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GateSystemUtils {
    public <T> T translate(String message, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(message, clazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public byte[] translateToBytes(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsBytes(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
