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

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aplicar tema oscuro si está activado en preferencias
        val prefs = TaskPreferences(this)
        if (prefs.isDarkTheme()) {
            setTheme(R.style.Theme_TaskMaster_Dark)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Requerido porque el tema usa NoActionBar:
        // la Toolbar del layout debe registrarse como ActionBar
        setSupportActionBar(binding.toolbar)

        setupNavigation()
        requestNotificationPermissionIfNeeded()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(
            setOf(R.id.taskListFragment)
        )
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

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
