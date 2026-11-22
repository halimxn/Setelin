package id.cekendaraan.app.domain

import id.cekendaraan.app.data.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToLong

enum class DueStatus { NONE, UNKNOWN, OK, SOON, DUE }

enum class DueLimit { NONE, KM, TIME }

data class DueInfo(
    val status: DueStatus,
    val ratio: Float,
    val progressKm: Float,
    val progressTime: Float,
    val remainingKm: Int?,
    val remainingDays: Long?,
    val dueDate: LocalDate?,
    val limit: DueLimit,
)

/**
 * Counts a component down by distance and by time, whichever runs out first.
 * Without a baseline the component is UNKNOWN rather than falsely overdue.
 */
fun Component.dueInfo(
    odometerKm: Int,
    avgDailyKm: Double = 0.0,
    today: LocalDate = LocalDate.now(),
): DueInfo {
    val hasInterval = (intervalKm != null && intervalKm > 0) ||
        (intervalMonths != null && intervalMonths > 0)
    if (!hasInterval) {
        return DueInfo(DueStatus.NONE, 0f, 0f, 0f, null, null, null, DueLimit.NONE)
    }

    val lastOn = lastServiceOn?.let(LocalDate::ofEpochDay)
    val usedKm = lastServiceOdo?.let { (odometerKm - it).coerceAtLeast(0) }

    val ratioKm = if (intervalKm != null && intervalKm > 0 && usedKm != null) {
        usedKm.toFloat() / intervalKm
    } else {
        null
    }

    val timeDue: LocalDate?
    val ratioTime: Float?
    if (lastOn != null && intervalMonths != null && intervalMonths > 0) {
        val due = lastOn.plusMonths(intervalMonths.toLong())
        val total = ChronoUnit.DAYS.between(lastOn, due).coerceAtLeast(1).toFloat()
        timeDue = due
        ratioTime = (ChronoUnit.DAYS.between(lastOn, today).toFloat() / total).coerceAtLeast(0f)
    } else {
        timeDue = null
        ratioTime = null
    }

    if (ratioKm == null && ratioTime == null) {
        return DueInfo(
            status = DueStatus.UNKNOWN,
            ratio = 0f,
            progressKm = 0f,
            progressTime = 0f,
            remainingKm = intervalKm,
            remainingDays = null,
            dueDate = null,
            limit = DueLimit.NONE,
        )
    }

    val ratio = max(ratioKm ?: 0f, ratioTime ?: 0f)
    val status = when {
        ratio >= 1f -> DueStatus.DUE
        ratio >= 0.8f -> DueStatus.SOON
        else -> DueStatus.OK
    }
    val limit = when {
        ratioKm != null && (ratioTime == null || ratioKm >= ratioTime) -> DueLimit.KM
        ratioTime != null -> DueLimit.TIME
        else -> DueLimit.NONE
    }

    val remainingKm = if (intervalKm != null && intervalKm > 0 && usedKm != null) {
        intervalKm - usedKm
    } else {
        null
    }

    val estimatedFromKm = remainingKm?.let { left ->
        if (avgDailyKm > 0.5) today.plusDays((max(left, 0) / avgDailyKm).roundToLong()) else null
    }
    val dueDate = timeDue ?: estimatedFromKm
    val remainingDays = dueDate?.let { ChronoUnit.DAYS.between(today, it) }

    return DueInfo(
        status = status,
        ratio = ratio,
        progressKm = (ratioKm ?: 0f).coerceIn(0f, 1f),
        progressTime = (ratioTime ?: 0f).coerceIn(0f, 1f),
        remainingKm = remainingKm,
        remainingDays = remainingDays,
        dueDate = dueDate,
        limit = limit,
    )
}
