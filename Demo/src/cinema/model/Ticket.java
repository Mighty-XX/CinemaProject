package cinema.model;

import java.math.BigDecimal;

public class Ticket {
    private int        ticketId;
    private int        movieShowId;
    private int        seatId;
    private int        billId;
    private BigDecimal ticketPrice;
    private String     status;         // BOOKED, CANCELLED, USED
    private String     bookingChannel; // ONLINE, COUNTER

    // Display helpers
    private String     seatLabel;
    private String     movieTitle;

    public Ticket() {}

    public Ticket(int movieShowId, int seatId, int billId,
                  BigDecimal ticketPrice, String bookingChannel) {
        this.movieShowId    = movieShowId;
        this.seatId         = seatId;
        this.billId         = billId;
        this.ticketPrice    = ticketPrice;
        this.status         = "BOOKED";
        this.bookingChannel = bookingChannel;
    }

    public int        getTicketId()               { return ticketId; }
    public void       setTicketId(int v)          { this.ticketId = v; }

    public int        getMovieShowId()            { return movieShowId; }
    public void       setMovieShowId(int v)       { this.movieShowId = v; }

    public int        getSeatId()                 { return seatId; }
    public void       setSeatId(int v)            { this.seatId = v; }

    public int        getBillId()                 { return billId; }
    public void       setBillId(int v)            { this.billId = v; }

    public BigDecimal getTicketPrice()            { return ticketPrice; }
    public void       setTicketPrice(BigDecimal v){ this.ticketPrice = v; }

    public String     getStatus()                 { return status; }
    public void       setStatus(String v)         { this.status = v; }

    public String     getBookingChannel()         { return bookingChannel; }
    public void       setBookingChannel(String v) { this.bookingChannel = v; }

    public String     getSeatLabel()              { return seatLabel; }
    public void       setSeatLabel(String v)      { this.seatLabel = v; }

    public String     getMovieTitle()             { return movieTitle; }
    public void       setMovieTitle(String v)     { this.movieTitle = v; }
}
