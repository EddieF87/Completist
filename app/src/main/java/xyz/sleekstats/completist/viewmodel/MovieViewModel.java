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
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.GenreList;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.MovieCredits;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
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
    private final PublishSubject<List<MyList>> popularActorsSubject = PublishSubject.create();
    private final PublishSubject<Integer> viewPagerSubject = PublishSubject.create();
    private final PublishSubject<List<FilmByPerson>> rankingsSubject = PublishSubject.create();

    private FilmListDetails mFilmListDetails;
    private FilmPOJO mFilmDetails;
    private MovieCredits mMovieCredits;
    private int mTotalFilms;
    private int mWatchedFilms;
    private final List<FilmByPerson> displayList = new ArrayList<>();
    private final List<MyList> popularActorList = new ArrayList<>();
    private int displayTotalFilms;
    private int displayWatchedFilms;
    private boolean initialSpin = true;
    private int filmsPageNumber = 1;
    private int actorsPageNumber = 0;
    private boolean scrollLoading = false;
    private String genreID;
    private String tvOrMovieString;
    private boolean isFilmRankings = true;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        mRepo = new Repo(application);
    }

    public void getMovieInfo(String movieId) {
        mCompositeDisposable.add(mRepo.getFilm(movieId)
                .subscribe(this::updateFilm,
                        e -> Log.e(TAG_RXERROR, "getMovieInfo mRepo.getFilm e = " + e.getMessage()))
        );
        viewPagerSubject.onNext(2);
    }

    public void getShowInfo(String movieId) {
        mCompositeDisposable.add(mRepo.getShow(movieId)
                .subscribe(this::updateShow,
                        e -> Log.e(TAG_RXERROR, "mRepo.getShow e = " + e.getMessage()))
        );
        viewPagerSubject.onNext(2);
    }

    private void updateShow(FilmPOJO filmPOJO) {
        filmPOJO.setIsFilm(false);
        checkForMovieFromDetails(filmPOJO);
    }

    private void updateFilm(FilmPOJO filmPOJO) {
        filmPOJO.setIsFilm(true);
        checkForMovieFromDetails(filmPOJO);
    }

    public void getShowOrFilm() {
        if (mFilmDetails != null) {
            checkForMovieFromDetails(mFilmDetails);
        } else {
            mCompositeDisposable.add(
                    mRepo.getFilm(MOVIE_ID)
                            .subscribe(this::updateFilm,
                                    e -> Log.e(TAG_RXERROR, "getShowOrFilm mRepo.getFilm = " + e.getMessage()))
            );
        }
    }

    public void getFilms() {
        finishScrollLoading();
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

    public void getFilmsByPerson(String personID) {

        filmsPageNumber = 1;
        Observable<PersonPOJO> personObservable;
        Observable<List<FilmByPerson>> filmsObservable;

        switch (personID) {
            case MovieKeys.LIST_WATCHED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("My Watched", "Movies that I have watched.", "Movies", "", personID));
                filmsObservable = mRepo.getMyWatchedMovies();
                break;
            case MovieKeys.LIST_QUEUED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("My Queued", "Movies that I have queued.", "Movies", "", personID));
                filmsObservable = mRepo.getMyQueuedMovies();
                break;
            case MovieKeys.LIST_POPULAR:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("Popular", "Most popular movies on tmdb today.", "Movies", "", personID));
                filmsObservable = setAsFilm(mRepo.getPopularFilms(filmsPageNumber));
                break;
            case MovieKeys.LIST_NOWPLAYING:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("Now Showing", "Movies currently playing in theaters.", "Movies", "", personID));
                filmsObservable = setAsFilm(mRepo.getNowPlaying(filmsPageNumber));
                break;
            case MovieKeys.LIST_TOPRATED:
                mMovieCredits = null;
                personObservable = Observable.just(new PersonPOJO("Top Rated", "Top-rated movies on tmdb.", "Movies", "", personID));
                filmsObservable = setAsFilm(mRepo.getTopRated(filmsPageNumber));
                break;
            default:
                personObservable = mRepo.getFilmsByPerson(personID);
                Observable<MovieCredits> movieCreditsObservable = personObservable
                        .flatMap(s -> Observable.just(s.getMovieCredits()));
                mCompositeDisposable.add(
                        movieCreditsObservable
                                .subscribe(credits -> mMovieCredits = credits,
                                        e -> Log.e(TAG_RXERROR, "movieCreditsObservable e: " + e.getMessage()))
                );
                filmsObservable = setAsFilm(personObservable
                        .map(s -> s.getKnown_for_department().equals("Directing") || s.getKnown_for_department().equals("Writing")
                                ? new ArrayList<>(s.getMovieCredits().getCrew()) : new ArrayList<>(s.getMovieCredits().getCast())
                        ));
        }
        mCompositeDisposable.add(
                Observable.zip(personObservable, filmsObservable, FilmListDetails::new)
                        .subscribe(this::publishNewDetails,
                                e -> Log.e(TAG_RXERROR, "movieCreditsObservable e: " + e.getMessage()))
        );
    }

    public void getFilmsByGenre(Genre genre, boolean tvOrMovie) {
        filmsPageNumber = 1;
        tvOrMovieString = tvOrMovie ? "movie" : "tv";
        genreID = genre.getId();
        Observable<PersonPOJO> personObservable = Observable.just(
                new PersonPOJO(genre.getName() + " (" + tvOrMovieString.substring(0, 1).toUpperCase() + tvOrMovieString.substring(1) + ")",
                        "", "Movies", "", MovieKeys.LIST_GENRE));
        Observable<List<FilmByPerson>> filmsObservable
                = setAsFilm(mRepo.getMoviesByGenre(tvOrMovieString, genreID, filmsPageNumber));

        mCompositeDisposable.add(
                Observable.zip(personObservable, filmsObservable, FilmListDetails::new)
                        .subscribe(this::publishNewDetails,
                                e -> Log.e(TAG_RXERROR, "getFilmsByGenre e: " + e.getMessage()))
        );
        viewPagerSubject.onNext(1);
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

    public PublishSubject<List<FilmByPerson>> getRankingsSubject() {
        return rankingsSubject;
    }

    public PublishSubject<FilmPOJO> getFilmDetailsPublishSubject() {
        return filmDetailsPublishSubject;
    }

    public PublishSubject<WatchCount> getWatchCountPublishSubject() {
        return watchCountPublishSubject;
    }

    public PublishSubject<List<MyList>> getPopularActorsSubject() {
        return popularActorsSubject;
    }

    private void publishNewDetails(FilmListDetails details) {

        personPublishSubject.onNext(details.getPersonPOJO());

        mFilmListDetails = details;

        List<FilmByPerson> films = details.getFilmByPersonList();
        mTotalFilms = films.size();

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
        return mRepo.queryMedia(mediaQuery);
    }

    public Observable<MediaQueryPOJO> queryRankings(String mediaQuery) {
        return mRepo.queryForRankings(mediaQuery, isFilmRankings);
    }

    public void getMediaForRankings(String mediaId) {
        if (isFilmRankings) {
            mCompositeDisposable.add(mRepo.getFilm(mediaId)
                    .subscribe(filmPOJO -> addRankedMovie(filmPOJO, true),
                            e -> Log.e(TAG_RXERROR, "getMediaForRankings mRepo.getFilm e = " + e.getMessage()))
            );
        } else {
            mCompositeDisposable.add(mRepo.getShow(mediaId)
                    .subscribe(filmPOJO -> addRankedMovie(filmPOJO, false),
                            e -> Log.e(TAG_RXERROR, "getMediaForRankings mRepo.getShow e = " + e.getMessage()))
            );
        }
    }

    public void getPopularActors() {
        if (popularActorList.isEmpty()) {
            finishScrollLoading();
            queryPopularActors();
        } else {
            popularActorsSubject.onNext(popularActorList);
        }
    }

    public void queryPopularActors() {
        if (scrollLoading) {
            return;
        }
        scrollLoading = true;
        actorsPageNumber++;
        mCompositeDisposable.add(mRepo.getPopularActors(actorsPageNumber)
                .flatMap(personQueryPOJO -> {
                            List<PersonPOJO> personPOJOList = personQueryPOJO.getResults();
                            List<MyList> myLists = new ArrayList<>();
                            for (PersonPOJO personPOJO : personPOJOList) {
                                myLists.add(new MyList(Integer.parseInt(personPOJO.getId()),
                                        personPOJO.getName(), -1, 1, personPOJO.getProfile_path()));
                            }
                            return Observable.just(myLists);
                        }
                )
                .subscribe(this::publishPopularActors,
                        e -> {
                            Log.e(TAG_RXERROR, "getPopularActors " + e.getMessage());
                            actorsPageNumber++;
                            finishScrollLoading();
                        }
                )
        );
    }

    private void publishPopularActors(List<MyList> actors) {
        popularActorList.addAll(actors);
        popularActorsSubject.onNext(popularActorList);
    }

    private void addWatchedMovie(FilmByPerson movie) {
        movie.setRanking(-1);
        mCompositeDisposable.add(mRepo.insertMovie(movie));
        updateWatchedMovieCount(movie.getId(), 1);
        publishRankedMovies();
    }

    private void addRankedMovie(FilmPOJO filmPOJO, boolean isFilm) {

        FilmByPerson movie = new FilmByPerson(filmPOJO.getTitle(), filmPOJO.getId(), filmPOJO.getPoster_path());
        movie.setRanking(-1);
        movie.setIsFilm(isFilm);

        mCompositeDisposable.add(checkIfMovieExists(movie)
                .subscribe(x -> {
                            if (x == null) {
                                mRepo.insertMovie(movie);
                                publishRankedMovies();
                            }
                        },
                        e -> {
                            mRepo.insertMovie(movie);
                            publishRankedMovies();
                        }
                )
        );
    }

    private void updateWatchedMovie(FilmByPerson movie) {
        mCompositeDisposable.add(mRepo.updateFilm(movie));
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
                                e -> Log.e(TAG_RXERROR, "movieInListObservable" + e.getMessage())
                        )
        );
        displayWatchedFilms += x;
        updateWatchCount(displayWatchedFilms, displayTotalFilms);

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

    private Single<FilmByPerson> checkIfMovieExists(FilmByPerson film) {
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
                        updateWatchedMovie(film);
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
                        film.setRanking(-1);
                        mRepo.insertQueuedMovie(film);
                        publishRankedMovies();
                    } else {
                        mRepo.updateQueuedFilm(film);
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
            if (mFilmDetails == null) {
                return new FilmPOJO();
            }
        }
        mFilmDetails.reverseWatched();
        FilmByPerson film = new FilmByPerson(mFilmDetails.getTitle(), mFilmDetails.getId(), mFilmDetails.getPoster_path());
        film.setWatched(mFilmDetails.isWatched());
        film.setQueued(mFilmDetails.isQueued());
        film.setIsFilm(mFilmDetails.isFilm());
        mCompositeDisposable.add(checkIfMovieExists(film)
                        .doOnEvent((x, y) -> {
                            if (x == null) {
                                film.setRanking(-1);
                                mCompositeDisposable.add(mRepo.insertMovie(film));
                                publishRankedMovies();
                            } else {
                                mCompositeDisposable.add(mRepo.updateFilm(film));
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
            if (mFilmDetails == null) {
                return new FilmPOJO();
            }
        }
        mFilmDetails.reverseQueued();
        FilmByPerson film = new FilmByPerson(mFilmDetails.getTitle(), mFilmDetails.getId(), mFilmDetails.getPoster_path());
        film.setWatched(mFilmDetails.isWatched());
        film.setQueued(mFilmDetails.isQueued());
        film.setIsFilm(mFilmDetails.isFilm());
        mCompositeDisposable.add(checkIfMovieExists(film)
                        .doOnEvent((x, y) -> {
                            if (x == null) {
                                film.setRanking(-1);
                                mRepo.insertQueuedMovie(film);
                                publishRankedMovies();
                            } else {
                                mRepo.updateQueuedFilm(film);
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

    private boolean detailsFilmInFilmList() {
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

        return mRepo.checkIfMovieExists(filmPOJO.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    private void checkForMovieFromDetails(FilmPOJO filmPOJO) {
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

    public Single<GenreList> getGenreList(boolean tvOrMovie) {
        String tvOrMovieString = tvOrMovie ? "movie" : "tv";
        return mRepo.getGenreList(tvOrMovieString);
    }

    public Maybe<MyList> checkIfListExists(String id) {
        return mRepo.checkIfListExists(id);
    }

    private Single<List<FilmByPerson>> getAllMovies(List<String> ids) {
        return mRepo.getAllMovies(ids);
    }

    public Flowable<List<MyList>> getSavedLists() {
        return mRepo.getSavedLists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void setInitialSpin(boolean initial) {
        initialSpin = initial;
    }

    public void onSpin(int pos) {

        if (initialSpin) {
            setInitialSpin(false);
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
            case 3:
                filmByPersonSet = mFilmListDetails.getPersonPOJO().getTvCredits().bothLists();
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

    private Observable<List<FilmByPerson>> setAsFilm(Observable<List<FilmByPerson>> listObservable) {
        return listObservable
                .flatMap(list -> {
                    for (FilmByPerson film : list) {
                        film.setIsFilm(true);
                    }
                    return Observable.just(list);
                });
    }

    public void finishScrollLoading() {
        scrollLoading = false;
    }

    public void onScrollEnd() {
        if (scrollLoading) {
            return;
        }
        scrollLoading = true;
        filmsPageNumber++;

        Observable<List<FilmByPerson>> filmsObservable;
        switch (mFilmListDetails.getPersonPOJO().getId()) {
            case MovieKeys.LIST_POPULAR:
                filmsObservable = setAsFilm(mRepo.getPopularFilms(filmsPageNumber));
                break;
            case MovieKeys.LIST_NOWPLAYING:
                filmsObservable = setAsFilm(mRepo.getNowPlaying(filmsPageNumber));
                break;
            case MovieKeys.LIST_TOPRATED:
                filmsObservable = setAsFilm(mRepo.getTopRated(filmsPageNumber));
                break;
            case MovieKeys.LIST_GENRE:
                filmsObservable = setAsFilm(mRepo.getMoviesByGenre(tvOrMovieString, genreID, filmsPageNumber));
                break;
            default:
                return;
        }

        mCompositeDisposable.add(filmsObservable
                .subscribe(filmByPeople -> {
                            displayList.addAll(filmByPeople);
                            List<String> filmIDs = getFilmIDs(displayList);
                            mTotalFilms = displayList.size();

                            mCompositeDisposable.add(scanMoviesForWatched(filmIDs)
                                    .subscribe(watchedList -> updateWatched(watchedList, displayList),
                                            e -> {
                                                Log.e(TAG_RXERROR, "scanMoviesForWatched e=" + e.getMessage());
                                                filmsPageNumber--;
                                                finishScrollLoading();
                                            })
                            );
                        }, e -> {
                            Log.e(TAG_RXERROR, "onScrollEnd e=" + e.getMessage());
                            filmsPageNumber--;
                            finishScrollLoading();
                        }
                )
        );
    }

    public void setFilmShowRankings(boolean isFilm) {
        isFilmRankings = isFilm;
    }

    public boolean isFilmRankings() {
        return isFilmRankings;
    }

    public void publishRankedMovies() {
        mCompositeDisposable.add(mRepo.getSavedForRankings(isFilmRankings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rankingsSubject::onNext,
                        e -> Log.e(TAG_RXERROR, "getSavedForRankings e=" + e.getMessage())
                )
        );
    }


    public void updateRankingNew(FilmByPerson movie, int newRank) {
        mCompositeDisposable.add(checkIfMovieExists(movie)
                .subscribe(x -> {
                            if (x == null) {
                                mRepo.insertMovie(movie);
                            }
                            mCompositeDisposable.add(mRepo.updateRankingNew(movie.getId(), newRank, isFilmRankings));
                        },
                        e -> {
                            mRepo.insertMovie(movie);
                            mCompositeDisposable.add(mRepo.updateRankingNew(movie.getId(), newRank, isFilmRankings));
                        }
                )
        );
    }

    public void updateRankingRemove(String id, int oldRank) {
        mCompositeDisposable.add(mRepo.updateRankingRemove(id, oldRank, isFilmRankings));
    }

    public void updateRankingUp(String id, int oldRank, int newRank) {
        mCompositeDisposable.add(mRepo.updateRankingUp(id, oldRank, newRank, isFilmRankings));
    }

    public void updateRankingDown(String id, int oldRank, int newRank) {
        mCompositeDisposable.add(mRepo.updateRankingDown(id, oldRank, newRank, isFilmRankings));
    }

    public Observable<List<FilmByPerson>> getPopularForRankings() {
        return isFilmRankings ? setAsFilm(mRepo.getPopularFilms(1)) : mRepo.getPopularShows(1);
    }

    public Single<List<FilmByPerson>> getWatchedForRankings() {
        return mRepo.getSavedForRankings(isFilmRankings);
    }
}
