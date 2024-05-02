package com.bish.tasklist

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bish.tasklist.model.FilterUIModel
import com.bish.tasklist.model.Status
import com.bish.tasklist.model.Task
import com.bish.tasklist.model.getNextState
import com.bish.tasklist.model.getTask
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModelImpl : ViewModel(), TaskViewModel {
    companion object {
        private const val PATH_TASKS = "tasks"
    }

    override val taskList: SnapshotStateList<Task> = mutableStateListOf()

    private val taskListUnfiltered: SnapshotStateList<Task> = mutableStateListOf()

    private val dbReference by lazy {
        FirebaseDatabase.getInstance().reference.child(PATH_TASKS)
    }

    override val filterUiList: SnapshotStateList<FilterUIModel> = mutableStateListOf()

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            dbReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.mapNotNull {
                        (it.value as? Map<String, Any>)?.getTask(it.key)
                    }.also {
                        taskListUnfiltered.clear()
                        taskListUnfiltered.addAll(it.sortedByDescending { task: Task -> task.timeStamp })
                    }.also {
                        taskList.clear()
                        val selectedFilter = filterUiList.firstOrNull { it.isSelected }
                        val hasFilteredTask =
                            taskListUnfiltered.firstOrNull { it.status == selectedFilter?.status } != null
                        if (hasFilteredTask) {
                            taskList.addAll(taskListUnfiltered.filter {
                                selectedFilter == null ||
                                        it.status == selectedFilter.status
                            })
                        } else {
                            taskList.addAll(taskListUnfiltered)
                            filterUiList.clear()
                        }

                    }
                    updateFilter()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    override fun createTask(title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                timeStamp = System.currentTimeMillis()
            )
            dbReference.push().setValue(task)
        }
    }

    override fun onDeleteClick(item: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            dbReference.get().addOnCompleteListener {
                it.result.children.firstOrNull { task ->
                    (task.value as? Map<*, *>)?.get("id").toString() == item.id
                }?.let { snapshot ->
                    snapshot.key?.let { it1 ->
                        dbReference.child(it1).removeValue().addOnSuccessListener {
                            taskList.remove(item)
                            updateFilter()
                        }
                    }
                }
            }
        }
    }

    override fun onUpdateState(item: Task, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            item.status.getNextState()?.let {
                val updatedTask = item.copy(
                    status = it
                )
                taskList[index] = updatedTask
                updateFilter()
                dbReference.child(item.key.orEmpty()).setValue(updatedTask)
            }
        }
    }

    override fun onDeleteAllClick() {
        viewModelScope.launch(Dispatchers.IO) {
            dbReference.setValue(null)
            updateFilter()
        }
    }

    private fun updateFilter() {
        val selectedFilter = filterUiList.firstOrNull { it.isSelected }
        filterUiList.clear()
        taskListUnfiltered.groupBy { it.status }.keys.sortedBy { it.ordinal }.map {
            filterUiList.add(FilterUIModel(it, it == selectedFilter?.status))
        }
    }

    override fun onFilterClick(index: Int, uiModel: FilterUIModel) {
        if (uiModel.isSelected) {
            filterUiList[index] = uiModel.copy(isSelected = false)
            taskList.clear()
            taskList.addAll(taskListUnfiltered)
        } else {
            val selectedIndex = filterUiList.indexOfFirst { it.isSelected }
            if (selectedIndex > -1) {
                filterUiList[selectedIndex] = filterUiList[selectedIndex].copy(isSelected = false)
            }
            filterUiList[index] = uiModel.copy(isSelected = true)
            taskList.clear()
            taskList.addAll(taskListUnfiltered.filter { it.status == uiModel.status })
        }
    }

    override fun isFilterApplied(): Boolean {
        return filterUiList.firstOrNull { it.isSelected } != null
    }
}