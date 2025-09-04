package com.harshal.didit

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Task(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val creationTimestamp: Long = System.currentTimeMillis(),
    val lastLoggedTimestamp: Long = 0,
    val notes: String = "",
    @SerializedName("isCompleted")
    val isCompleted: Boolean,
    @SerializedName("completedTimestamp")
    val completedTimestamp: Long = 0,
    val groupName: String? = null,
    var isSelected: Boolean = false,
    val reminderTime: Long? = null,
    val recurrenceRule: String? = null,
    val recurrenceData: Set<Int>? = null
) {
    // Secondary constructor for backward compatibility
    constructor(
        id: UUID = UUID.randomUUID(),
        name: String,
        creationTimestamp: Long = System.currentTimeMillis(),
        lastLoggedTimestamp: Long = 0,
        notes: String = "",
        completedTimestamp: Long = 0,
        groupName: String? = null,
        isSelected: Boolean = false,
        reminderTime: Long? = null,
        recurrenceRule: String? = null,
        recurrenceData: Set<Int>? = null
    ) : this(id, name, creationTimestamp, lastLoggedTimestamp, notes, false, completedTimestamp, groupName, isSelected, reminderTime, recurrenceRule, recurrenceData)
}
