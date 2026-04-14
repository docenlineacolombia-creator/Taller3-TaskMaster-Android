package com.taskmaster.taller3

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Clase Application personalizada.
 * Se encarga de crear el canal de notificaciones al iniciar la app.
 */
class TaskMasterApp : Application() {

    companion object {
        /** ID del canal de notificaciones para recordatorios de tareas */
        const val REMINDER_CHANNEL_ID = "task_reminder_channel"
        const val REMINDER_CHANNEL_NAME = "Recordatorios de Tareas"
        const val REMINDER_CHANNEL_DESC = "Notificaciones para recordar tareas pendientes"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Crea los canales de notificación necesarios para Android 8+ (API 26+).
     * Sin un canal registrado, las notificaciones no se muestran en dispositivos modernos.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = REMINDER_CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
