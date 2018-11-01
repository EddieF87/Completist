package xyz.sleekstats.completist.model;

public class WatchCount {

    private int watched;
    private int total;

    public WatchCount(int watched, int total) {
        this.watched = watched;
        this.total = total;
    }

    public int getWatched() {
        return watched;
    }

    public void setWatched(int watched) {
        this.watched = watched;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getWatchedPct() {
        if(this.total == 0) {
            return 0;
        }
        return (this.watched * 100)/this.total;
    }

    @Override
    public String toString() {
        return "Watched: " + this.watched + "/" + this.total + " (" + this.getWatchedPct() + "%)";
    }
}
