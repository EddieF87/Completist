package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {MyMovie.class, MyList.class}, version = 1)
public abstract class MovieRoomDB  extends RoomDatabase {

    public abstract MovieDao movieDao();
    private static MovieRoomDB INSTANCE;

    public static MovieRoomDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MovieRoomDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MovieRoomDB.class, "movie_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
