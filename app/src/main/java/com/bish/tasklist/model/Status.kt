package com.bish.tasklist.model

import androidx.compose.ui.graphics.Color

enum class Status {
    TO_DO,
    IN_PROGRESS,
    DONE
}

fun Status.getLabel(): String {
    return when (this) {
        Status.TO_DO -> "To Do"
        Status.IN_PROGRESS -> "In-Progress"
        Status.DONE -> "Done"
    }
}

fun Status.getNextState(): Status? {
    return when (this) {
        Status.TO_DO -> Status.IN_PROGRESS
        Status.IN_PROGRESS -> Status.DONE
        Status.DONE -> null
    }
}
fun Status.getNextStateLabel(): String? {
    return when (this) {
        Status.TO_DO -> "In-Progress"
        Status.IN_PROGRESS -> "Done"
        Status.DONE -> null
    }
}

fun Status.getBackgroundColor(): Color {
    return when (this) {
        Status.TO_DO -> Color(0xFF000000)
        Status.IN_PROGRESS -> Color(0xFFFFC107)
        Status.DONE -> Color(0xFF1C659E)
    }
}
fun Status.getTextColor(): Color {
    return when (this) {
        Status.TO_DO -> Color.White
        Status.IN_PROGRESS -> Color.Black
        Status.DONE -> Color.White
    }
}