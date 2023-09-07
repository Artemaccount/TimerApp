package ru.artemaccount.activitylifecycle

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceChangeListener

class SettingsFragment : PreferenceFragmentCompat(),
    OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        val editTextPreference: EditTextPreference? =
            preferenceManager.findPreference("default_interval")
        editTextPreference?.setOnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

        }
        editTextPreference?.onPreferenceChangeListener = this
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.timer_preferences)

        preferenceScreen.forEach {
            if (it is ListPreference) {
                setPreferenceLabel(it)
            }
            if (it is EditTextPreference) {
                setPreferenceLabel(it)
            }
        }
    }

    private fun setPreferenceLabel(pref: ListPreference) {
        val value = preferenceScreen.sharedPreferences?.getString(pref.key, "")
        val index = pref.findIndexOfValue(value)
        if (index >= 0) pref.summary = pref.entries[index]
    }

    private fun setPreferenceLabel(pref: EditTextPreference) {
        pref.summary = pref.text
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1 != null) {
            val preference: Preference? = findPreference(p1)
            if (preference is ListPreference) {
                setPreferenceLabel(preference)
            }
            if (preference is EditTextPreference) {
                setPreferenceLabel(preference)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference.key == "default_interval")
            try {
                newValue to Int
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(context, "only number", Toast.LENGTH_LONG).show()
            }
        return true
    }
}