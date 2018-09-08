package xyz.sleekstats.completist.model;

import java.util.List;

public class FilmListDetails {

    private PersonPOJO personPOJO;
    private List<FilmByPerson> filmByPersonList;

    public FilmListDetails(PersonPOJO personPOJO, List<FilmByPerson> filmByPersonList) {
        this.personPOJO = personPOJO;
        this.filmByPersonList = filmByPersonList;
    }

    public PersonPOJO getPersonPOJO() {
        return personPOJO;
    }

    public List<FilmByPerson> getFilmByPersonList() {
        return filmByPersonList;
    }
}
