/*
 * Copyright (C) 2025 KamiKaonashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.garnetparts;

import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;
import org.lineageos.settings.R;
import org.lineageos.settings.corecontrol.CoreControlActivity;
import org.lineageos.settings.charge.ChargeActivity;
import org.lineageos.settings.kernelmanager.KernelManagerActivity;
import org.lineageos.settings.gpumanager.GpuManagerActivity;
import org.lineageos.settings.saturation.SaturationActivity;
import org.lineageos.settings.refreshrate.RefreshSettingsActivity;
import org.lineageos.settings.speaker.ClearSpeakerActivity;
import org.lineageos.settings.thermal.ThermalComposeActivity;

public class GarnetPartsFragment extends SettingsBasePreferenceFragment {

    private static final String KEY_CORE_CONTROL = "core_control";
    private static final String KEY_BYPASS_CHARGE = "bypass_charge";
    private static final String KEY_KERNEL_MANAGER = "kernel_manager";
    private static final String KEY_GPU_MANAGER = "gpu_manager";
    private static final String KEY_SATURATION = "saturation";
    private static final String KEY_CLEAR_SPEAKER = "clear_speaker_pref";
    private static final String KEY_THERMAL = "thermal_enable";
    private static final String KEY_REFRESHRATE = "refreshrate";
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.garnet_parts_settings, rootKey);

        // Core Control preference
        Preference coreControlPref = findPreference(KEY_CORE_CONTROL);
        if (coreControlPref != null) {
            coreControlPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), CoreControlActivity.class);
                startActivity(intent);
                return true;
            });
        }

        // Bypass Charge preference
        Preference chargePref = findPreference(KEY_BYPASS_CHARGE);
        if (chargePref != null) {
            chargePref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), ChargeActivity.class);
                startActivity(intent);
                return true;
            });
        }
        
        // Kernel Manager preference
        Preference kernelManagerPref = findPreference(KEY_KERNEL_MANAGER);
        if (kernelManagerPref != null) {
            kernelManagerPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), KernelManagerActivity.class);
                startActivity(intent);
                return true;
            });
        }
        
        // GPU Manager preference
        Preference gpuManagerPref = findPreference(KEY_GPU_MANAGER);
        if (gpuManagerPref != null) {
            gpuManagerPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), GpuManagerActivity.class);
                startActivity(intent);
                return true;
            });
        }
        
        // Saturation preference
        Preference saturationPref = findPreference(KEY_SATURATION);
        if (saturationPref != null) {
            saturationPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), SaturationActivity.class);
                startActivity(intent);
                return true;
            });
        }
        
        // Clear Speaker preference
        Preference clearSpeakerPref = findPreference(KEY_CLEAR_SPEAKER);
        if (clearSpeakerPref != null) {
            clearSpeakerPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), ClearSpeakerActivity.class);
                startActivity(intent);
                return true;
            });
        }
        
        // Refresh Rate preference
        Preference refreshSettingsPref = findPreference(KEY_REFRESHRATE);
        if (refreshSettingsPref != null) {
            refreshSettingsPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), RefreshSettingsActivity.class);
                startActivity(intent);
                return true;
            });
        }
        
        // Thermal preference
        Preference thermalComposePref = findPreference(KEY_THERMAL);
        if (thermalComposePref != null) {
            thermalComposePref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), ThermalComposeActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }
}
