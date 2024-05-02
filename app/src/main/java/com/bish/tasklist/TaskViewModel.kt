package com.bish.tasklist

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.bish.tasklist.model.FilterUIModel
import com.bish.tasklist.model.Task

interface TaskViewModel {

    val taskList: SnapshotStateList<Task>

    fun createTask(title: String, description: String)

    fun onDeleteClick(item: Task)

    fun onUpdateState(item: Task, index: Int)

    fun onDeleteAllClick()

    val filterUiList: SnapshotStateList<FilterUIModel>

    fun onFilterClick(index: Int, uiModel: FilterUIModel)

    fun isFilterApplied(): Boolean
}