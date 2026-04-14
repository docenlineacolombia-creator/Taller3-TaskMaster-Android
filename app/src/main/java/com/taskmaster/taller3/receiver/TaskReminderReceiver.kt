package com.taskmaster.taller3.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.taskmaster.taller3.R
import com.taskmaster.taller3.TaskMasterApp
import com.taskmaster.taller3.ui.task.MainActivity

/**
 * TaskReminderReceiver – BroadcastReceiver para recordatorios de tareas.
 *
 * Se dispara cuando el AlarmManager envía el PendingIntent programado
 * desde TaskDetailFragment al guardar una tarea con recordatorio activado.
 *
 * Al recibir el broadcast:
 * 1. Extrae el ID, título y descripción de la tarea desde los extras del Intent.
 * 2. Construye una notificación local con estilo BigTextStyle.
 * 3. La muestra usando NotificationManager en el canal REMINDER_CHANNEL_ID.
 *
 * El Receiver está declarado en AndroidManifest.xml con android:exported="false"
 * porque solo se invoca internamente desde la propia app.
 */
class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        /** Extras que se pasan desde TaskDetailFragment al programar la alarma */
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Extraer datos de la tarea desde el Intent
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Recordatorio"
        val taskDesc = intent.getStringExtra(EXTRA_TASK_DESC) ?: ""

        // Crear un Intent para abrir la app cuando el usuario toca la notificación
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, TaskMasterApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)       // ikon de la notificación
            .setContentTitle("📌 $taskTitle")               // Título con emoji
            .setContentText(taskDesc.ifEmpty { "\u00a1Tienes una tarea pendiente!" })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskDesc.ifEmpty { "\u00a1No olvides completar tu tarea!" })
                    .setBigContentTitle("📌 Recordatorio: $taskTitle")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Alta prioridad = heads-up
            .setAutoCancel(true)                             // Se borra al tocarla
            .setContentIntent(pendingIntent)                 // Abrir app al tocar
            .setVibrate(longArrayOf(0, 250, 250, 250))       // Patrón de vibración
            .build()

        // Mostrar la notificación (ID único por tarea para no solaparse)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId, notification)
    }
}
