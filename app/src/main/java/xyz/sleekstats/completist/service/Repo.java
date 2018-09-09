package xyz.sleekstats.completist.service;

import android.app.Application;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;

public class Repo {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private TmdbAPI tmdbAPI;


    public Repo(Application application) {
        if(tmdbAPI == null) {
            tmdbAPI = getData();
        }
    }

    //Build Retrofit API service
    private TmdbAPI getData() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        Retrofit retrofit = builder.build();
        return retrofit.create(TmdbAPI.class);
    }

    //Get list of films matching search query
    public Observable<MediaQueryPOJO> queryFilms(String movieQuery) {
        return tmdbAPI.queryFilms(movieQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get specific film details based  Tmdb film id
    public Observable<FilmPOJO> getFilm(String movie_id) {
        return tmdbAPI.retrieveFilm(movie_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of films by a specific actor/director based on Tmdb actor/director id
    public Observable<PersonPOJO> getFilmsByPerson(String person_id) {
        return tmdbAPI.retrievePersonInfo(person_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get specific film details based  Tmdb film id
    public Observable<FilmPOJO> getShow(String show_id) {
        return tmdbAPI.retrieveShow(show_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    //Get list of most popular film-people
    public Observable<PersonQueryPOJO> getPopularActors() {
        return tmdbAPI.retrievePopularActors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
