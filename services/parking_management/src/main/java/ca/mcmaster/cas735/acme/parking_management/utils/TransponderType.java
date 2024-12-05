package ca.mcmaster.cas735.acme.parking_management.utils;

public enum TransponderType {
    RENEW1(1, 50), //renew can be done no matter transponder expired or not
    RENEW2(2, 100),
    RENEW3(3, 120),
    RENEW4(4, 160 ),
    RENEW5(5, 200),
    RENEW6(6, 240),
    RENEW12(12, 360),
    REGI1(1, 50), //renew can be done no matter transponder expired or not
    REGI2(2, 100),
    REGI3(3, 120),
    REGI4(4, 160 ),
    REGI5(5, 200),
    REGI6(6, 240),
    REGI12(12, 360);


    private final int numberOfMonths;
    private final int price;
    //constructor
    TransponderType(int numberOfMonths, int price) {
        this.numberOfMonths = numberOfMonths;
        this.price = price;
    }

    //Getter
    public int getNumberOfMonths() {
        return numberOfMonths;
    }

    public int getPrice() {return price;}
}
