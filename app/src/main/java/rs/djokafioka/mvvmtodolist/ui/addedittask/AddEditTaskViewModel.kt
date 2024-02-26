package rs.djokafioka.mvvmtodolist.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.djokafioka.mvvmtodolist.R
import rs.djokafioka.mvvmtodolist.data.Task
import rs.djokafioka.mvvmtodolist.data.TaskDao
import rs.djokafioka.mvvmtodolist.ui.ADD_TASK_RESULT_OK
import rs.djokafioka.mvvmtodolist.ui.EDIT_TASK_RESULT_OK

/**
 * Created by Djordje on 12.8.2022..
 */
class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle //Handling process death
) : ViewModel() {

    val task = state.get<Task>("task") //mora da bude isto (task) kao sto smo upisali u navgraph kada smo dodeljivali argument fragmentu

    var taskName = state.get<String>("taskName") ?: task?.name ?: "" //Izvlacimo iz savedInstanceState, ako je null, onda preuzimamo iz taska, ako je i task null, onda je prazan string
        set(value) {
            field = value
            state.set("taskName", value) //Cuvamo odmah kada promenimo u SavedInstanceState
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    //Treba nam za obavestavanje framneta od strane viewModela channel i sealed class na dnu
    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) { //Empty or only white spaces
            showInvalidInputMessage(R.string.name_empty_error) //how to extract String resource in ViewModel?
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)//Since our task is immutable, we need to create a completely new object and send it to db (Task is immutable because we used val instead of var in constructor)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(resId: Int) {
        viewModelScope.launch {
            addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(resId))
        }
    }

    //Treba nam za obavestavanje fragmenta od strane viewModela ovo i channel gore
    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msgResId: Int) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}