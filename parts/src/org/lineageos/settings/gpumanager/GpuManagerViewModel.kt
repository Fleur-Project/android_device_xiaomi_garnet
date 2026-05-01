/*
 * Copyright (C) 2025 KamiKaonashi
 *           (C) 2026 zylhdrXP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package org.lineageos.settings.gpumanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GpuManagerViewModel : ViewModel() {

    private val gpuUtils = GpuManagerUtils()

    data class GpuState(
        val gpuModel: String = "",
        val currentGovernor: String = "",
        val availableGovernors: List<String> = emptyList(),
        val currentMinFreq: String = "",
        val currentMaxFreq: String = "",
        val availableFrequencies: List<String> = emptyList(),
        val currentFreq: String = "",
        val busyPercentage: String = "",
        val temperature: String = "",
        val thermalPowerLevel: String = "",
        val forceClkOn: Boolean = false,
        val forceBusOn: Boolean = false,
        val forceRailOn: Boolean = false,
        val forceNoNap: Boolean = false,
        val busSplit: Boolean = false
    )

    private val _uiState = MutableStateFlow(GpuState())
    val uiState: StateFlow<GpuState> = _uiState.asStateFlow()

    init {
        loadInitialState()
        startPeriodicUpdates()
    }

    private fun loadInitialState() {
        _uiState.update {
            it.copy(
                gpuModel = gpuUtils.getGpuModel(),
                currentGovernor = gpuUtils.getCurrentGovernor(),
                availableGovernors = gpuUtils.getAvailableGovernors().toList(),
                currentMinFreq = gpuUtils.getCurrentMinFrequency(),
                currentMaxFreq = gpuUtils.getCurrentMaxFrequency(),
                availableFrequencies = gpuUtils.getAvailableFrequencies()?.toList() ?: emptyList(),
                forceClkOn = gpuUtils.getForceClkOn(),
                forceBusOn = gpuUtils.getForceBusOn(),
                forceRailOn = gpuUtils.getForceRailOn(),
                forceNoNap = gpuUtils.getForceNoNap(),
                busSplit = gpuUtils.getBusSplit()
            )
        }
        updateDynamicInfo()
    }

    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            while (true) {
                updateDynamicInfo()
                delay(2000)
            }
        }
    }

    private fun updateDynamicInfo() {
        _uiState.update {
            it.copy(
                currentFreq = gpuUtils.getCurrentFrequency(),
                busyPercentage = gpuUtils.getGpuBusyPercentage(),
                temperature = gpuUtils.getGpuTemperature(),
                thermalPowerLevel = gpuUtils.getThermalPowerLevel()
            )
        }
    }

    fun setGovernor(governor: String) {
        _uiState.update { it.copy(currentGovernor = governor) }
    }

    fun setMinFrequency(freq: String) {
        _uiState.update { it.copy(currentMinFreq = freq) }
    }

    fun setMaxFrequency(freq: String) {
        _uiState.update { it.copy(currentMaxFreq = freq) }
    }

    fun setForceClkOn(enabled: Boolean) {
        _uiState.update { it.copy(forceClkOn = enabled) }
    }

    fun setForceBusOn(enabled: Boolean) {
        _uiState.update { it.copy(forceBusOn = enabled) }
    }

    fun setForceRailOn(enabled: Boolean) {
        _uiState.update { it.copy(forceRailOn = enabled) }
    }

    fun setForceNoNap(enabled: Boolean) {
        _uiState.update { it.copy(forceNoNap = enabled) }
    }

    fun setBusSplit(enabled: Boolean) {
        _uiState.update { it.copy(busSplit = enabled) }
    }

    fun applySettings() {
        val state = _uiState.value
        gpuUtils.setGovernor(state.currentGovernor)
        gpuUtils.setFrequencyRange(state.currentMinFreq, state.currentMaxFreq)
        gpuUtils.setForceClkOn(state.forceClkOn)
        gpuUtils.setForceBusOn(state.forceBusOn)
        gpuUtils.setForceRailOn(state.forceRailOn)
        gpuUtils.setForceNoNap(state.forceNoNap)
        gpuUtils.setBusSplit(state.busSplit)
    }

    fun resetSettings() {
        gpuUtils.resetToDefaults()
        loadInitialState()
    }
}
