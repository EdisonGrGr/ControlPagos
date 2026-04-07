package com.example.controlpagos

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecordatorioHelper(private val context: Context) {

    fun cancelarRecordatorio(numeroCuenta: String) {
        WorkManager.getInstance(context).cancelUniqueWork(claveRecordatorio(numeroCuenta))
    }

    fun programarRecordatorio(nombre: String, numeroCuenta: String, fecha: String) {
        try {
            if (numeroCuenta.isBlank() || fecha.isBlank()) return

            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaPago = formato.parse(fecha) ?: return

            val calendarioPago = Calendar.getInstance()
            calendarioPago.time = fechaPago

            calendarioPago.set(Calendar.HOUR_OF_DAY, 9)
            calendarioPago.set(Calendar.MINUTE, 0)
            calendarioPago.set(Calendar.SECOND, 0)
            calendarioPago.set(Calendar.MILLISECOND, 0)

            val calendarioAntes = calendarioPago.clone() as Calendar
            calendarioAntes.add(Calendar.DAY_OF_MONTH, -1)

            val delayAntes = calendarioAntes.timeInMillis - System.currentTimeMillis()

            if (delayAntes <= 0) return

            val dataAntes = Data.Builder()
                .putString("titulo", "Recordatorio de pago")
                .putString("mensaje", "Mañana debes pagar: $nombre")
                .build()

            val requestAntes = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayAntes, TimeUnit.MILLISECONDS)
                .setInputData(dataAntes)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                claveRecordatorio(numeroCuenta),
                ExistingWorkPolicy.REPLACE,
                requestAntes
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun claveRecordatorio(numeroCuenta: String): String {
        val limpia = numeroCuenta.trim().replace(Regex("[^A-Za-z0-9_-]"), "_")
        return "recordatorio_$limpia"
    }
}
