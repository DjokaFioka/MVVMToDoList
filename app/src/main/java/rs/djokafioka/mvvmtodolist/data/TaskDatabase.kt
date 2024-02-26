package rs.djokafioka.mvvmtodolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rs.djokafioka.mvvmtodolist.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Provider

/**
 * Created by Djordje on 10.8.2022..
 */

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>, //Mora da bude Provider<TaskDatabase> a ne TaskDatabase jer bi onda bilo cirkularne dependency
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() { //@Inject tells dagger how to create instance of this class
        //punimo bazu nekim dummy podacima
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            //db operations
            val dao = database.get().taskDao() //tek sada ce se kreirati baza

            applicationScope.launch {
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("Do the laundry"))
                dao.insert(Task("Buy groceries", important = true))
                dao.insert(Task("Prepare food", completed = true))
                dao.insert(Task("Call mom"))
                dao.insert(Task("Visit grandma", completed = true))
                dao.insert(Task("Repair my bike"))
                dao.insert(Task("Call Elon Musk"))
            }

        }
    }
}