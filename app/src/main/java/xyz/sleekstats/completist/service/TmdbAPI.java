package xyz.sleekstats.completist.service;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;

public interface TmdbAPI {

    String API_KEY = "b5f45c3ea3adf1ca53b96fa5bb9394d2";

    //Query for specific film details based on Tmdb film id
    @GET("movie/{movie_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&append_to_response=credits")
    Observable<FilmPOJO> retrieveFilm(
            @Path("movie_id") String movie_id
    );

    //Query for specific tv show details based on Tmdb show id
    @GET("tv/{show_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&append_to_response=credits")
    Observable<FilmPOJO> retrieveShow(
            @Path("show_id") String show_id
    );

    //Query for list of films by a specific actor/director based on Tmdb actor/director id
    @GET("person/{person_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&append_to_response=movie_credits")
    Observable<PersonPOJO> retrievePersonInfo(
            @Path("person_id") String person_id
    );

    @GET("https://api.themoviedb.org/3/search/multi?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2" +
            "&language=en-US&page=1&include_adult=false")
    Observable<MediaQueryPOJO> queryFilms(
            @Query("query") String movie_query
    );

    @GET("https://api.themoviedb.org/3/person/popular?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&page=1")
    Observable<PersonQueryPOJO> retrievePopularActors();
}
