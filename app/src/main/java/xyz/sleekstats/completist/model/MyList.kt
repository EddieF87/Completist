package xyz.sleekstats.completist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_table")
class MyList(
        @field:PrimaryKey var list_id: Int,
        var list_name: String?,
        var watched_films: Int,
        var total_films: Int,
        var list_img: String?
) {

    val list_pct: Int
        get() = if (total_films == 0) {
            -1
        } else watched_films * 100 / total_films

}