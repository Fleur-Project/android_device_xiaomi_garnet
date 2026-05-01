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

import java.io.File

object KernelManagerUtils {
    const val EFFICIENCY_CLUSTER = 0
    const val PERFORMANCE_CLUSTER = 4
    
    private val POLICIES = intArrayOf(EFFICIENCY_CLUSTER, PERFORMANCE_CLUSTER)
    private const val DEFAULT_GOVERNOR = "schedutil"
    
    private const val CPU_BASE_PATH = "/sys/devices/system/cpu/cpufreq/policy"
    private const val SCALING_GOVERNOR = "/scaling_governor"
    private const val SCALING_MIN_FREQ = "/scaling_min_freq"
    private const val SCALING_MAX_FREQ = "/scaling_max_freq"
    private const val SCALING_AVAILABLE_GOVERNORS = "/scaling_available_governors"
    private const val SCALING_AVAILABLE_FREQUENCIES = "/scaling_available_frequencies"

    fun getAvailableGovernors(): List<String> {
        return try {
            readFile(CPU_BASE_PATH + EFFICIENCY_CLUSTER + SCALING_AVAILABLE_GOVERNORS)
                .trim().split("\\s+".toRegex())
        } catch (e: Exception) {
            listOf("schedutil", "performance", "powersave", "ondemand", "conservative")
        }
    }

    fun getAvailableFrequencies(cluster: Int): List<String>? {
        return try {
            readFile(CPU_BASE_PATH + cluster + SCALING_AVAILABLE_FREQUENCIES)
                .trim().split("\\s+".toRegex())
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentGovernor(cluster: Int): String {
        return try {
            readFile(CPU_BASE_PATH + cluster + SCALING_GOVERNOR).trim()
        } catch (e: Exception) {
            DEFAULT_GOVERNOR
        }
    }

    fun getCurrentMinFrequency(cluster: Int): String {
        return try {
            readFile(CPU_BASE_PATH + cluster + SCALING_MIN_FREQ).trim()
        } catch (e: Exception) {
            getAvailableFrequencies(cluster)?.firstOrNull() ?: "0"
        }
    }

    fun getCurrentMaxFrequency(cluster: Int): String {
        return try {
            readFile(CPU_BASE_PATH + cluster + SCALING_MAX_FREQ).trim()
        } catch (e: Exception) {
            getAvailableFrequencies(cluster)?.lastOrNull() ?: "0"
        }
    }

    fun setGovernor(governor: String) {
        for (cluster in POLICIES) {
            try {
                writeFile(CPU_BASE_PATH + cluster + SCALING_GOVERNOR, governor)
            } catch (e: Exception) {
                // Continue
            }
        }
    }

    fun setFrequencyRange(cluster: Int, minFreq: String, maxFreq: String) {
        try {
            writeFile(CPU_BASE_PATH + cluster + SCALING_MIN_FREQ, minFreq)
            writeFile(CPU_BASE_PATH + cluster + SCALING_MAX_FREQ, maxFreq)
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    fun resetToDefaults() {
        setGovernor(DEFAULT_GOVERNOR)
        for (cluster in POLICIES) {
            val frequencies = getAvailableFrequencies(cluster)
            if (!frequencies.isNullOrEmpty()) {
                setFrequencyRange(cluster, frequencies.first(), frequencies.last())
            }
        }
    }

    private fun readFile(path: String): String {
        return File(path).readText()
    }

    private fun writeFile(path: String, value: String) {
        File(path).writeText(value)
    }
}
