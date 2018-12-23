package xyz.sleekstats.completist.model;

import java.util.List;

public class FilmListDetails {

    private final PersonPOJO personPOJO;
    private final List<MediaByPerson> mediaByPersonList;

    public FilmListDetails(PersonPOJO personPOJO, List<MediaByPerson> mediaByPersonList) {
        this.personPOJO = personPOJO;
        this.mediaByPersonList = mediaByPersonList;
    }

    public PersonPOJO getPersonPOJO() {
        return personPOJO;
    }

    public List<MediaByPerson> getMediaByPersonList() {
        return mediaByPersonList;
    }
}
