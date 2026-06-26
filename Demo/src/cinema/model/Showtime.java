package cinema.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Showtime {
    private int       showtimeId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate showDate;

    public Showtime() {}

    public Showtime(int showtimeId, LocalTime startTime, LocalTime endTime, LocalDate showDate) {
        this.showtimeId = showtimeId;
        this.startTime  = startTime;
        this.endTime    = endTime;
        this.showDate   = showDate;
    }

    public int       getShowtimeId()             { return showtimeId; }
    public void      setShowtimeId(int v)        { this.showtimeId = v; }

    public LocalTime getStartTime()              { return startTime; }
    public void      setStartTime(LocalTime v)   { this.startTime = v; }

    public LocalTime getEndTime()                { return endTime; }
    public void      setEndTime(LocalTime v)     { this.endTime = v; }

    public LocalDate getShowDate()               { return showDate; }
    public void      setShowDate(LocalDate v)    { this.showDate = v; }

    @Override public String toString() {
        java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return String.format("%s  |  %s → %s", showDate, startTime.format(timeFmt), endTime.format(timeFmt));
    }
}
