package com.taskmaster.taller3.ui.task

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.taskmaster.taller3.R
import com.taskmaster.taller3.databinding.ActivityMainBinding
import com.taskmaster.taller3.data.task.TaskPreferences

/**
 * MainActivity – Single Activity Architecture.
 *
 * Esta Activity actúa como host del NavHostFragment.
 * Toda la navegación ocurre entre Fragments; la Activity no tiene lógica de negocio.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Launcher para solicitar permiso de notificaciones (Android 13+)
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Si el usuario niega el permiso, los recordatorios no mostrarán notificación.
            // La app sigue funcionando con las demás características.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // IMPORTANTE: super.onCreate() debe llamarse PRIMERO.
        // Aplicar el tema después de super pero antes de setContentView.
        super.onCreate(savedInstanceState)

        // Aplicar tema guardado en preferencias ANTES de inflar el layout
        val prefs = TaskPreferences(this)
        if (prefs.isDarkTheme()) {
            setTheme(R.style.Theme_TaskMaster_Dark)
        }
        // El tema claro (Theme.TaskMaster) ya está declarado en AndroidManifest,
        // no es necesario setearlo explícitamente salvo para el tema oscuro.

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        requestNotificationPermissionIfNeeded()
    }

    private fun setupNavigation() {
        // Obtener referencia al NavHostFragment desde el layout
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar la AppBar para que muestre el título del destino actual
        // y el botón "← Atrás" automáticamente
        val appBarConfig = AppBarConfiguration(
            setOf(R.id.taskListFragment) // Destinos de nivel superior (sin flecha atrás)
        )
        setupActionBarWithNavController(navController, appBarConfig)
    }

    /**
     * Delega el botón "Atrás" de la AppBar al NavController.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Solicita el permiso POST_NOTIFICATIONS en Android 13+ (API 33+).
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
