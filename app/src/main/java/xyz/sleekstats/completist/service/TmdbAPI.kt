package xyz.sleekstats.completist.service

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import xyz.sleekstats.completist.model.*

interface TmdbAPI {
    //Query for specific film details based on Tmdb film id
    @GET("movie/{movie_id}?api_key=$API_KEY&language=en-US&append_to_response=credits,similar&include_adult=false&language=en-US")
    fun retrieveFilm(@Path("movie_id") movie_id: String?): Single<FilmPOJO>

    //Query for specific tv show details based on Tmdb show id
    @GET("tv/{show_id}?api_key=$API_KEY&language=en-US&append_to_response=credits,similar&include_adult=false&language=en-US")
    fun retrieveShow(@Path("show_id") show_id: String?): Single<ShowPOJO>

    //Query for list of films by a specific actor/director based on Tmdb actor/director id
    @GET("person/{person_id}?api_key=$API_KEY&append_to_response=movie_credits,tv_credits&include_adult=false&language=en-US")
    fun retrievePersonInfo(@Path("person_id") person_id: String?): Single<PersonPOJO>

    //retrieve most popular movies from TMDB API
    @GET("movie/popular?api_key=$API_KEY&include_adult=false&language=en-US")
    fun retrievePopularMovies(@Query("page") pageNumber: Int): Single<FilmResultsPOJO>

    //retrieve most popular movies from TMDB API
    @GET("tv/popular?api_key=$API_KEY&include_adult=false&language=en-US")
    fun retrievePopularShows(@Query("page") pageNumber: Int): Single<ShowResultsPOJO>

    //retrieve movies currently playing in theatres from TMDB API
    @GET("movie/now_playing?api_key=$API_KEY&include_adult=false&language=en-US")
    fun retrieveNowPlaying(@Query("page") pageNumber: Int): Single<FilmResultsPOJO>

    //retrieve  top-rated movies from TMDB API
    @GET("movie/top_rated?api_key=$API_KEY&include_adult=false&language=en-US")
    fun retrieveTopRated(@Query("page") pageNumber: Int): Single<FilmResultsPOJO>

    @GET("discover/{tvOrMovie}?api_key=$API_KEY&sort_by=popularity.desc&include_adult=false&language=en-US")
    fun retrieveByGenre(
            @Path("tvOrMovie") tvOrMovie: String?,
            @Query("with_genres") genre: String?,
            @Query("page") pageNumber: Int
    ): Single<FilmResultsPOJO>

    @GET("search/multi?api_key=$API_KEY&language=en-US&page=1&include_adult=false&language=en-US")
    fun queryMedia(@Query("query") movie_query: String?): Observable<MediaQueryPOJO>

    @GET("search/movie?api_key=$API_KEY&language=en-US&page=1&include_adult=false&language=en-US")
    fun queryFilmsForRankings(@Query("query") movie_query: String?): Observable<MediaQueryPOJO>

    @GET("search/tv?api_key=$API_KEY&language=en-US&page=1&include_adult=false&language=en-US")
    fun queryShowsForRankings(@Query("query") movie_query: String?): Observable<MediaQueryPOJO>

    @GET("person/popular?api_key=$API_KEY&include_adult=false&language=en-US")
    fun retrievePopularActors(@Query("page") pageNumber: Int): Observable<PersonQueryPOJO>

    @GET("genre/{tvOrMovie}/list?api_key=$API_KEY&language=en-US")
    fun getGenreList(@Path("tvOrMovie") tvOrMovie: String?): Single<GenreList>

    companion object {
        const val API_KEY = "b5f45c3ea3adf1ca53b96fa5bb9394d2"
    }
}