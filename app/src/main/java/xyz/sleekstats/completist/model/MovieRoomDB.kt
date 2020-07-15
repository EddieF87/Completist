package xyz.sleekstats.completist.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.sleekstats.completist.service.MovieDao

@Database(entities = [MediaByPerson::class, MyList::class], version = 2)
abstract class MovieRoomDB : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        private var INSTANCE: MovieRoomDB? = null
        fun getDatabase(context: Context): MovieRoomDB {
            if (INSTANCE == null) {
                synchronized(MovieRoomDB::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                MovieRoomDB::class.java, "movie_database")
                                .build()
                    }
                }
            }
            return INSTANCE ?: getDatabase(context)
        }
    }
}