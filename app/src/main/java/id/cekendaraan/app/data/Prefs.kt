package id.cekendaraan.app.data

import android.content.Context

object Prefs {
    private const val FILE = "cekendaraan_settings"
    private const val REMINDERS = "reminders_enabled"

    fun remindersEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(REMINDERS, false)

    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(REMINDERS, enabled)
            .apply()
    }

    fun note(context: Context, componentId: Long): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString("note_$componentId", "") ?: ""

    fun setNote(context: Context, componentId: Long, text: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString("note_$componentId", text)
            .apply()
    }
}
