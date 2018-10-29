package xyz.sleekstats.completist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmListDetails;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.MyMovie;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.service.Repo;

public class MovieViewModel extends AndroidViewModel {

    private final Repo mRepo;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        mRepo = new Repo(application);
    }

    public Observable<FilmPOJO> getMovieInfo(String movieId) {
        return mRepo.getFilm(movieId);
    }

    public Observable<FilmListDetails> getFilmsByPerson(String personId) {

        Observable<PersonPOJO> personObservable;
        Observable<List<FilmByPerson>> filmsObservable;

        switch (personId) {
            case "my":
                personObservable = Observable.just(new PersonPOJO("My Watched", "Movies that I have watched.", "Movies", "", null));
                filmsObservable = mRepo.getMyMovies();
                break;
            case "pop":
                personObservable = Observable.just(new PersonPOJO("Popular", "Most popular movies on tmdb today.", "Movies", "", null));
                filmsObservable = mRepo.getPopularFilms();
                break;
            case "np":
                personObservable = Observable.just(new PersonPOJO("Now Showing", "Movies currently playing in theaters.", "Movies", "", null));
                filmsObservable = mRepo.getNowPlaying();
                break;
            case "tr":
                personObservable = Observable.just(new PersonPOJO("Top Rated", "Top-rated movies on tmdb.", "Movies", "", null));
                filmsObservable = mRepo.getTopRated();
                break;
            default:
                personObservable = mRepo.getFilmsByPerson(personId);
                filmsObservable = personObservable
                        .map(s -> new ArrayList<>(s.getMovieCredits().bothLists()));
        }
        return Observable.zip(personObservable, filmsObservable,
                FilmListDetails::new);
    }

    public Observable<FilmPOJO> getShowInfo(String showId) {
        return mRepo.getShow(showId);
    }

    public Observable<MediaQueryPOJO> queryMedia(String mediaQuery) {
        return mRepo.queryFilms(mediaQuery);
    }

    public Observable<PersonQueryPOJO> queryPopular() {
        return mRepo.getPopularActors();
    }

    public void addMovie(MyMovie movie, String personID) {
        mRepo.insertMovie(movie, personID);
    }

    public void removeMovie(String movieID, String personID) {
        mRepo.removeMovie(movieID, personID);
    }


    public void addList(MyList list) {
        mRepo.insertList(list);
    }

    public void removeList(String id) {
        mRepo.removeList(id);
    }

    public void updateList(int numberSeen, int numberOfMovies, String id) {
        mRepo.updateList(numberSeen, numberOfMovies, id);
    }

    public Single<MyMovie> checkIfMovieExists(String id) {
        return mRepo.checkIfMovieExists(id);
    }

    public Maybe<MyList> checkIfListExists(String id) {
        return mRepo.checkIfListExists(id);
    }

    public Flowable<List<MyMovie>> getMoviesWatched(List<String> ids) {
        return mRepo.getMoviesWatched(ids);
    }

    public Flowable<List<MyList>> getSavedLists() {
        return mRepo.getSavedLists();
    }
}
