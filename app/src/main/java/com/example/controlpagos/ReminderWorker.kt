package com.example.controlpagos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        val titulo = inputData.getString("titulo") ?: "Recordatorio"
        val mensaje = inputData.getString("mensaje") ?: "Tienes un pago pendiente"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "pagos_channel"

        val channel = NotificationChannel(
            channelId,
            "Recordatorios de pagos",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        notificationManager.notify((0..1000).random(), notification)

        return Result.success()
    }
}
