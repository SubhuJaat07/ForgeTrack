package com.forgetrack.app.util

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDateTimeString(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Double.toCurrency(currency: String = "USD"): String {
    return "$${String.format("%.2f", this)}"
}

fun Long.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return when {
        hours > 0 -> String.format("%dh %02dm", hours, minutes)
        minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
        else -> "${seconds}s"
    }
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.replaceFirstCharIfNeeded { c -> c.uppercaseChar() } }
}

private fun String.replaceFirstCharIfNeeded(transform: (Char) -> Char): String {
    if (isEmpty()) return this
    return transform(first()) + substring(1)
}

fun todayStart(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun todayEnd(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}

fun weekAgo(): Long {
    return System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
}

fun generateId(): String = UUID.randomUUID().toString().take(8)
