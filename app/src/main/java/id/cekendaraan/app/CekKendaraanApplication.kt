package id.cekendaraan.app

import android.app.Application
import id.cekendaraan.app.data.AppDatabase
import id.cekendaraan.app.data.Prefs
import id.cekendaraan.app.data.Reminder

class CekKendaraanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.get(this)
        Reminder.ensureChannel(this)
        if (Prefs.remindersEnabled(this)) {
            Reminder.schedule(this)
        }
    }
}
