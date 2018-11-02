package xyz.sleekstats.completist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.graphics.Movie;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.databinding.MovieKeys;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmListDetails;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.MovieCredits;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.model.WatchCount;
import xyz.sleekstats.completist.service.Repo;

public class MovieViewModel extends AndroidViewModel {

    private static final String TAG_RXERROR = "rxprob MovieVM";
    private final Repo mRepo;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private PublishSubject<PersonPOJO> personPublishSubject = PublishSubject.create();
    private PublishSubject<List<FilmByPerson>> filmListPublishSubject = PublishSubject.create();
    private PublishSubject<WatchCount> watchCountPublishSubject = PublishSubject.create();

    private FilmListDetails mFilmListDetails;
    private MovieCredits mMovieCredits;
    //    private List<FilmByPerson> mFilmList;
    private int mTotalFilms;
    private int mWatchedFilms;
    private List<FilmByPerson> displayList;
    private int displayTotalFilms;
    private int displayWatchedFilms;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        mRepo = new Repo(application);
    }

    public Observable<FilmPOJO> getMovieInfo(String movieId) {
        return mRepo.getFilm(movieId);
    }

    public void getFilmsByPerson(String personId) {

        Observable<PersonPOJO> personObservable;
        Observable<List<FilmByPerson>> filmsObservable;

        switch (personId) {
            case MovieKeys.LIST_QUEUED:
            case MovieKeys.LIST_WATCHED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("My Watched", "Movies that I have watched.", "Movies", "", personId));
                filmsObservable = mRepo.getMyMovies();
                break;
            case MovieKeys.LIST_POPULAR:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("Popular", "Most popular movies on tmdb today.", "Movies", "", personId));
                filmsObservable = mRepo.getPopularFilms();
                break;
            case MovieKeys.LIST_NOWPLAYING:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("Now Showing", "Movies currently playing in theaters.", "Movies", "", personId));
                filmsObservable = mRepo.getNowPlaying();
                break;
            case MovieKeys.LIST_TOPRATED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("Top Rated", "Top-rated movies on tmdb.", "Movies", "", personId));
                filmsObservable = mRepo.getTopRated();
                break;
            default:
                personObservable = mRepo.getFilmsByPerson(personId);
                Observable<MovieCredits> movieCreditsObservable = personObservable
                        .flatMap(s -> Observable.just(s.getMovieCredits()));
                mCompositeDisposable.add(
                        movieCreditsObservable
                                .doOnError(e -> Log.e(TAG_RXERROR, "Error: " + e.getMessage()))
                                .subscribe(s -> {
                                    mMovieCredits = s;
                                })
                );
                filmsObservable = personObservable
                        .map(s -> {
                            if (s.getKnown_for_department().equals("Directing") || s.getKnown_for_department().equals("Writing")) {
                                return new ArrayList<>(s.getMovieCredits().getCrew());
                            } else {
                                return new ArrayList<>(s.getMovieCredits().getCast());
                            }
                        });
        }
        mCompositeDisposable.add(
                Observable.zip(personObservable, filmsObservable, FilmListDetails::new)
                        .doOnError(e -> Log.e(TAG_RXERROR, "Error: " + e.getMessage()))
                        .subscribe(this::publishNewDetails)
        );
    }

    public PublishSubject<PersonPOJO> getPersonPublishSubject() {
        return personPublishSubject;
    }


    public PublishSubject<List<FilmByPerson>> getFilmListPublishSubject() {
        return filmListPublishSubject;
    }

    public PublishSubject<WatchCount> getWatchCountPublishSubject() {
        return watchCountPublishSubject;
    }

    private void publishNewDetails(FilmListDetails details) {

        personPublishSubject.onNext(details.getPersonPOJO());

        mFilmListDetails = details;
        mTotalFilms = details.getFilmByPersonList().size();

        List<String> filmIDs = getFilmIDs(details.getFilmByPersonList());

        mCompositeDisposable.add(scanMoviesForWatched(filmIDs)
                .subscribe(watchedList ->
                        updateWatched(watchedList, mFilmListDetails.getFilmByPersonList()))
        );
    }

    private void updateWatched(List<FilmByPerson> watchedFilms, List<FilmByPerson> totalFilms) {

        for (FilmByPerson film : totalFilms) {
            if (watchedFilms.contains(film)) {
                film.setWatchType(2);
            }
        }
        mWatchedFilms = watchedFilms.size();

        String id = mFilmListDetails.getPersonPOJO().getId();
        mCompositeDisposable.add(Observable.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> updateList(mWatchedFilms, mTotalFilms, id))
        );
        displayWatchedFilms = mWatchedFilms;
        displayTotalFilms = mTotalFilms;
        updateWatchCount(displayWatchedFilms, displayTotalFilms);
        filmListPublishSubject.onNext(totalFilms);
    }

    private void updateDisplayWatched(List<FilmByPerson> watchedFilms, List<FilmByPerson> totalFilms) {

        for (FilmByPerson film : totalFilms) {
            if (watchedFilms.contains(film)) {
                film.setWatchType(2);
            }
        }
        displayWatchedFilms = watchedFilms.size();
        displayTotalFilms = totalFilms.size();

        updateWatchCount(displayWatchedFilms, displayTotalFilms);
        filmListPublishSubject.onNext(totalFilms);
    }

    private void updateWatchCount(int watched, int total) {
        watchCountPublishSubject.onNext(new WatchCount(watched, total));
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

    private void addMovie(FilmByPerson movie, String personID) {
        mRepo.insertMovie(movie, personID);
        mCompositeDisposable.add(
                movieInListObservable(movie.getId())
                        .subscribe(inList -> { if (inList) { mWatchedFilms++; } })
        );
        displayWatchedFilms++;
        updateWatchCount(displayWatchedFilms, displayTotalFilms);
    }

    private void removeMovie(String movieID, String personID) {
        mRepo.removeMovie(movieID, personID);
        mCompositeDisposable.add(
                movieInListObservable(movieID)
                        .subscribe(inList -> { if (inList) { mWatchedFilms--; } })
        );
        displayWatchedFilms--;
        updateWatchCount(displayWatchedFilms, displayTotalFilms);
    }

    private Single<Boolean> movieInListObservable(String id) {
        return Observable.just(mFilmListDetails.getFilmByPersonList())
                .flatMapIterable(list -> list)
                .map(FilmByPerson::getId)
                .contains(id)
                .doOnError(e -> Log.e(TAG_RXERROR, "rrroh movieInListObservable" + e.getMessage()));
    }

    private void addList(MyList list) {
        mRepo.insertList(list);
    }

    private void removeList(String id) {
        mRepo.removeList(id);
    }

    private void updateList(int numberSeen, int numberOfMovies, String id) {
        mRepo.updateList(numberSeen, numberOfMovies, id);
    }

    public Single<FilmByPerson> checkIfMovieExists(FilmByPerson film) {
        return mRepo.checkIfMovieExists(film.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        addMovie(film, mFilmListDetails.getPersonPOJO().getId());
                    } else {
                        removeMovie(String.valueOf(film.getId()), mFilmListDetails.getPersonPOJO().getId());
                    }
                });
    }

    public Maybe<MyList> addOrRemoveList() {
        String id = mFilmListDetails.getPersonPOJO().getId();
        return mRepo.checkIfListExists(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        addList(new MyList(Integer.parseInt(id), mFilmListDetails.getPersonPOJO().getName(),
                                mWatchedFilms, mTotalFilms, mFilmListDetails.getPersonPOJO().getProfile_path()));
                    } else {
                        removeList(id);
                    }
                });
    }


    public Maybe<MyList> checkIfListExists(String id) {
        return mRepo.checkIfListExists(id);
    }

    public Single<List<FilmByPerson>> getMoviesWatched(List<String> ids) {
        return mRepo.getMoviesWatched(ids);
    }

    public Flowable<List<MyList>> getSavedLists() {
        return mRepo.getSavedLists();
    }

    public void onSpin(int pos) {

        if (mMovieCredits == null) {
            return;
        }

        Set<FilmByPerson> filmByPersonSet;
        switch (pos) {
            case 1:
                filmByPersonSet = mMovieCredits.getCast();
                break;
            case 2:
                filmByPersonSet = mMovieCredits.getCrew();
                break;
            default:
                filmByPersonSet = mMovieCredits.bothLists();
        }
        List<FilmByPerson> displayList = new ArrayList<>(filmByPersonSet);

        List<String> filmIDs = getFilmIDs(displayList);

        mCompositeDisposable.add(
                scanMoviesForWatched(filmIDs)
                        .subscribe(watchedList
                                -> updateDisplayWatched(watchedList, displayList))
        );
    }

    private List<String> getFilmIDs(List<FilmByPerson> films) {
        List<String> filmIDs = new ArrayList<>();
        for (FilmByPerson film : films) {
            filmIDs.add(film.getId());
        }
        return filmIDs;
    }

    private Single<List<FilmByPerson>> scanMoviesForWatched(List<String> filmIDs) {
        return getMoviesWatched(filmIDs)
                .subscribeOn(Schedulers.io())
                .flattenAsObservable(s -> s)
                .filter(film -> filmIDs.contains(film.getId()))
                .toList()
                .doOnError(e -> Log.e(TAG_RXERROR, "getMoviesWatched" + e.getMessage()))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.clear();
    }
}
