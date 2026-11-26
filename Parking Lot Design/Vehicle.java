public abstract class Vehicle {
    protected String licenSeNo;
    protected ParkingTicket ticket;

    protected Vehicle(String licenSeNo,ParkingTicket ticket){
        this.licenSeNo = licenSeNo;
        this.ticket = ticket;
    }

    public abstract void assignTicket();
}
