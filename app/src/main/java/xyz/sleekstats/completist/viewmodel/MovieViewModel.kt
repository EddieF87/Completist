package xyz.sleekstats.completist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import xyz.sleekstats.completist.databinding.MovieKeys
import xyz.sleekstats.completist.model.*
import xyz.sleekstats.completist.service.Repo
import java.util.*

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepo = Repo(application)
    private val mCompositeDisposable = CompositeDisposable()

    val popularForRankings: Observable<List<MediaByPerson>>
        get() = (if (isFilmRankings) mRepo.getPopularFilms(1) else mRepo.getPopularShows(1))
                .flatMap { mediaByPeople -> Observable.just(ArrayList(mediaByPeople)) }

    val watchedForRankings: Single<List<MediaByPerson>>
        get() = mRepo.getSavedForRankings(isFilmRankings)

    var personPublishSubject = PublishSubject.create<PersonPOJO>()
    var watchCountPublishSubject = PublishSubject.create<WatchCount>()
    var filmListPublishSubject = PublishSubject.create<List<MediaByPerson>>()
    var filmDetailsPublishSubject = PublishSubject.create<MediaPOJO>()

    val popularActorsSubject = PublishSubject.create<List<MyList>>()
    val viewPagerSubject = PublishSubject.create<Int>()
    val rankingsSubject = PublishSubject.create<List<MediaByPerson>>()

    private var mFilmListDetails: FilmListDetails? = null
    private var mFilmDetails: MediaPOJO? = null
    private var mMovieCredits: MovieCredits? = null
    private var mTotalFilms = 0
    private var mWatchedFilms = 0
    private val displayList = mutableListOf<MediaByPerson>()
    private val popularActorList = mutableListOf<MyList>()
    private var displayTotalFilms = 0
    private var displayWatchedFilms = 0
    private var initialSpin = true
    private var filmsPageNumber = 1
    private var actorsPageNumber = 0
    private var scrollLoading = false
    private var genreID: String = ""
    private var tvOrMovieString: String = ""
    var isFilmRankings = true
        private set

    fun getMovieInfo(movieId: String?) {
        mCompositeDisposable.add(mRepo.getFilm(movieId)
                .subscribe({ filmPOJO: FilmPOJO -> updateFilm(filmPOJO) }
                ) { e: Throwable -> Log.e(TAG_RXERROR, "getMovieInfo mRepo.getFilm e = ${e.message}") }
        )
        viewPagerSubject.onNext(2)
    }

    fun getShowInfo(movieId: String?) {
        mCompositeDisposable.add(mRepo.getShow(movieId)
                .subscribe({ showPOJO: ShowPOJO -> updateShow(showPOJO) }
                ) { e: Throwable -> Log.e(TAG_RXERROR, "mRepo.getShow e = ${e.message}") }
        )
        viewPagerSubject.onNext(2)
    }

    private fun updateShow(showPOJO: ShowPOJO) {
        checkForMovieFromDetails(showPOJO)
    }

    private fun updateFilm(filmPOJO: FilmPOJO) {
        checkForMovieFromDetails(filmPOJO)
    }

    fun showOrFilm() {
        if (mFilmDetails != null) {
            checkForMovieFromDetails(mFilmDetails ?: MediaPOJO())
        } else {
            mCompositeDisposable.add(
                    mRepo.getFilm(MOVIE_ID)
                            .subscribe({ filmPOJO: FilmPOJO -> updateFilm(filmPOJO) }
                            ) { e: Throwable -> Log.e(TAG_RXERROR, "getShowOrFilm mRepo.getFilm = ${e.message}") }
            )
        }
    }

    val films: Unit
        get() {
            finishScrollLoading()
            if (mFilmListDetails == null) {
                getFilmsByPerson(MovieKeys.LIST_WATCHED)
            } else {
                filmListPublishSubject.onNext(displayList)
                updateWatchCount(displayWatchedFilms, displayTotalFilms)
                personPublishSubject.onNext(mFilmListDetails!!.personPOJO)
            }
        }

    fun updateFilms(personID: String?) {
        getFilmsByPerson(personID)
        viewPagerSubject.onNext(1)
    }

    fun getFilmsByPerson(personID: String?) {
        filmsPageNumber = 1
        val personObservable: Observable<PersonPOJO>
        val filmsObservable: Observable<List<MediaByPerson>>
        when (personID) {
            MovieKeys.LIST_WATCHED -> {
                mMovieCredits = null
                personObservable = Observable.just(PersonPOJO("My Watched", "Movies that I've watched.", "Movies", "", personID))
                filmsObservable = mRepo.myWatchedMovies
            }
            MovieKeys.LIST_QUEUED -> {
                mMovieCredits = null
                personObservable = Observable.just(PersonPOJO("My Queued", "Movies that I'm planning to watch.", "Movies", "", personID))
                filmsObservable = mRepo.myQueuedMovies
            }
            MovieKeys.LIST_POPULAR -> {
                mMovieCredits = null
                personObservable = Observable.just(PersonPOJO("Popular", "Most popular movies on TMDB today.", "Movies", "", personID))
                filmsObservable = mRepo.getPopularFilms(filmsPageNumber)
                        .flatMap { filmByPeople -> Observable.just(ArrayList<MediaByPerson>(filmByPeople)) }
            }
            MovieKeys.LIST_NOWPLAYING -> {
                mMovieCredits = null
                personObservable = Observable.just(PersonPOJO("Now Showing", "Movies currently playing in theaters.", "Movies", "", personID))
                filmsObservable = mRepo.getNowPlaying(filmsPageNumber)
                        .flatMap { filmByPeople -> Observable.just(ArrayList<MediaByPerson>(filmByPeople)) }
            }
            MovieKeys.LIST_TOPRATED -> {
                mMovieCredits = null
                personObservable = Observable.just(PersonPOJO("Top Rated", "Top-rated movies on TMDB.", "Movies", "", personID))
                filmsObservable = mRepo.getTopRated(filmsPageNumber)
                        .flatMap { filmByPeople -> Observable.just(ArrayList<MediaByPerson>(filmByPeople)) }
            }
            else -> {
                personObservable = mRepo.getFilmsByPerson(personID)
                val movieCreditsObservable = personObservable
                        .flatMap { s: PersonPOJO -> Observable.just(s.movieCredits) }
                mCompositeDisposable.add(
                        movieCreditsObservable
                                .subscribe({ credits: MovieCredits? -> mMovieCredits = credits }
                                ) { e: Throwable -> Log.e(TAG_RXERROR, "movieCreditsObservable e: " + e.message) }
                )
                filmsObservable = personObservable
                        .map { s: PersonPOJO ->
                            if (s.known_for_department == "Directing" || s.known_for_department == "Writing") ArrayList<MediaByPerson>(s.movieCredits?.crew
                                    ?: emptySet()) else ArrayList<MediaByPerson>(s.movieCredits?.cast
                                    ?: emptySet())
                        }
            }
        }
        mCompositeDisposable.add(
                Observable.zip(personObservable, filmsObservable, BiFunction { personPOJO: PersonPOJO, mediaByPersonList: List<MediaByPerson>? ->
                    FilmListDetails(personPOJO, mediaByPersonList ?: emptyList())
                })
                        .subscribe({ details: FilmListDetails -> publishNewDetails(details) }
                        ) { e: Throwable -> Log.e(TAG_RXERROR, "movieCreditsObservable e: " + e.message) }
        )
    }

    fun getFilmsByGenre(genre: Genre, tvOrMovie: Boolean) {
        filmsPageNumber = 1
        tvOrMovieString = if (tvOrMovie) "movie" else "tv"
        genreID = genre.id
        val personObservable = Observable.just(
                PersonPOJO("${genre.name} (${tvOrMovieString.substring(0, 1).toUpperCase(Locale.ENGLISH)}${tvOrMovieString.substring(1)})",
                        "", "Movies", "", MovieKeys.LIST_GENRE))
        val filmsObservable: Observable<List<MediaByPerson>> = mRepo.getMoviesByGenre(tvOrMovieString, genreID, filmsPageNumber)
                .flatMap { filmByPeople -> Observable.just(ArrayList<MediaByPerson>(filmByPeople)) }
        mCompositeDisposable.add(
                Observable.zip(personObservable, filmsObservable, BiFunction { personPOJO: PersonPOJO, mediaByPersonList: List<MediaByPerson>? ->
                    FilmListDetails(personPOJO, mediaByPersonList ?: emptyList())
                })
                        .subscribe({ details: FilmListDetails -> publishNewDetails(details) }
                        ) { e: Throwable -> Log.e(TAG_RXERROR, "getFilmsByGenre e: ${e.message}") }
        )
        viewPagerSubject.onNext(1)
    }

    private fun publishNewDetails(details: FilmListDetails) {
        personPublishSubject.onNext(details.personPOJO)
        mFilmListDetails = details
        mTotalFilms = details.mediaByPersonList.size

        displayList.clear()
        displayList.addAll(mFilmListDetails!!.mediaByPersonList)
        val filmIDs = getFilmIDs(displayList)

        mCompositeDisposable.add(scanMoviesForWatched(filmIDs)
                .subscribe({ watchedList: Map<String, MediaByPerson> ->
                    updateWatched(watchedList, displayList)
                }) { e: Throwable -> Log.e(TAG_RXERROR, "scanMoviesForWatched e: ${e.message}") }
        )
    }

    private fun updateWatched(watchedFilms: Map<String, MediaByPerson>, totalFilms: List<MediaByPerson>) {
        Log.d("fata", "updateWatched")
        mWatchedFilms = 0
        for (film in totalFilms) {
            if (watchedFilms.containsKey(film.id)) {
                val film1 = watchedFilms[film.id]
                if (film1!!.isQueued) {
                    film.isQueued = true
                }
                if (film1.isWatched) {
                    film.isWatched = true
                    mWatchedFilms++
                }
            }
        }
        val id = mFilmListDetails!!.personPOJO.id
        mCompositeDisposable.add(Observable.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe({ updateList(mWatchedFilms, mTotalFilms, id) }
                ) { e: Throwable -> Log.e(TAG_RXERROR, "updateList e=" + e.message) }
        )
        displayWatchedFilms = mWatchedFilms
        displayTotalFilms = mTotalFilms
        updateWatchCount(displayWatchedFilms, displayTotalFilms)
        filmListPublishSubject.onNext(totalFilms)
    }

    private fun updateDisplayWatched(watchedFilms: Map<String, MediaByPerson>, totalFilms: List<MediaByPerson>) {
        displayWatchedFilms = 0
        for (film in totalFilms) {
            if (watchedFilms.containsKey(film.id)) {
                val film1 = watchedFilms[film.id]
                if (film1!!.isQueued) {
                    film.isQueued = true
                }
                if (film1.isWatched) {
                    film.isWatched = true
                    displayWatchedFilms++
                }
            }
        }
        displayTotalFilms = totalFilms.size
        updateWatchCount(displayWatchedFilms, displayTotalFilms)
        filmListPublishSubject.onNext(totalFilms)
    }

    private fun updateWatchCount(watched: Int, total: Int) {
        watchCountPublishSubject.onNext(WatchCount(watched, total))
    }

    fun queryMedia(mediaQuery: String?): Observable<MediaQueryPOJO> {
        return mRepo.queryMedia(mediaQuery)
    }

    fun queryRankings(mediaQuery: String?): Observable<MediaQueryPOJO> {
        return mRepo.queryForRankings(mediaQuery, isFilmRankings)
    }

    fun getMediaForRankings(mediaId: String?) {
        if (isFilmRankings) {
            mCompositeDisposable.add(mRepo.getFilm(mediaId)
                    .subscribe({ mediaPOJO: FilmPOJO -> addRankedMovie(mediaPOJO) }
                    ) { e: Throwable -> Log.e(TAG_RXERROR, "getMediaForRankings mRepo.getFilm e = ${e.message}") }
            )
        } else {
            mCompositeDisposable.add(mRepo.getShow(mediaId)
                    .subscribe({ mediaPOJO: ShowPOJO -> addRankedMovie(mediaPOJO) }
                    ) { e: Throwable -> Log.e(TAG_RXERROR, "getMediaForRankings mRepo.getShow e = ${e.message}") }
            )
        }
    }

    val popularActors: Unit
        get() {
            if (popularActorList.isEmpty()) {
                finishScrollLoading()
                queryPopularActors()
            } else {
                popularActorsSubject.onNext(popularActorList)
            }
        }

    fun queryPopularActors() {
        if (scrollLoading) {
            return
        }
        scrollLoading = true
        actorsPageNumber++
        mCompositeDisposable.add(mRepo.getPopularActors(actorsPageNumber)
                .flatMap { personQueryPOJO: PersonQueryPOJO ->
                    val personPOJOList = personQueryPOJO.results ?: emptyList()
                    val myLists: MutableList<MyList> = ArrayList()
                    personPOJOList.forEach { personPOJO ->
                        myLists.add(MyList(personPOJO.id.toInt(),
                                personPOJO.name, -1, 1, personPOJO.profile_path))
                    }
                    Observable.just<List<MyList>>(myLists)
                }
                .subscribe({ actors: List<MyList> -> publishPopularActors(actors) }
                ) { e: Throwable ->
                    Log.e(TAG_RXERROR, "getPopularActors ${e.message}")
                    actorsPageNumber++
                    finishScrollLoading()
                }
        )
    }

    private fun publishPopularActors(actors: List<MyList>) {
        popularActorList.addAll(actors)
        popularActorsSubject.onNext(popularActorList)
    }

    private fun addWatchedMovie(movie: MediaByPerson) {
        movie.ranking = -1
        mCompositeDisposable.add(mRepo.insertMovie(movie))
        updateWatchedMovieCount(movie.id, 1)
        publishRankedMovies()
    }

    private fun addRankedMovie(mediaPOJO: MediaPOJO) {
        val movie = MediaByPerson(mediaPOJO.title, mediaPOJO.id, mediaPOJO.poster_path).also {
            it.isFilm = mediaPOJO.isFilm
        }
        mCompositeDisposable.add(checkIfMovieExists(movie)
                .subscribe({ x: MediaByPerson? ->
                    if (x == null) {
                        mRepo.insertMovie(movie)
                        publishRankedMovies()
                    }
                }
                ) {
                    mRepo.insertMovie(movie)
                    publishRankedMovies()
                }
        )
    }

    private fun updateWatchedMovie(movie: MediaByPerson) {
        mCompositeDisposable.add(mRepo.updateFilm(movie))
        if (movie.isWatched) {
            updateWatchedMovieCount(movie.id, 1)
        } else {
            updateWatchedMovieCount(movie.id, -1)
        }
    }

    private fun updateWatchedMovieCount(filmID: String, x: Int) {
        mCompositeDisposable.add(
                movieInListObservable(filmID)
                        .subscribe(
                                { inList: Boolean ->
                                    if (inList) {
                                        mWatchedFilms += x
                                    }
                                }
                        ) { e: Throwable -> Log.e(TAG_RXERROR, "movieInListObservable${e.message}") }
        )
        displayWatchedFilms += x
        updateWatchCount(displayWatchedFilms, displayTotalFilms)
    }

    private fun movieInListObservable(id: String): Single<Boolean> =
            Observable.just(mFilmListDetails!!.mediaByPersonList)
                    .flatMapIterable { list: List<MediaByPerson>? -> list }
                    .map { obj: MediaByPerson -> obj.id }
                    .contains(id)

    private fun addList(list: MyList) = mRepo.insertList(list)

    private fun removeList(id: String) = mRepo.removeList(id)

    private fun updateList(numberSeen: Int, numberOfMovies: Int, id: String) =
            mRepo.updateList(numberSeen, numberOfMovies, id)

    private fun checkIfMovieExists(film: MediaByPerson): Single<MediaByPerson> =
            mRepo.checkIfMovieExists(film.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())

    fun onMovieWatchedFromList(film: MediaByPerson): Single<MediaByPerson> {
        film.reverseWatched()
        return checkIfMovieExists(film)
                .doOnEvent { x: MediaByPerson?, _: Throwable? ->
                    if (x == null) {
                        addWatchedMovie(film)
                    } else {
                        updateWatchedMovie(film)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent { _, _ ->
                    if (detailsFilmInFilmList()) {
                        setQueuedAndWatched(film, mFilmDetails ?: MediaPOJO())
                    }
                }
    }

    fun onMovieQueuedFromList(film: MediaByPerson): Single<MediaByPerson> {
        film.reverseQueued()
        return checkIfMovieExists(film)
                .doOnEvent { x, _ ->
                    if (x == null) {
                        film.ranking = -1
                        mRepo.insertQueuedMovie(film)
                        publishRankedMovies()
                    } else {
                        mRepo.updateQueuedFilm(film)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent { _, _ ->
                    if (detailsFilmInFilmList()) {
                        setQueuedAndWatched(film, mFilmDetails ?: MediaPOJO())
                    }
                }
    }

    fun onMovieWatchedFromDetails(mediaPOJO: MediaPOJO?): MediaPOJO {
        if (mFilmDetails == null) {
            mFilmDetails = mediaPOJO
            if (mFilmDetails == null) {
                return MediaPOJO()
            }
        }

        return mFilmDetails?.let { details ->
            details.reverseWatched()
            val film = MediaByPerson(details.title, details.id, details.poster_path).also {
                it.isFilm = details.isFilm
                it.isWatched = details.isWatched
                it.isQueued = details.isQueued
            }

            mCompositeDisposable.add(checkIfMovieExists(film)
                    .doOnEvent { x: MediaByPerson?, _: Throwable? ->
                        if (x == null) {
                            film.ranking = -1
                            mCompositeDisposable.add(mRepo.insertMovie(film))
                            publishRankedMovies()
                        } else {
                            mCompositeDisposable.add(mRepo.updateFilm(film))
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _, _ ->
                        if (detailsFilmInFilmList()) {
                            displayList[displayList.indexOf(film)].isWatched = film.isWatched
                            filmListPublishSubject.onNext(displayList)
                            if (film.isWatched) {
                                updateWatchedMovieCount(film.id, 1)
                            } else {
                                updateWatchedMovieCount(film.id, -1)
                            }
                        }
                    }
            )
            details
        } ?: MediaPOJO()
    }

    fun onMovieQueuedFromDetails(mediaPOJO: MediaPOJO?): MediaPOJO {
        if (mFilmDetails == null) {
            mFilmDetails = mediaPOJO
        }

        return mFilmDetails?.let { details ->
            details.reverseQueued()
            val film = MediaByPerson(details.title, details.id, details.poster_path).also {
                it.isFilm = details.isFilm
                it.isWatched = details.isWatched
                it.isQueued = details.isQueued
            }

            mCompositeDisposable.add(checkIfMovieExists(film)
                    .doOnEvent { x: MediaByPerson?, _: Throwable? ->
                        if (x == null) {
                            film.ranking = -1
                            mRepo.insertQueuedMovie(film)
                            publishRankedMovies()
                        } else {
                            mRepo.updateQueuedFilm(film)
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _, _ ->
                        Log.d("fata", "subscribe 1")
                        if (detailsFilmInFilmList()) {
                            displayList[displayList.indexOf(film)].isQueued = film.isQueued
                            filmListPublishSubject.onNext(displayList)
                        }
                    }
            )
            details
        } ?: MediaPOJO()
    }

    private fun detailsFilmInFilmList(): Boolean = mFilmDetails?.let {
        getFilmIDs(displayList).contains(it.id)
    } ?: false

    private fun checkMovie(mediaPOJO: MediaPOJO): Single<MediaByPerson> = mRepo
            .checkIfMovieExists(mediaPOJO.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun checkForMovieFromDetails(mediaPOJO: MediaPOJO) {
        mCompositeDisposable.add(checkMovie(mediaPOJO)
                .subscribe(
                        { film: MediaByPerson -> setQueuedAndWatched(film, mediaPOJO) }
                ) { publishFilmDetails(mediaPOJO) })
    }

    private fun setQueuedAndWatched(film: MediaByPerson, mediaPOJO: MediaPOJO) {
        mediaPOJO.apply {
            isWatched = film.isWatched
            isQueued = film.isQueued
            publishFilmDetails(this)
        }
    }

    private fun publishFilmDetails(mediaPOJO: MediaPOJO) {
        mFilmDetails = mediaPOJO
        filmDetailsPublishSubject.onNext(mFilmDetails ?: MediaPOJO())
    }

    fun addOrRemoveList(): Maybe<MyList>? = mFilmListDetails?.personPOJO?.id?.let { id ->
        mRepo.checkIfListExists(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnEvent { x: MyList?, y: Throwable? ->
                    if (x == null) {
                        mFilmListDetails?.personPOJO?.let {
                            addList(MyList(id.toInt(), it.name, mWatchedFilms, mTotalFilms, it.profile_path))
                        }
                    } else {
                        removeList(id)
                    }
                }

    }

    fun getGenreList(tvOrMovie: Boolean): Single<GenreList> = mRepo.getGenreList(if (tvOrMovie) "movie" else "tv")

    fun checkIfListExists(id: String?): Maybe<MyList> = mRepo.checkIfListExists(id)

    val savedLists: Flowable<List<MyList>>
        get() = mRepo.savedLists
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    fun setInitialSpin(initial: Boolean) {
        initialSpin = initial
    }

    fun onSpin(pos: Int) {
        if (initialSpin) {
            setInitialSpin(false)
            return
        }
        if (mMovieCredits == null) {
            return
        }
        val mediaByPersonSet: Set<MediaByPerson>
        mediaByPersonSet = when (pos) {
            1 -> HashSet<MediaByPerson>(mMovieCredits?.cast ?: emptySet())
            2 -> HashSet<MediaByPerson>(mMovieCredits?.crew ?: emptySet())
            3 -> HashSet<MediaByPerson>(mFilmListDetails?.personPOJO?.tvCredits?.bothLists()
                    ?: emptyList())
            else -> HashSet<MediaByPerson>(mMovieCredits?.bothLists() ?: emptyList())
        }
        displayList.clear()
        displayList.addAll(mediaByPersonSet)
        val filmIDs = getFilmIDs(displayList)
        mCompositeDisposable.add(
                scanMoviesForWatched(filmIDs)
                        .subscribe({ watchedList: Map<String, MediaByPerson> -> updateDisplayWatched(watchedList, displayList) }
                        ) { e: Throwable -> Log.e(TAG_RXERROR, "scanMoviesForWatched670" + e.message) }
        )
    }

    private fun getFilmIDs(films: List<MediaByPerson>): List<String> = films.map { it.id }

    private fun scanMoviesForWatched(filmIDs: List<String>): Single<Map<String, MediaByPerson>> =
            mRepo.getWatchedMovies()
                    .subscribeOn(Schedulers.io())
                    .flattenAsObservable { s: List<MediaByPerson>? -> s }
                    .filter { film: MediaByPerson -> filmIDs.contains(film.id) }
                    .toMap { obj: MediaByPerson -> obj.id }
                    .observeOn(AndroidSchedulers.mainThread())

    override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    fun finishScrollLoading() {
        scrollLoading = false
    }

    fun onScrollEnd() {
        if (scrollLoading) {
            return
        }
        scrollLoading = true
        filmsPageNumber++
        val filmsObservable: Observable<List<FilmByPerson>> = when (mFilmListDetails!!.personPOJO.id) {
            MovieKeys.LIST_POPULAR -> mRepo.getPopularFilms(filmsPageNumber)
            MovieKeys.LIST_NOWPLAYING -> mRepo.getNowPlaying(filmsPageNumber)
            MovieKeys.LIST_TOPRATED -> mRepo.getTopRated(filmsPageNumber)
            MovieKeys.LIST_GENRE -> mRepo.getMoviesByGenre(tvOrMovieString, genreID, filmsPageNumber)
            else -> return
        }
        mCompositeDisposable.add(filmsObservable
                .flatMap { filmByPeople -> Observable.just(ArrayList(filmByPeople)) }
                .subscribe({ filmByPeople: ArrayList<FilmByPerson>? ->
                    displayList.addAll(filmByPeople!!)
                    val filmIDs = getFilmIDs(displayList)
                    mTotalFilms = displayList.size
                    Log.d("ssssssss", "total films$mTotalFilms")
                    mCompositeDisposable.add(scanMoviesForWatched(filmIDs)
                            .subscribe({ watchedList: Map<String, MediaByPerson> -> updateWatched(watchedList, displayList) }
                            ) { e: Throwable ->
                                Log.e(TAG_RXERROR, "scanMoviesForWatched738 e=" + e.message)
                                filmsPageNumber--
                                finishScrollLoading()
                            }
                    )
                }
                ) { e: Throwable ->
                    Log.e(TAG_RXERROR, "onScrollEnd e=" + e.message)
                    filmsPageNumber--
                    finishScrollLoading()
                }
        )
    }

    fun setFilmShowRankings(isFilm: Boolean) {
        isFilmRankings = isFilm
    }

    fun publishRankedMovies() {
        mCompositeDisposable.add(mRepo.getSavedForRankings(isFilmRankings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t: List<MediaByPerson> -> rankingsSubject.onNext(t) }
                ) { e: Throwable -> Log.e(TAG_RXERROR, "getSavedForRankings e=" + e.message) }
        )
    }

    fun updateRankingNew(movie: MediaByPerson, newRank: Int) {
        mCompositeDisposable.add(checkIfMovieExists(movie)
                .subscribe({ x: MediaByPerson? ->
                    if (x == null) {
                        mRepo.insertMovie(movie)
                    }
                    mCompositeDisposable.add(mRepo.updateRankingNew(movie.id, newRank, isFilmRankings))
                }
                ) {
                    mRepo.insertMovie(movie)
                    mCompositeDisposable.add(mRepo.updateRankingNew(movie.id, newRank, isFilmRankings))
                }
        )
    }

    fun updateRankingRemove(id: String?, oldRank: Int) {
        mCompositeDisposable.add(mRepo.updateRankingRemove(id!!, oldRank, isFilmRankings))
    }

    fun updateRankingUp(id: String?, oldRank: Int, newRank: Int) {
        mCompositeDisposable.add(mRepo.updateRankingUp(id!!, oldRank, newRank, isFilmRankings))
    }

    fun updateRankingDown(id: String?, oldRank: Int, newRank: Int) {
        mCompositeDisposable.add(mRepo.updateRankingDown(id!!, oldRank, newRank, isFilmRankings))
    }

    companion object {
        private const val TAG_RXERROR = "rxprob MovieVM"
        private const val MOVIE_ID = "287"
    }

}