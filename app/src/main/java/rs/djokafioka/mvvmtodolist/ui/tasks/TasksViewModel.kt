package rs.djokafioka.mvvmtodolist.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.djokafioka.mvvmtodolist.R
import rs.djokafioka.mvvmtodolist.data.PreferencesManager
import rs.djokafioka.mvvmtodolist.data.SortOrder
import rs.djokafioka.mvvmtodolist.data.Task
import rs.djokafioka.mvvmtodolist.data.TaskDao
import rs.djokafioka.mvvmtodolist.ui.ADD_TASK_RESULT_OK
import rs.djokafioka.mvvmtodolist.ui.EDIT_TASK_RESULT_OK

/**
 * Created by Djordje on 10.8.2022..
 */
class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle //Handling process death, pa cuvamo search query u SavedInstanceState
) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")
//    val searchQuery = MutableStateFlow("")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val taskEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        searchQuery.asFlow(),
//        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPrefs) ->
        taskDao.getTasks(query, filterPrefs.sortOrder, filterPrefs.hideCompleted)
    }

    //Pre nego sto smo napravili cuvanje izabranih opcija u DataStore
//    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
//    val hideCompleted = MutableStateFlow(false)

//    private val tasksFlow = combine(
//        searchQuery,
//        sortOrder,
//        hideCompleted
//    ) { query, sortOrder, hideCompleted ->
//        Triple(query, sortOrder, hideCompleted)
//    }.flatMapLatest { (query, sortOrder, hideCompleted) -> //Destructuring
//        taskDao.getTasks(query, sortOrder, hideCompleted)
//    }

//    private val tasksFlow = searchQuery
//        .flatMapLatest {
//        taskDao.getTasks(it)
//    }

    val tasks = tasksFlow.asLiveData() //slicno kao Flow, samo LiveData ima uvek poslednju verziju vrednosti, a ne ceo strim vrednosti

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
        }
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            taskDao.update(task.copy(completed = isChecked)) //copy postoji jer je data class, a moramo ovako jer su u konstruktoru Task.kt val a ne var
        }
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(R.string.task_added_msg)
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(R.string.task_update_msg)
        }
    }

    private fun showTaskSavedConfirmationMessage(msgResId: Int) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(msgResId))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteALlCompletedScreen)
    }

    //Implementing showing snackbar from viewModel to Fragment using channels
    sealed class TasksEvent {
        object NavigateToAddTaskScreen: TasksEvent() //Ako ne prosledjujemo nista, ne moramo da pravimo data class, moze samo object
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msgRedId: Int) : TasksEvent()
        object NavigateToDeleteALlCompletedScreen: TasksEvent()
    }
}

