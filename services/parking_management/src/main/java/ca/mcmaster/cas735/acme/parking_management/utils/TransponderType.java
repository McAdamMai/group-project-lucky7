package ca.mcmaster.cas735.acme.parking_management.utils;

public enum TransponderType {
    RENEW1(1, "Month"), //renew can be done no matter transponder expired or not
    RENEW2(2, "Month"),
    RENEW3(3, "Month"),
    RENEW4(1, "Term"),
    RENEW5(2, "Term"),
    RENEW6(1, "Year"),
    Register1(1, "Month"),
    Register2(2, "Month"),
    Register3(3, "Month"),
    Register4(1, "Term"),
    Register5(2, "Term"),
    Register6(1, "Year");


    private final int numberOfMonths;
    private final String timeSpan;
    //constructor
    TransponderType(int numberOfMonths, String timeSpan) {
        this.numberOfMonths = numberOfMonths;
        this.timeSpan = timeSpan;
    }

    //Getter
    public int getNumberOfMonths() {
        return numberOfMonths;
    }

    public String getTimeSpan() {return timeSpan;}
}
