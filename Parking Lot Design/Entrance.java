
public class Entrance {
    private int id;

    public Entrance(){

    }

    public Entrance(int id){
        this.id = id;
    }

    public ParkingTicket getTicket(){
        return new ParkingTicket();
    }
}
