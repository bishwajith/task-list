package com.bish.tasklist.model

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val timeStamp: Long,
    val status: Status = Status.TO_DO,
    val key: String? = null
)

fun Map<String, Any>.getTask(key: String?): Task {
    val map = this
    val id: String = map["id"].toString()
    val title: String = map["title"].toString()
    val description: String = map["description"].toString()
    val timeStamp = map["timeStamp"]?.toString()?.toLongOrNull() ?: 0L
    val statusString = map["status"] ?: ""
    val status = when (statusString.toString().uppercase()) {
        "TO_DO" -> Status.TO_DO
        "IN_PROGRESS" -> Status.IN_PROGRESS
        "DONE" -> Status.DONE
        else -> Status.TO_DO // Default to TO_DO if status is not recognized
    }

    // Create a Task object
    return Task(id, title, description, timeStamp, status, key = key)
}