package cinema.model;

public class Room {
    private int    roomId;
    private String roomName;
    private String roomType;    // Standard, IMAX, 4DX, VIP

    public Room() {}

    public Room(int roomId, String roomName, String roomType) {
        this.roomId   = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
    }

    public int    getRoomId()           { return roomId; }
    public void   setRoomId(int v)      { this.roomId = v; }

    public String getRoomName()         { return roomName; }
    public void   setRoomName(String v) { this.roomName = v; }

    public String getRoomType()         { return roomType; }
    public void   setRoomType(String v) { this.roomType = v; }

    @Override public String toString()  { return roomName + " (" + roomType + ")"; }
}
