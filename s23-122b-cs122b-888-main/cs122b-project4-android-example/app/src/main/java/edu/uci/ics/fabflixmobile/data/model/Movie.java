package edu.uci.ics.fabflixmobile.data.model;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String movieId;
    private final String name;
    private final String year;
    private final String genres;
    private final String stars;
    private final String director;
    private final String rating;



    public Movie(String movieId, String name, String year, String genres, String stars, String director, String rating) {
        this.movieId = movieId;
        this.name = name;
        this.year = year;
        this.genres = genres;
        this.stars = stars;
        this.director = director;
        this.rating = rating;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public String getGenres() {
        return genres;
    }

    public String getStars() {
        return stars;
    }

    public String getDirector() {
        return director;
    }

    public String getRating() {
        return rating;
    }


}