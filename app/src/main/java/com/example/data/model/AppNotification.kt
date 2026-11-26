package com.example.data.model

enum class NotificationType {
    SCAN_COMPLETED,
    REPORT_GENERATED,
    HIGH_RISK_ALERT,
    QUALITY_ADVICE,
    SYSTEM_STATUS
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.SCAN_COMPLETED,
    val isRead: Boolean = false,
    val diseaseClass: SkinDiseaseClass? = null,
    val confidencePercentage: Int? = null,
    val scanId: Long? = null
)
