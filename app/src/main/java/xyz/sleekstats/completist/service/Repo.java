package xyz.sleekstats.completist.service;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.MovieRoomDB;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.model.ResultsPOJO;

public class Repo {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private TmdbAPI tmdbAPI;
    private MovieDao mMovieDao;

    public Repo(Application application) {

        if (tmdbAPI == null) {
            tmdbAPI = getData();
        }

        if (mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(application);
            this.mMovieDao = db.movieDao();
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

    //Get list of most popular films
    public Observable<List<FilmByPerson>> getPopularFilms() {
        return tmdbAPI.retrievePopularMovies()
                .map(ResultsPOJO::getResults)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of films currently playing in theatres
    public Observable<List<FilmByPerson>> getNowPlaying() {
        return tmdbAPI.retrieveNowPlaying()
                .map(ResultsPOJO::getResults)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of top-rated films
    public Observable<List<FilmByPerson>> getTopRated() {
        return tmdbAPI.retrieveTopRated()
                .map(ResultsPOJO::getResults)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of top-rated films
    public Observable<List<FilmByPerson>> getMyMovies() {
        return mMovieDao.getSavedMovies()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void insertMovie(FilmByPerson movie, String personID) {
        mMovieDao.insertMovie(movie);
        addRemoveAll(String.valueOf(movie.getId()), personID, true);
    }

    public void removeMovie(String movieID, String personID) {
        mMovieDao.removeMovie(movieID);
        addRemoveAll(movieID, personID, false);
    }

    public void insertList(MyList list) {
        mMovieDao.insertList(list);
    }

    public void removeList(String id) {
        mMovieDao.removeList(id);
    }

    public void updateList(int numberSeen, int numberOfMovies, String id) {
        mMovieDao.updateListWatched(numberSeen, numberOfMovies, id);
    }

    public Single<FilmByPerson> checkIfMovieExists(String id) {
        return mMovieDao.checkIfMovieExists(id);
    }

    public Maybe<MyList> checkIfListExists(String id) {
        return mMovieDao.checkIfListExists(id);
    }

    public Single<List<FilmByPerson>> getMoviesWatched(List<String> ids) {
        return mMovieDao.getMoviesWatched(ids);
    }

    public Flowable<List<MyList>> getSavedLists() {
        return mMovieDao.getSavedLists();
    }


    private void addRemoveAll(String movieID, String personID, boolean addWatched) {

        Single<List<MyList>> listSingle = mMovieDao.getSavedListsToUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(x -> Log.d("yyyy", "myListsObservable doOnError" + x.getMessage()));

        Single<List<String>> castSingle = tmdbAPI.retrieveFilm(movieID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(FilmPOJO::getCastCredits)
                .map(CastCredits::bothLists)
                .flatMapIterable(list -> list)
                .map(CastInfo::getId)
                .toList();

        Single.zip(listSingle, castSingle, (x, y) -> {
                    List<String> ids = new ArrayList<>();
                    for (MyList myList : x) {
                        String id = String.valueOf(myList.getList_id());
                        if (y.contains(id)) {
                            ids.add(id);
                        }
                    }
//                    if (ids.contains(personID)) {
//                        ids.remove(personID);
//                    }
                    return ids;
                }
        ).subscribe(ids -> {
            if (addWatched) {
                for (String id : ids) {
                    mMovieDao.addWatchedMovie(id);
                }
            } else {
                for (String id : ids) {
                    mMovieDao.removeWatchedMovie(id);
                }
            }
        });
    }
}
