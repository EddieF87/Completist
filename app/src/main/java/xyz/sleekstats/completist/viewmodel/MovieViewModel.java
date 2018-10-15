package xyz.sleekstats.completist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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
                personObservable = Observable.just(new PersonPOJO("Now Playing", "Movies currently playing in theaters.", "Movies", "", null));
                filmsObservable = mRepo.getNowPlaying();
                break;
            case "tr":
                personObservable = Observable.just(new PersonPOJO("Top Rated", "Top-rated movies on tmdb.", "Movies", "", null));
                filmsObservable = mRepo.getTopRated();
                break;
            default:
                personObservable = mRepo.getFilmsByPerson(personId);
                filmsObservable = personObservable
                        .map(s -> {
                            if (s.getKnown_for_department().equals("Directing")) {
                                return filterCrew(s.getMovieCredits().getCrew());
                            } else {
                                return s.getMovieCredits().getCast();
                            }
                        });

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

    private List<FilmByPerson> filterCrew(List<FilmByPerson> unfiltered) {
        List<FilmByPerson> filteredList = new ArrayList<>();
        for (FilmByPerson film : unfiltered) {
            if (film.getJob().equals("Director")) {
                filteredList.add(film);
            }
        }
        return filteredList;
    }

    public void addMovie(MyMovie movie) {
        mRepo.insertMovie(movie);
    }

    public void removeMovie(String id) {
        mRepo.removeMovie(id);
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
