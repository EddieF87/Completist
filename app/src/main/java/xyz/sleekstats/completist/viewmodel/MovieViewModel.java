package xyz.sleekstats.completist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmListDetails;
import xyz.sleekstats.completist.model.FilmPOJO;
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
                personObservable = Observable.just(new PersonPOJO("My Watched", "", "Movies", "", null));
                filmsObservable = mRepo.getMyMovies();
                break;
            case "pop":
                personObservable = Observable.just(new PersonPOJO("Popular", "", "Movies", "", null));
                filmsObservable = mRepo.getPopularFilms();
                break;
            case "np":
                personObservable = Observable.just(new PersonPOJO("Now Playing", "", "Movies", "", null));
                filmsObservable = mRepo.getNowPlaying();
                break;
            case "tr":
                personObservable = Observable.just(new PersonPOJO("Top Rated", "", "Movies", "", null));
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
}
