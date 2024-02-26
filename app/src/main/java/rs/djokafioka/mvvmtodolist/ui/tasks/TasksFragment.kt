package rs.djokafioka.mvvmtodolist.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import rs.djokafioka.mvvmtodolist.R
import rs.djokafioka.mvvmtodolist.data.SortOrder
import rs.djokafioka.mvvmtodolist.data.Task
import rs.djokafioka.mvvmtodolist.databinding.FragmentTasksBinding
import rs.djokafioka.mvvmtodolist.util.exhaustive
import rs.djokafioka.mvvmtodolist.util.onQueryTextChanged

/**
 * Created by Djordje on 9.8.2022..
 */
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView: SearchView //napravili smo member variable jer postoji bug kod rotiranja ekrana i process death da zatvara SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view) //ne moramo da radimo inflate jer je u konstruktor prosledjen layout
        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }

            //Implementing swipe to delete
            ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->//isti key smo upotrebili u AddEditTaskFragment
                val result = bundle.getInt("add_edit_result") //isti key smo upotrebili u AddEditTaskFragment
                //showSnackbar, but viewModel should decide whether to show it or not, actually whether to tell the fragment if the Snackbar should be shown or not
                viewModel.onAddEditResult(result)
        }


        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }

        //Preuzimamo flow da bismo znali kada treba da prikazemo snackbar za brisanje
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), getString(R.string.task_deleted), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.undo_capital)) {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(title = getString(
                                                    R.string.new_task)) //da bi se pojavila ova metoda morali smo da uradimo rebuild
                        findNavController().navigate(action)
                        //moze i ovako
                        //findNavController().navigate(R.id.addEditTaskFragment)
                        //ali je bolje preko action jer imamo compile time safety
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task, title = getString(
                                                    R.string.edit_task)) //da bi se pojavila ova metoda morali smo da uradimo rebuild
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(event.msgRedId), Snackbar.LENGTH_SHORT).show()
                    }
                    TasksViewModel.TasksEvent.NavigateToDeleteALlCompletedScreen -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment() //za ovo je nekad potreban rebuild da bi se videla funkcija
                        findNavController().navigate(action)
                    }
                }.exhaustive //Naparvili smo Extension Function u Utils.kt da bismo od statement-a napravili expression i proverili da li su pokriveni svi slucajevi

            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value //zbog bug sa SearchView kod rotiranja
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

//        searchView.setOnQueryTextListener() //posto ovo zahteva override 2 funkcije, napravicemo Kotlin Extension Function u ViewExt.kt
        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
            //ovde ne mozemo da stavimo return jer imamo crossinline u Extension function
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_sort_by_name -> {
               viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null) //opet ovo je zbog bug sa searchview kod rotiranja jer salje prazan string kada se unistava
    }
}