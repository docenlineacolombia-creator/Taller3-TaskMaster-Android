package com.taskmaster.taller3.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.taskmaster.taller3.data.task.TaskPreferences
import com.taskmaster.taller3.databinding.FragmentSettingsBinding

/**
 * SettingsFragment – Configuración de la app.
 *
 * Permite al usuario:
 * - Cambiar su nombre (se persiste en TaskPreferences / SharedPreferences).
 * - Alternar entre tema claro y oscuro.
 * - Seleccionar el orden de clasificación de las tareas.
 * - Ver estadísticas básicas (recordatorios enviados).
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentSettings()
        setupSaveNameButton()
        setupThemeSwitch()
        setupSortOrderGroup()
        loadStats()
    }

    private fun loadCurrentSettings() {
        val prefs = viewModel.preferences
        binding.etUserName.setText(prefs.getUserName())
        binding.switchDarkTheme.isChecked = prefs.isDarkTheme()

        // Establecer el RadioButton correcto según el orden guardado
        when (prefs.getSortOrder()) {
            TaskPreferences.SORT_BY_PRIORITY -> binding.rbSortPriority.isChecked = true
            TaskPreferences.SORT_BY_TITLE -> binding.rbSortTitle.isChecked = true
            else -> binding.rbSortDate.isChecked = true
        }
    }

    private fun setupSaveNameButton() {
        binding.btnSaveName.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilUserName.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }
            binding.tilUserName.error = null
            viewModel.preferences.setUserName(name)
            Toast.makeText(requireContext(), "✅ Nombre guardado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupThemeSwitch() {
        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.preferences.setDarkTheme(isChecked)
            // Reiniciar la Activity para aplicar el tema
            requireActivity().recreate()
        }
    }

    private fun setupSortOrderGroup() {
        binding.rgSortOrder.setOnCheckedChangeListener { _, checkedId ->
            val order = when (checkedId) {
                binding.rbSortPriority.id -> TaskPreferences.SORT_BY_PRIORITY
                binding.rbSortTitle.id -> TaskPreferences.SORT_BY_TITLE
                else -> TaskPreferences.SORT_BY_DATE
            }
            viewModel.preferences.setSortOrder(order)
            viewModel.setFilter(viewModel.filterLiveData.value ?: TaskViewModel.FILTER_ALL)
            Toast.makeText(requireContext(), "Orden actualizado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStats() {
        val remindersSent = viewModel.preferences.getRemindersSent()
        binding.tvReminderStats.text = "Recordatorios programados: $remindersSent"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
