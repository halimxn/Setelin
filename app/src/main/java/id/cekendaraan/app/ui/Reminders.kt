package id.cekendaraan.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import id.cekendaraan.app.data.Prefs
import id.cekendaraan.app.data.Reminder

class ReminderToggle(val enabled: Boolean, val set: (Boolean) -> Unit)

/** Daily service reminder switch, asking for the notification permission when needed. */
@Composable
fun rememberReminderToggle(): ReminderToggle {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(Prefs.remindersEnabled(context)) }

    fun turnOn() {
        Prefs.setRemindersEnabled(context, true)
        Reminder.schedule(context)
        enabled = true
        Toast.makeText(context, "Pengingat servis aktif", Toast.LENGTH_SHORT).show()
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            turnOn()
        } else {
            enabled = false
            Toast.makeText(context, "Izin notifikasi belum diberikan", Toast.LENGTH_LONG).show()
        }
    }

    return ReminderToggle(enabled) { value ->
        if (!value) {
            Prefs.setRemindersEnabled(context, false)
            Reminder.cancel(context)
            enabled = false
            return@ReminderToggle
        }
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else turnOn()
    }
}
