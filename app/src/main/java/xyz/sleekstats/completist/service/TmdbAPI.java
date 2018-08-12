package xyz.sleekstats.completist.service;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.QueryPOJO;

public interface TmdbAPI {

    String API_KEY = "b5f45c3ea3adf1ca53b96fa5bb9394d2";

    //API query for specific film details based on Tmdb film id
    @GET("movie/{movie_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&language=en-US&append_to_response=credits")
    Observable<FilmPOJO> retrieveFilm(
            @Path("movie_id") String movie_id
    );

    //API query for list of films by a specific actor/director based on Tmdb actor/director id
    @GET("person/{person_id}?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2&append_to_response=movie_credits")
    Observable<PersonPOJO> retrievePersonInfo(
            @Path("person_id") String person_id
    );

    @GET("https://api.themoviedb.org/3/search/multi?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2" +
            "&language=en-US&page=1&include_adult=false")
    Observable<QueryPOJO> queryFilms(
            @Query("query") String movie_query
    );

//    @GET("https://api.themoviedb.org/3/search/movie?api_key=b5f45c3ea3adf1ca53b96fa5bb9394d2" +
//            "&language=en-US&query=monk&page=1&include_adult=false")
//    Observable<QueryPOJO> queryPeople(
//            @Query("query") String person_query
//    );

}
