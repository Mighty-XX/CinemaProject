package cinema.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Movie {
    private int    movieId;
    private String title;
    private String director;
    private List<String> castMembers = new ArrayList<>();  // from Cast_Member table
    private String genre;
    private int    duration;      // minutes
    private String language;
    private String ageRating;
    private LocalDate releaseDate;
    private String synopsis;
    // UI only – not in DB
    private String posterColor;   // hex for gradient fallback

    public Movie() {}

    public Movie(int movieId, String title, String director, List<String> castMembers,
                 String genre, int duration, String language, String ageRating,
                 LocalDate releaseDate, String synopsis) {
        this.movieId     = movieId;
        this.title       = title;
        this.director    = director;
        this.castMembers = castMembers != null ? castMembers : new ArrayList<>();
        this.genre       = genre;
        this.duration    = duration;
        this.language    = language;
        this.ageRating   = ageRating;
        this.releaseDate = releaseDate;
        this.synopsis    = synopsis;
    }

    // ── Getters / Setters ───────────────────────────────────────
    public int    getMovieId()            { return movieId; }
    public void   setMovieId(int v)       { this.movieId = v; }

    public String getTitle()              { return title; }
    public void   setTitle(String v)      { this.title = v; }

    public String getDirector()           { return director; }
    public void   setDirector(String v)   { this.director = v; }

    public List<String> getCastMembers()             { return castMembers; }
    public void         setCastMembers(List<String> v){ this.castMembers = v != null ? v : new ArrayList<>(); }

    /** Convenience: comma-separated string for display in UI. */
    public String getCastMembersDisplay() {
        return castMembers == null || castMembers.isEmpty() ? "—" : String.join(", ", castMembers);
    }

    public String getGenre()              { return genre; }
    public void   setGenre(String v)      { this.genre = v; }

    public int    getDuration()           { return duration; }
    public void   setDuration(int v)      { this.duration = v; }

    public String getLanguage()           { return language; }
    public void   setLanguage(String v)   { this.language = v; }

    public String getAgeRating()          { return ageRating; }
    public void   setAgeRating(String v)  { this.ageRating = v; }

    public LocalDate getReleaseDate()           { return releaseDate; }
    public void      setReleaseDate(LocalDate v){ this.releaseDate = v; }

    public String getSynopsis()           { return synopsis; }
    public void   setSynopsis(String v)   { this.synopsis = v; }

    public String getPosterColor()        { return posterColor; }
    public void   setPosterColor(String v){ this.posterColor = v; }

    public String getDurationFormatted() {
        return String.format("%dh %dm", duration / 60, duration % 60);
    }

    @Override public String toString()    { return title; }
}
