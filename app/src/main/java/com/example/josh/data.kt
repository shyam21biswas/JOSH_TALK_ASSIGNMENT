package com.example.josh

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskType: String,
    val text: String = "",
    val audioPath: String,
    val imagePath: String = "",
    val imageUrl: String = "",
    val durationSec: Int,
    val timestamp: String
)



@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTaskCount(): Flow<Int>

    @Query("SELECT SUM(durationSec) FROM tasks")
    fun getTotalDuration(): Flow<Int?>
}


@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_recorder_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
