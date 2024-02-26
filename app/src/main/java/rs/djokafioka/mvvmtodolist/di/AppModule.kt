package rs.djokafioka.mvvmtodolist.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import rs.djokafioka.mvvmtodolist.data.TaskDatabase
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Created by Djordje on 10.8.2022..
 */

@Module
@InstallIn(ApplicationComponent::class) //
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback //ovo smo mi napravili kao nested class
    ) =
        Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback) //hocemo da inicijalno napunimo bazu nekim dummy podacima
            .build()

    @Provides
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob()) //Pravimo svoj coroutine scope da ne koristimo GlobalScope. Prvo to se ne savetuje, a drugo, koristimo SupervisorJob da ako bi jedna coroutine pukla, ne bi ceo Scope pukao. Sa SupervisorJob ostale coroutine nastavljaju
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope