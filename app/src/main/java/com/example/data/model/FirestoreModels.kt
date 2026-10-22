package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UnitProgressDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookNumber: Long = 1L,
    val unitNumber: Long = 1L,
    val isUnlocked: Boolean = false,
    val isPassed: Boolean = false,
    val score: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WordProgressDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val wordId: Long = 0L,
    val bookNumber: Long = 1L,
    val unitNumber: Long = 1L,
    val word: String = "",
    val isLearned: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
