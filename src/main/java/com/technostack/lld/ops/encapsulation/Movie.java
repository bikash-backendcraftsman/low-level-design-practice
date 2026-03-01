package com.technostack.lld.ops.encapsulation;

import java.time.LocalDate;

public class Movie {

    //Data members  | state
    private String movieId;
    private String title;
    private String movieGenre;
    private LocalDate releasedYear = LocalDate.now();

    //Default Constructor
    public Movie(){

    }
    //Parameterized Constructor
    public Movie(String movieId, String tittle, String movieGenre, LocalDate releasedYear) {
        this.movieId = movieId;
        this.title = tittle;
        this.movieGenre = movieGenre;
        this.releasedYear = releasedYear;
    }

    //Getters and Setters
    public String getMovieId(){
        return movieId;
    }

    public void setMovieId(String movieId){
        this.movieId = movieId;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(String movieGenre) {
        this.movieGenre = movieGenre;
    }

    public LocalDate getReleasedYear() {
        return releasedYear;
    }

    public void setReleasedYear(LocalDate releasedYear) {
        this.releasedYear = releasedYear;
    }

    /**
     * This will print the movie Information.
     */
    public void printMovieInformation(){
        System.out.println("Title: " + this.title);
        System.out.println("Year: " + this.releasedYear);
        System.out.println("Genre: " + this.movieGenre);
    }

    /**
     * As this is a private method(), it can only be access inside the current Movie class.
     * Can not call this private methods outside the Movie class.
     * @param tittle
     * @param movieGenre
     */
    private static void printTittleWithGenre(String tittle,String movieGenre){
        System.out.println("Movie Tittle with Genre Type : "+tittle+" :-> "+movieGenre );
    }

    public static void main(String[] args) {
        Movie movie = new Movie();
        movie.setMovieId("34500");
        movie.setTitle("Badmash Company");
        movie.setMovieGenre("Comedy");
        movie.setReleasedYear(LocalDate.now());

        movie.printMovieInformation();

        System.out.println("---");
        movie.setTitle("Forrest Gump");
        System.out.print("New title: " + movie.getTitle());

        System.out.println("---");
        printTittleWithGenre(movie.getTitle(),movie.getMovieGenre());
    }
}
