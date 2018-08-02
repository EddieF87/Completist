package xyz.sleekstats.completist.service;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.PersonPOJO;

public interface TmdbAPI {


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


}
