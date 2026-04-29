package com.example.controlpagos

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecordatorioHelper(private val context: Context) {

    fun cancelarRecordatorio(numeroCuenta: String, fecha: String) {
        WorkManager.getInstance(context).cancelUniqueWork(claveRecordatorio(numeroCuenta, fecha))
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
            val now = System.currentTimeMillis()
            if (delayAntes <= 0) {
                // Fecha demasiado próxima o pasada: programar ejecución inmediata para no perder el recordatorio.
                val mensaje = when {
                    calendarioPago.timeInMillis > now -> "Próximo pago: $nombre - $fecha"
                    else -> "Pago vencido: $nombre - $fecha"
                }

                val dataAntes = Data.Builder()
                    .putString("titulo", "Recordatorio de pago")
                    .putString("mensaje", mensaje)
                    .build()

                val requestAhora = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(0, TimeUnit.MILLISECONDS)
                    .setInputData(dataAntes)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    claveRecordatorio(numeroCuenta, fecha),
                    ExistingWorkPolicy.REPLACE,
                    requestAhora
                )
            } else {
                val dataAntes = Data.Builder()
                    .putString("titulo", "Recordatorio de pago")
                    .putString("mensaje", "Mañana debes pagar: $nombre")
                    .build()

                val requestAntes = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delayAntes, TimeUnit.MILLISECONDS)
                    .setInputData(dataAntes)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    claveRecordatorio(numeroCuenta, fecha),
                    ExistingWorkPolicy.REPLACE,
                    requestAntes
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun claveRecordatorio(numeroCuenta: String, fecha: String): String {
        val limpiaCuenta = numeroCuenta.trim().replace(Regex("[^A-Za-z0-9_-]"), "_")
        val limpiaFecha = fecha.trim().replace(Regex("[^A-Za-z0-9_-]"), "_")
        return "recordatorio_${limpiaCuenta}_${limpiaFecha}"
    }
}
