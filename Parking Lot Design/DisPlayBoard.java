import java.util.List;
import java.util.Map;

public class DisPlayBoard {
    private int id;
    private Map<String, List<ParkingSpot>> parkingSpots;

    public DisPlayBoard(int id, Map<String, List<ParkingSpot>>parkingSpots){
        this.id = id;
        this.parkingSpots = parkingSpots;
    }

    public void addParkingSpot(String spotType, List<ParkingSpot> parkingSpotList){

    }

    public void showFreeSlot(){

    }

}
