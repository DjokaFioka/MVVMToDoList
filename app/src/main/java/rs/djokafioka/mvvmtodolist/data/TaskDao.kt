package rs.djokafioka.mvvmtodolist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Created by Djordje on 10.8.2022..
 */
@Dao
interface TaskDao {

    //suspend function can only be called from another suspend function or from a coroutine
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    @Query("SELECT * FROM task_table " +
            "WHERE (completed != :hideCompleted OR completed = 0) " +
            "AND name LIKE '%' || :searchQuery || '%' " +
            "ORDER BY important DESC, name") //task_table smo naveli u klasi Task i anotirali kao @Entity, || nije or nego konkatenacija za SQLIte
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>> //flow is asynchronous stream of data - this could be also LiveData

    @Query("SELECT * FROM task_table " +
            "WHERE (completed != :hideCompleted OR completed = 0) " +
            "AND name LIKE '%' || :searchQuery || '%' " +
            "ORDER BY important DESC, created") //task_table smo naveli u klasi Task i anotirali kao @Entity, || nije or nego konkatenacija za SQLIte
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>> //flow is asynchronous stream of data - this could be also LiveData

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()
}