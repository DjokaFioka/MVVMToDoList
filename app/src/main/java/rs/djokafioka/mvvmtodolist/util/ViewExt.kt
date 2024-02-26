package rs.djokafioka.mvvmtodolist.util

import androidx.appcompat.widget.SearchView

/**
 * Created by Djordje on 11.8.2022..
 */
//Pravimo Extension function na SearchView
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}