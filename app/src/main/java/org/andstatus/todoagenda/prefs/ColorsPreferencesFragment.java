package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.rarepebble.colorpicker.ColorPreference;
import com.rarepebble.colorpicker.ColorPreferenceDialog;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.TextShading;
import org.andstatus.todoagenda.widget.TimeSection;

import static org.andstatus.todoagenda.WidgetConfigurationActivity.FRAGMENT_TAG;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DIFFERENT_COLORS_FOR_DARK;

/** AndroidX version created by yvolk@yurivolkov.com
 *   based on this answer: https://stackoverflow.com/a/53290775/297710
 *   and on the code of https://github.com/koji-1009/ChronoDialogPreference
 */
public class ColorsPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setTitle();
        addPreferencesFromResource(R.xml.preferences_colors);
        removeUnavailablePreferences();
    }

    private void setTitle() {
        ApplicationPreferences.getEditingColorThemeType(getActivity()).setTitle(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        removeUnavailablePreferences();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        showShadings();
    }

    private void removeUnavailablePreferences() {
        Context context = getActivity();
        if (context == null) return;

        ColorThemeType colorThemeType = ApplicationPreferences.getColorThemeType(context);
        if (!ColorThemeType.canHaveDifferentColorsForDark() ||
                colorThemeType == ColorThemeType.LIGHT ||
                colorThemeType == ColorThemeType.SINGLE && !InstanceSettings.isDarkThemeOn(context)) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(PREF_DIFFERENT_COLORS_FOR_DARK);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
        if (ApplicationPreferences.noPastEvents(context)) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(TimeSection.PAST.preferenceCategoryKey);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
    }

    private void showShadings() {
        for (TextShadingPref shadingPref : TextShadingPref.values()) {
            showShading(shadingPref);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_DIFFERENT_COLORS_FOR_DARK.equals(key)){
            FragmentActivity activity = getActivity();
            if (activity != null) {
                if (ApplicationPreferences.getEditingColorThemeType(activity) == ColorThemeType.NONE) {
                    activity.startActivity(MainActivity.intentToConfigure(activity, ApplicationPreferences.getWidgetId(activity)));
                    activity.finish();
                    return;
                };
                setTitle();
            }
        }
        showShadings();
    }

    private void showShading(TextShadingPref prefs) {
        ListPreference preference = (ListPreference) findPreference(prefs.preferenceName);
        if (preference != null) {
            TextShading shading = TextShading.fromName(preference.getValue(), prefs.defaultShading);
            preference.setSummary(getActivity().getString(shading.titleResId));
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ColorPreference) {
            dialogFragment = new ColorPreferenceDialog((ColorPreference) preference);
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}