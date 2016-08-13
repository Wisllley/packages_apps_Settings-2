 /*
 * copyright (C) 2013 The CyanogenMod project
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

package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SystemSettings";
    
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String KEY_VOLUME_WAKE_DEVICE = "volume_key_wake_device";    

    private static final String CATEGORY_VOLUME = "volume_keys";
   
    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mVolumeKeyWakeControl;
  
    private PreferenceCategory mNavigationPreferencesCat;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        boolean hasAnyBindableKey = false;
        
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);

        mHandler = new Handler();
 
        if (Utils.hasVolumeRocker(getActivity())) {
            int cursorControlAction = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);
            int wakeControlAction = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_ROCKER_WAKE, 0);
            mVolumeKeyWakeControl = initSwitch(KEY_VOLUME_WAKE_DEVICE, (wakeControlAction == 1));
         }else {
            prefScreen.removePreference(volumeCategory);
        }
    }
    
    private SwitchPreference initSwitch(String key, boolean checked) {
        SwitchPreference switchPreference = (SwitchPreference) getPreferenceManager()
                .findPreference(key);
        if (switchPreference != null) { 
           switchPreference.setChecked(checked);
            switchPreference.setOnPreferenceChangeListener(this);
        }
        return switchPreference;
    }

    private void handleSwitchChange(SwitchPreference pref, Object newValue, String setting) {
        Boolean value = (Boolean) newValue;
        int intValue = (value) ? 1 : 0;
        Settings.System.putInt(getContentResolver(), setting, intValue);
    }
 
    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
 		if (preference == mVolumeKeyCursorControl) {
            handleActionListChange(mVolumeKeyCursorControl, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference == mVolumeKeyWakeControl) {
            handleSwitchChange(mVolumeKeyWakeControl, newValue,
                    Settings.System.VOLUME_ROCKER_WAKE);
            return true;
        }
        return false;
    }
}
