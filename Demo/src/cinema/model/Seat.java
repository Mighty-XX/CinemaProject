package cinema.model;

public class Seat {
    private int    seatId;
    private int    roomId;
    private String seatRow;     // A, B, C …
    private int    seatNumber;
    private String seatType;    // Standard, VIP, Couple
    private boolean booked;     // runtime flag for seat map

    public Seat() {}

    public Seat(int seatId, int roomId, String seatRow, int seatNumber, String seatType) {
        this.seatId     = seatId;
        this.roomId     = roomId;
        this.seatRow    = seatRow;
        this.seatNumber = seatNumber;
        this.seatType   = seatType;
    }

    public int    getSeatId()           { return seatId; }
    public void   setSeatId(int v)      { this.seatId = v; }

    public int    getRoomId()           { return roomId; }
    public void   setRoomId(int v)      { this.roomId = v; }

    public String getSeatRow()          { return seatRow; }
    public void   setSeatRow(String v)  { this.seatRow = v; }

    public int    getSeatNumber()       { return seatNumber; }
    public void   setSeatNumber(int v)  { this.seatNumber = v; }

    public String getSeatType()         { return seatType; }
    public void   setSeatType(String v) { this.seatType = v; }

    public boolean isBooked()           { return booked; }
    public void    setBooked(boolean v) { this.booked = v; }

    public String getLabel()            { return seatRow + seatNumber; }

    @Override public String toString()  { return getLabel() + " [" + seatType + "]"; }
}
