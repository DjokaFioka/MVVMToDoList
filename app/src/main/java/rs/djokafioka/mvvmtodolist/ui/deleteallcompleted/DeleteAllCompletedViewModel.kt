package rs.djokafioka.mvvmtodolist.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rs.djokafioka.mvvmtodolist.data.TaskDao
import rs.djokafioka.mvvmtodolist.di.ApplicationScope

/**
 * Created by Djordje on 23.2.2024..
 */
class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
    //koristimo CoroutineScope jer nam treba siri Scope od ViewModelScope kada je dijalog u pitanju jer brisanje moze da potraje,
    // a na potvrdu dijalog ce se odmah zatvoriti i ViewModelScope ce nestati i potencijalno otkazati brisanje
) : ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}