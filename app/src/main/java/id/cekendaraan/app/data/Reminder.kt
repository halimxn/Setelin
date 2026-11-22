package id.cekendaraan.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import id.cekendaraan.app.MainActivity
import id.cekendaraan.app.R
import id.cekendaraan.app.domain.DueStatus
import id.cekendaraan.app.domain.dueInfo
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object Reminder {
    const val CHANNEL_ID = "service_reminders"
    private const val WORK_NAME = "daily_service_reminder"
    private const val NOTIFICATION_ID = 160

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val channel = android.app.NotificationChannel(
            CHANNEL_ID,
            "Pengingat servis",
            android.app.NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Pengingat komponen Vario yang mendekati atau melewati jadwal servis."
        }
        context.getSystemService(android.app.NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun schedule(context: Context) {
        ensureChannel(context)
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(8, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMinutes = Duration.between(now, next).toMinutes().coerceAtLeast(1)

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    @SuppressLint("MissingPermission")
    fun notify(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_service)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!Prefs.remindersEnabled(context)) return Result.success()

        val dao = AppDatabase.get(context).dao()
        val vehicle = dao.vehicleOnce() ?: return Result.success()
        val components = dao.componentsOnce()
        val avgDaily = vehicle.monthlyKm / 30.0
        val attention = components
            .map { it to it.dueInfo(vehicle.odometerKm, avgDaily) }
            .filter { it.second.status == DueStatus.DUE || it.second.status == DueStatus.SOON }

        if (attention.isEmpty()) return Result.success()

        val names = attention.take(3).joinToString(" · ") { (component, due) ->
            when {
                due.remainingKm != null && due.remainingKm < 0 ->
                    "${component.name} lewat ${-due.remainingKm} km"
                due.remainingDays != null && due.remainingDays < 0 ->
                    "${component.name} lewat ${-due.remainingDays} hari"
                else -> "${component.name} segera"
            }
        }
        val title = if (attention.size == 1) {
            "${attention.first().first.name} perlu perhatian"
        } else {
            "${attention.size} komponen Vario perlu perhatian"
        }
        Reminder.notify(context, title, names)
        return Result.success()
    }
}
