package rs.djokafioka.mvvmtodolist.ui.deleteallcompleted

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import rs.djokafioka.mvvmtodolist.R

/**
 * Created by Djordje on 13.8.2022..
 */
@AndroidEntryPoint
class DeleteAllCompletedDialogFragment : DialogFragment() {

    private val viewModel: DeleteAllCompletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dlg_delete_confirmation_title))
            .setMessage(getString(R.string.dlg_delete_all_completed_message))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                viewModel.onConfirmClick()
            }
            .create()
    }
}