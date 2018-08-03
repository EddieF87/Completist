package xyz.sleekstats.completist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.service.Repo;

public class MovieListViewModel  extends AndroidViewModel {

    private final Repo mRepo;

    public MovieListViewModel(@NonNull Application application) {
        super(application);
        mRepo = new Repo(application);
    }

    public Observable<PersonPOJO> getFilmsByPerson(String personId) {
        return mRepo.getFilmsByPerson(personId);
    }

    public List<FilmByPerson> filterCrew(List<FilmByPerson> unfiltered) {
        List<FilmByPerson> filteredList = new ArrayList<>();
        for (FilmByPerson film : unfiltered) {
            if (film.getJob().equals("Director")) {
                filteredList.add(film);
            }
        }
        return filteredList;
    }
}
