package com.taskmaster.taller3.data.task

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferencias globales de configuración de la app.
 *
 * Almacena preferencias de usuario como:
 * - Nombre del usuario (para personalización del saludo).
 * - Tema de la app (claro/oscuro).
 * - Número de recordatorios enviados (estadística simple).
 *
 * Usa SharedPreferences directamente (no JSON, porque son valores primitivos).
 */
class TaskPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_REMINDERS_SENT = "reminders_sent"
        private const val KEY_SORT_ORDER = "sort_order"

        const val SORT_BY_DATE = 0
        const val SORT_BY_PRIORITY = 1
        const val SORT_BY_TITLE = 2
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- Nombre del usuario ----

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Usuario") ?: "Usuario"

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
    }

    // ---- Tema claro / oscuro ----

    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, false)

    fun setDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
    }

    // ---- Contador de recordatorios ----

    fun getRemindersSent(): Int = prefs.getInt(KEY_REMINDERS_SENT, 0)

    fun incrementRemindersSent() {
        prefs.edit().putInt(KEY_REMINDERS_SENT, getRemindersSent() + 1).apply()
    }

    // ---- Orden de clasificación ----

    fun getSortOrder(): Int = prefs.getInt(KEY_SORT_ORDER, SORT_BY_DATE)

    fun setSortOrder(order: Int) {
        prefs.edit().putInt(KEY_SORT_ORDER, order).apply()
    }
}
