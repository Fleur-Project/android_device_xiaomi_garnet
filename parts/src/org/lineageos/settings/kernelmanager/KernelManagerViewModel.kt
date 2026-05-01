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

package org.lineageos.settings.kernelmanager

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class KernelState(
    val availableGovernors: List<String> = emptyList(),
    val currentGovernor: String = "",
    val effAvailableFreqs: List<String> = emptyList(),
    val effMinFreq: String = "",
    val effMaxFreq: String = "",
    val perfAvailableFreqs: List<String> = emptyList(),
    val perfMinFreq: String = "",
    val perfMaxFreq: String = "",
)

class KernelManagerViewModel : ViewModel() {
    private val _state = MutableStateFlow(KernelState())
    val state: StateFlow<KernelState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        val govs = KernelManagerUtils.getAvailableGovernors()
        val curGov = KernelManagerUtils.getCurrentGovernor(KernelManagerUtils.EFFICIENCY_CLUSTER)
        
        val effFreqs = KernelManagerUtils.getAvailableFrequencies(KernelManagerUtils.EFFICIENCY_CLUSTER) ?: emptyList()
        val effMin = KernelManagerUtils.getCurrentMinFrequency(KernelManagerUtils.EFFICIENCY_CLUSTER)
        val effMax = KernelManagerUtils.getCurrentMaxFrequency(KernelManagerUtils.EFFICIENCY_CLUSTER)
        
        val perfFreqs = KernelManagerUtils.getAvailableFrequencies(KernelManagerUtils.PERFORMANCE_CLUSTER) ?: emptyList()
        val perfMin = KernelManagerUtils.getCurrentMinFrequency(KernelManagerUtils.PERFORMANCE_CLUSTER)
        val perfMax = KernelManagerUtils.getCurrentMaxFrequency(KernelManagerUtils.PERFORMANCE_CLUSTER)

        _state.update {
            it.copy(
                availableGovernors = govs,
                currentGovernor = curGov,
                effAvailableFreqs = effFreqs,
                effMinFreq = effMin,
                effMaxFreq = effMax,
                perfAvailableFreqs = perfFreqs,
                perfMinFreq = perfMin,
                perfMaxFreq = perfMax
            )
        }
    }

    fun updateGovernor(gov: String) {
        _state.update { it.copy(currentGovernor = gov) }
    }

    fun updateEffMinFreq(freq: String) {
        _state.update { it.copy(effMinFreq = freq) }
    }

    fun updateEffMaxFreq(freq: String) {
        _state.update { it.copy(effMaxFreq = freq) }
    }

    fun updatePerfMinFreq(freq: String) {
        _state.update { it.copy(perfMinFreq = freq) }
    }

    fun updatePerfMaxFreq(freq: String) {
        _state.update { it.copy(perfMaxFreq = freq) }
    }

    fun applySettings() {
        val s = state.value
        KernelManagerUtils.setGovernor(s.currentGovernor)
        KernelManagerUtils.setFrequencyRange(KernelManagerUtils.EFFICIENCY_CLUSTER, s.effMinFreq, s.effMaxFreq)
        KernelManagerUtils.setFrequencyRange(KernelManagerUtils.PERFORMANCE_CLUSTER, s.perfMinFreq, s.perfMaxFreq)
    }

    fun resetSettings() {
        KernelManagerUtils.resetToDefaults()
        loadSettings()
    }
}
