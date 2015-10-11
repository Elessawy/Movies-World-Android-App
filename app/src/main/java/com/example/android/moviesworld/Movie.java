package com.example.android.moviesworld;

/**
 * Created by AbdElrahman on 23/9/2015.
 */
public class Movie {

    int movieId;
    String originalTitle;
    String title;
    String releaseDate;
    String overview;
    double voteAverage;
    int voteCount;
    String posterPath;
    String coverPath;

    public void Movie(){
        movieId = -1;
        originalTitle = "";
        title = "";
        releaseDate = "";
        overview = "";
        voteAverage = -1;
        voteCount = -1;
        posterPath = "";
        coverPath = "";
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int id) {
        this.movieId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public Movie clone(){

        Movie movie = new Movie();

        movie.setMovieId(movieId);
        movie.setOriginalTitle(originalTitle);
        movie.setTitle(title);
        movie.setReleaseDate(releaseDate);
        movie.setVoteAverage(voteAverage);
        movie.setVoteCount(voteCount);
        movie.setOverview(overview);
        movie.setPosterPath(posterPath);
        movie.setCoverPath(coverPath);

        return movie;
    }
}

