package xyz.sleekstats.completist.service;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.model.ResultsPOJO;

public interface TmdbAPI {

    String API_KEY = "b5f45c3ea3adf1ca53b96fa5bb9394d2";

    //Query for specific film details based on Tmdb film id
    @GET("movie/{movie_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&append_to_response=credits")
    Single<FilmPOJO> retrieveFilm(
            @Path("movie_id") String movie_id
    );

    //Query for specific tv show details based on Tmdb show id
    @GET("tv/{show_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&append_to_response=credits")
    Single<FilmPOJO> retrieveShow(
            @Path("show_id") String show_id
    );

    //Query for list of films by a specific actor/director based on Tmdb actor/director id
    @GET("person/{person_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&append_to_response=movie_credits,tv_credits")
    Observable<PersonPOJO> retrievePersonInfo(
            @Path("person_id") String person_id
    );

    //retrieve most popular movies from TMDB API
    @GET("movie/popular?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US")
    Observable<ResultsPOJO> retrievePopularMovies();

    //retrieve movies currently playing in theatres from TMDB API
    @GET("movie/now_playing?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&page=1")
    Observable<ResultsPOJO> retrieveNowPlaying();

    //retrieve  top-rated movies from TMDB API
    @GET("movie/top_rated?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&page=1")
    Observable<ResultsPOJO> retrieveTopRated();

    @GET("search/multi?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2" +
            "&language=en-US&page=1&include_adult=false")
    Observable<MediaQueryPOJO> queryFilms(
            @Query("query") String movie_query
    );

    @GET("person/popular?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&page=1")
    Observable<PersonQueryPOJO> retrievePopularActors();


}
