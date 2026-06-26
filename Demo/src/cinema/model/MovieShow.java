package cinema.model;

public class MovieShow {
    private int    movieShowId;
    private int    movieId;
    private int    roomId;
    private int    showtimeId;
    private String status;       // UPCOMING, ONGOING, CANCELLED

    // Populated via JOIN for display
    private Movie    movie;
    private Room     room;
    private Showtime showtime;

    public MovieShow() {}

    public MovieShow(int movieShowId, int movieId, int roomId, int showtimeId, String status) {
        this.movieShowId = movieShowId;
        this.movieId     = movieId;
        this.roomId      = roomId;
        this.showtimeId  = showtimeId;
        this.status      = status;
    }

    public int    getMovieShowId()         { return movieShowId; }
    public void   setMovieShowId(int v)    { this.movieShowId = v; }

    public int    getMovieId()             { return movieId; }
    public void   setMovieId(int v)        { this.movieId = v; }

    public int    getRoomId()              { return roomId; }
    public void   setRoomId(int v)         { this.roomId = v; }

    public int    getShowtimeId()          { return showtimeId; }
    public void   setShowtimeId(int v)     { this.showtimeId = v; }

    public String getStatus()              { return status; }
    public void   setStatus(String v)      { this.status = v; }

    public Movie    getMovie()             { return movie; }
    public void     setMovie(Movie v)      { this.movie = v; }

    public Room     getRoom()              { return room; }
    public void     setRoom(Room v)        { this.room = v; }

    public Showtime getShowtime()          { return showtime; }
    public void     setShowtime(Showtime v){ this.showtime = v; }

    @Override public String toString() {
        if (movie != null && showtime != null)
            return movie.getTitle() + " | " + showtime;
        return "Show #" + movieShowId;
    }
}
