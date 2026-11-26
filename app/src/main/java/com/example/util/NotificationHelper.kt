package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID = "dermavision_clinical_channel"
    private const val CHANNEL_NAME = "DermaVision Clinical Updates"
    private const val CHANNEL_DESC = "Notifications for completed skin condition analyses and clinical report exports"

    private const val NOTIFICATION_ID_ANALYSIS = 1001
    private const val NOTIFICATION_ID_REPORT = 1002
    private const val NOTIFICATION_ID_SYSTEM = 1003

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Triggers real system notification when skin analysis is completed.
     */
    fun showAnalysisCompletedNotification(
        context: Context,
        conditionName: String,
        confidencePercent: Int
    ) {
        createNotificationChannel(context)

        // Verify POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_ANALYSIS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Analysis Completed")
            .setContentText("Result: $conditionName ($confidencePercent% match)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Skin analysis completed successfully.\nPrimary finding: $conditionName with $confidencePercent% confidence. Tap to view detailed clinical observations.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ANALYSIS, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Triggers real system notification when a PDF report is generated.
     */
    fun showReportGeneratedNotification(
        context: Context,
        conditionName: String,
        filePath: String? = null
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_REPORT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Clinical Report Generated")
            .setContentText("PDF Summary ready for $conditionName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your comprehensive clinical dermatological report for $conditionName has been generated and is ready to share with your physician.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REPORT, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
