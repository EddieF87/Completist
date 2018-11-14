package xyz.sleekstats.completist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.MovieCredits;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.model.WatchCount;
import xyz.sleekstats.completist.service.Repo;

public class MovieViewModel extends AndroidViewModel {

    private static final String TAG_RXERROR = "rxprob MovieVM";
    private static final String MOVIE_ID = "287";
    private final Repo mRepo;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final PublishSubject<PersonPOJO> personPublishSubject = PublishSubject.create();
    private final PublishSubject<List<FilmByPerson>> filmListPublishSubject = PublishSubject.create();
    private final PublishSubject<FilmPOJO> filmDetailsPublishSubject = PublishSubject.create();
    private final PublishSubject<WatchCount> watchCountPublishSubject = PublishSubject.create();
    private final PublishSubject<Integer> viewPagerSubject = PublishSubject.create();

    private FilmListDetails mFilmListDetails;
    private FilmPOJO mFilmDetails;
    private MovieCredits mMovieCredits;
    private int mTotalFilms;
    private int mWatchedFilms;
    private final List<FilmByPerson> displayList = new ArrayList<>();
    private int displayTotalFilms;
    private int displayWatchedFilms;
    private boolean initialSpin = true;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        mRepo = new Repo(application);
    }

    public void getMovieInfo(String movieId) {
        mCompositeDisposable.add(mRepo.getFilm(movieId)
                .subscribe(this::updateShowOrFilm,
                        e -> {
                            Log.e(TAG_RXERROR, "getMovieInfo mRepo.getFilm e = " + e.getMessage());
//                            updateShowOrFilm(new FilmPOJO());
                        })
        );
        viewPagerSubject.onNext(2);
    }

    public void getShowInfo(String movieId) {
        mCompositeDisposable.add(mRepo.getShow(movieId)
                .subscribe(this::updateShowOrFilm,
                        e -> {
                            Log.e(TAG_RXERROR, "mRepo.getShow e = " + e.getMessage());
//                            updateShowOrFilm(new FilmPOJO());
                        })
        );
        viewPagerSubject.onNext(2);
    }

    private void updateShowOrFilm(FilmPOJO filmPOJO) {
        checkForMovieFromDetails(filmPOJO);
    }

    public void getShowOrFilm() {
        initialSpin = true;
        if (mFilmDetails != null) {
            checkForMovieFromDetails(mFilmDetails);
        } else {
            mCompositeDisposable.add(
                    mRepo.getFilm(MOVIE_ID)
                            .subscribe(this::updateShowOrFilm,
                                    e -> {
                                        Log.e(TAG_RXERROR, "getShowOrFilm mRepo.getFilm = " + e.getMessage());
//                                        updateShowOrFilm(new FilmPOJO());
                                    })
            );
        }
    }

    public void getFilms() {
        if (mFilmListDetails == null) {
            getFilmsByPerson(MovieKeys.LIST_WATCHED);
        } else {
            filmListPublishSubject.onNext(displayList);
            updateWatchCount(displayWatchedFilms, displayTotalFilms);
            personPublishSubject.onNext(mFilmListDetails.getPersonPOJO());
        }
    }

    public void updateFilms(String personID) {
        getFilmsByPerson(personID);
        viewPagerSubject.onNext(1);
    }

    public void getFilmsByPerson(String personId) {

        Observable<PersonPOJO> personObservable;
        Observable<List<FilmByPerson>> filmsObservable;

        switch (personId) {
            case MovieKeys.LIST_WATCHED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("My Watched", "Movies that I have watched.", "Movies", "", personId));
                filmsObservable = mRepo.getMyWatchedMovies();
                break;
            case MovieKeys.LIST_QUEUED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("My Queued", "Movies that I have queued.", "Movies", "", personId));
                filmsObservable = mRepo.getMyQueuedMovies();
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
                                .subscribe(credits -> mMovieCredits = credits,
                                        e -> {
                                            Log.e(TAG_RXERROR, "movieCreditsObservable e: " + e.getMessage());
                                            mMovieCredits = new MovieCredits();
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
                        .subscribe(this::publishNewDetails,
                                e -> {
                                    Log.e(TAG_RXERROR, "movieCreditsObservable e: " + e.getMessage());
//                                    mMovieCredits = new MovieCredits();
                                })
        );
    }

    public PublishSubject<PersonPOJO> getPersonPublishSubject() {
        return personPublishSubject;
    }

    public PublishSubject<Integer> getViewPagerSubject() {
        return viewPagerSubject;
    }

    public PublishSubject<List<FilmByPerson>> getFilmListPublishSubject() {
        return filmListPublishSubject;
    }

    public PublishSubject<FilmPOJO> getFilmDetailsPublishSubject() {
        return filmDetailsPublishSubject;
    }

    public PublishSubject<WatchCount> getWatchCountPublishSubject() {
        return watchCountPublishSubject;
    }

    private void publishNewDetails(FilmListDetails details) {

        personPublishSubject.onNext(details.getPersonPOJO());

        mFilmListDetails = details;
        mTotalFilms = details.getFilmByPersonList().size();

        displayList.clear();
        displayList.addAll(mFilmListDetails.getFilmByPersonList());
        List<String> filmIDs = getFilmIDs(displayList);

        mCompositeDisposable.add(scanMoviesForWatched(filmIDs)
                .subscribe(watchedList ->
                        updateWatched(watchedList, displayList),
                        e -> Log.e(TAG_RXERROR, "scanMoviesForWatched e=" + e.getMessage()))
        );
    }

    private void updateWatched(Map<String, FilmByPerson> watchedFilms, List<FilmByPerson> totalFilms) {

        mWatchedFilms = 0;

        for (FilmByPerson film : totalFilms) {
            if (watchedFilms.containsKey(film.getId())) {
                FilmByPerson film1 = watchedFilms.get(film.getId());
                if (film1.isQueued()) {
                    film.setQueued(true);
                }
                if (film1.isWatched()) {
                    film.setWatched(true);
                    mWatchedFilms++;
                }
            }
        }

        String id = mFilmListDetails.getPersonPOJO().getId();
        mCompositeDisposable.add(Observable.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> updateList(mWatchedFilms, mTotalFilms, id),
                        e -> Log.e(TAG_RXERROR, "updateList e=" + e.getMessage()))
        );
        displayWatchedFilms = mWatchedFilms;
        displayTotalFilms = mTotalFilms;
        updateWatchCount(displayWatchedFilms, displayTotalFilms);
        filmListPublishSubject.onNext(totalFilms);
    }

    private void updateDisplayWatched(Map<String, FilmByPerson> watchedFilms, List<FilmByPerson> totalFilms) {

        displayWatchedFilms = 0;

        for (FilmByPerson film : totalFilms) {
            if (watchedFilms.containsKey(film.getId())) {
                FilmByPerson film1 = watchedFilms.get(film.getId());
                if (film1.isQueued()) {
                    film.setQueued(true);
                }
                if (film1.isWatched()) {
                    film.setWatched(true);
                    displayWatchedFilms++;
                }
            }
        }
        displayTotalFilms = totalFilms.size();

        updateWatchCount(displayWatchedFilms, displayTotalFilms);
        filmListPublishSubject.onNext(totalFilms);
    }

    private void updateWatchCount(int watched, int total) {
        watchCountPublishSubject.onNext(new WatchCount(watched, total));
    }

    public Observable<MediaQueryPOJO> queryMedia(String mediaQuery) {
        return mRepo.queryFilms(mediaQuery);
    }

    public Observable<PersonQueryPOJO> queryPopular() {
        return mRepo.getPopularActors();
    }

    private void addWatchedMovie(FilmByPerson movie) {
        mRepo.insertMovie(movie);
        updateWatchedMovieCount(movie.getId(), 1);
    }

    private void updateWatchedMovie(FilmByPerson movie) {
        mRepo.updateFilm(movie);
        if (movie.isWatched()) {
            updateWatchedMovieCount(movie.getId(), 1);
        } else {
            updateWatchedMovieCount(movie.getId(), -1);
        }
    }

    private void updateWatchedMovieCount(String filmID, int x) {
        mCompositeDisposable.add(
                movieInListObservable(filmID)
                        .subscribe(
                                inList -> {
                                    if (inList) {
                                        mWatchedFilms += x;
                                    }
                                },
                                e -> Log.e(TAG_RXERROR, "rrroh movieInListObservable" + e.getMessage())
                        )
        );
        displayWatchedFilms += x;
        updateWatchCount(displayWatchedFilms, displayTotalFilms);

    }

    private void removeWatchedMovie(String movieID) {
        mRepo.removeMovie(movieID);
        updateWatchedMovieCount(movieID, -1);
    }

    private Single<Boolean> movieInListObservable(String id) {
        return Observable.just(mFilmListDetails.getFilmByPersonList())
                .flatMapIterable(list -> list)
                .map(FilmByPerson::getId)
                .contains(id);
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
                .observeOn(Schedulers.io());
    }

    public Single<FilmByPerson> onMovieWatchedFromList(FilmByPerson film) {
        film.reverseWatched();
        return checkIfMovieExists(film)
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        addWatchedMovie(film);
                    } else {
                        if (film.isQueued()) {
                            updateWatchedMovie(film);
                        } else {
                            removeWatchedMovie(String.valueOf(film.getId()));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((x, y) -> {
                            if (detailsFilmInFilmList()) {
                                setQueuedAndWatched(film, mFilmDetails);
                            }
                        }
                );
    }

    public Single<FilmByPerson> onMovieQueuedFromList(FilmByPerson film) {
        film.reverseQueued();
        return checkIfMovieExists(film)
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        mRepo.insertQueuedMovie(film);
                    } else {
                        if (film.isWatched()) {
                            mRepo.updateQueuedFilm(film);
                        } else {
                            mRepo.removeQueuedMovie(String.valueOf(film.getId()));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((x, y) -> {
                            if (detailsFilmInFilmList()) {
                                setQueuedAndWatched(film, mFilmDetails);
                            }
                        }
                );
    }

    public FilmPOJO onMovieWatchedFromDetails(FilmPOJO filmPOJO) {
        if (mFilmDetails == null) {
            mFilmDetails = filmPOJO;
            if(mFilmDetails == null) {
                return new FilmPOJO();
            }
        }
        mFilmDetails.reverseWatched();
        FilmByPerson film = new FilmByPerson(mFilmDetails.getTitle(), mFilmDetails.getId(), mFilmDetails.getPoster_path());
        film.setWatched(mFilmDetails.isWatched());
        film.setQueued(mFilmDetails.isQueued());
        mCompositeDisposable.add(checkIfMovieExists(film)
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        mRepo.insertMovie(film);
                    } else {
                        if (film.isQueued()) {
                            mRepo.updateFilm(film);
                        } else {
                            mRepo.removeMovie(String.valueOf(film.getId()));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((x, y) -> {
                            if (detailsFilmInFilmList()) {
                                displayList.get(displayList.indexOf(film)).setWatched(film.isWatched());
                                filmListPublishSubject.onNext(displayList);
                                if (film.isWatched()) {
                                    updateWatchedMovieCount(film.getId(), 1);
                                } else {
                                    updateWatchedMovieCount(film.getId(), -1);
                                }
                            }
                        }
                )
        );
        return mFilmDetails;
    }

    public FilmPOJO onMovieQueuedFromDetails(FilmPOJO filmPOJO) {

        if (mFilmDetails == null) {
            mFilmDetails = filmPOJO;
            if(mFilmDetails == null) {
                return new FilmPOJO();
            }
        }
        mFilmDetails.reverseQueued();
        FilmByPerson film = new FilmByPerson(mFilmDetails.getTitle(), mFilmDetails.getId(), mFilmDetails.getPoster_path());
        film.setWatched(mFilmDetails.isWatched());
        film.setQueued(mFilmDetails.isQueued());
        mCompositeDisposable.add(checkIfMovieExists(film)
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        mRepo.insertQueuedMovie(film);
                    } else {
                        if (film.isWatched()) {
                            mRepo.updateQueuedFilm(film);
                        } else {
                            mRepo.removeQueuedMovie(String.valueOf(film.getId()));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((x, y) -> {
                    if (detailsFilmInFilmList()) {
                        displayList.get(displayList.indexOf(film)).setQueued(film.isQueued());
                        filmListPublishSubject.onNext(displayList);
                    }
                })
        );
        return mFilmDetails;
    }

    public Single<FilmByPerson> checkIfMovieExistsFromDetails(FilmByPerson film) {
        return mRepo.checkIfMovieExists(film.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnEvent((x, y) -> {
                    if (x == null) {
                        addWatchedMovie(film);
                    } else {
                        removeWatchedMovie(String.valueOf(film.getId()));
                    }
                });
    }

    public boolean detailsFilmInFilmList() {
        if (mFilmDetails == null) {
            return false;
        }
        List<String> filmIDs = getFilmIDs(displayList);
        return filmIDs.contains(mFilmDetails.getId());
    }

    private Single<FilmByPerson> checkMovie(FilmPOJO filmPOJO) {
        if (filmPOJO.getId() == null) {
            filmPOJO.setId("");
        }
        Log.d("rxprob", "checkMovie filmpojo = " + filmPOJO.getName() + filmPOJO.getId());

        return mRepo.checkIfMovieExists(filmPOJO.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(e -> {
                    Log.e(TAG_RXERROR, "checkMovie e = " + e.getMessage());
                });
    }


    private void checkForMovieFromDetails(FilmPOJO filmPOJO) {
        Log.d("rxprob", "checkForMovieFromDetails filmpojo = " + filmPOJO.getName() + filmPOJO.getId());
        mCompositeDisposable.add(
                checkMovie(filmPOJO)
                        .subscribe(
                                film -> setQueuedAndWatched(film, filmPOJO),
                                error -> publishFilmDetails(filmPOJO)
                        )
        );
    }

    private void setQueuedAndWatched(FilmByPerson film, FilmPOJO filmPOJO) {
        filmPOJO.setWatched(film.isWatched());
        filmPOJO.setQueued(film.isQueued());
        publishFilmDetails(filmPOJO);
    }

    private void publishFilmDetails(FilmPOJO filmPOJO) {
        mFilmDetails = filmPOJO;
        filmDetailsPublishSubject.onNext(mFilmDetails);
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

    private Single<List<FilmByPerson>> getAllMovies(List<String> ids) {
        return mRepo.getAllMovies(ids);
    }

    public Flowable<List<MyList>> getSavedLists() {
        return mRepo.getSavedLists();
    }

    public void onSpin(int pos) {

        if (initialSpin) {
            initialSpin = false;
            return;
        }

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
        displayList.clear();
        displayList.addAll(filmByPersonSet);

        List<String> filmIDs = getFilmIDs(displayList);

        mCompositeDisposable.add(
                scanMoviesForWatched(filmIDs)
                        .subscribe(watchedList -> updateDisplayWatched(watchedList, displayList),
                                e -> Log.e(TAG_RXERROR, "scanMoviesForWatched" + e.getMessage())
                        )
        );
    }

    private List<String> getFilmIDs(List<FilmByPerson> films) {
        List<String> filmIDs = new ArrayList<>();
        for (FilmByPerson film : films) {
            filmIDs.add(film.getId());
        }
        return filmIDs;
    }

    private Single<Map<String, FilmByPerson>> scanMoviesForWatched(List<String> filmIDs) {
        return getAllMovies(filmIDs)
                .subscribeOn(Schedulers.io())
                .flattenAsObservable(s -> s)
                .filter(film -> filmIDs.contains(film.getId()))
                .toMap(FilmByPerson::getId)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.clear();
    }
}
