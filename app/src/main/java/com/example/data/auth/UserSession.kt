package com.example.data.auth

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val isGuest: Boolean = false,
    val profilePictureUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
