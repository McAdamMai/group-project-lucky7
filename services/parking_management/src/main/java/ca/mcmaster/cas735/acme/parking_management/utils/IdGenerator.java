package ca.mcmaster.cas735.acme.parking_management.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class IdGenerator {
    private IdGenerator() {
        throw new UnsupportedOperationException("IdGenerator is a utility class and cannot be instantiated");
    }
    public static String generateUUID(String key){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(key.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash){
                String hex = Integer.toHexString(0xff & b); // signed to unsigned
                if (hex.length() == 1) hexString.append('0'); // 0-15 extend to 2-digits hexadecimal
                hexString.append(hex);
            }
            String shortUUID = hexString.toString().substring(0, 16); //
            log.info("hex:{}, UUID: {}", hexString, shortUUID);;
            return shortUUID;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
