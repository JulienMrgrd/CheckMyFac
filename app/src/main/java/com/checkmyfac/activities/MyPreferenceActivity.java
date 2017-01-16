package com.checkmyfac.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.checkmyfac.R;
import com.checkmyfac.dao.pointInteret.PointInteretDAO;
import com.checkmyfac.dao.transport.TransportDAO;
import com.checkmyfac.properties.PropertiesLoader;
import com.checkmyfac.properties.PropertiesLoaderInterface;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CheckMyFacUtils;
import com.checkmyfac.utils.ColorUtils.ColorUtils;

import java.util.HashSet;
import java.util.Set;

import static com.checkmyfac.utils.CheckMyFacConstants.PREF_HAS_CHANGED;

public class MyPreferenceActivity extends AppCompatActivity {   // Activite gerant les parametres

	private static final String TAG = "MyPreferenceActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate !");
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment()).commit();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	public static class PrefFragment extends PreferenceFragment implements OnPreferenceChangeListener{

		private Context context;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.parametres);
			context = getActivity();
			final SharedPreferences prefs = getSharedPreferences();

			////////////////////// TYPE DE CARTE
			Preference pref_map = findPreference(CheckMyFacConstants.TYPE_MAP);
			pref_map.setOnPreferenceChangeListener(this);

			////////////////////// CACHE UTILISATEUR
			Preference pref_cache = findPreference(CheckMyFacConstants.CACHE_SYSTEM);
			pref_cache.setTitle(pref_cache.getTitle() + " (" +
					CheckMyFacUtils.getSizeOfCacheUser(context, CheckMyFacUtils.FORMAT_MO) + ')');
			pref_cache.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage(getString(R.string.VidCachConfirm))
							.setCancelable(true)
							.setPositiveButton(getString(R.string.Val), new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog, final int id) {
									CheckMyFacUtils.clearApplicationData(context, prefs);
									if (prefs.getBoolean(CheckMyFacConstants.PREF_MESSAGE, true)) {
										Toast.makeText(context, "\t\t\t\t\t"+getString(R.string.CachSup), Toast.LENGTH_SHORT).show();
									}
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();

					return true;
				}
			});

			////////////////////// CACHE DE LA MAP
			Preference pref_cache_MAP = findPreference(CheckMyFacConstants.CACHE_MAP);
			pref_cache_MAP.setTitle(pref_cache_MAP + " ("+ CheckMyFacUtils.getSizeOfCacheMap(context, CheckMyFacUtils.FORMAT_MO) +')');
			pref_cache_MAP.setOnPreferenceClickListener(new OnPreferenceClickListener() {   // Si on clique sur le bouton "vider les caches de la carte"

				@Override
				public boolean onPreferenceClick(Preference preference) {

					final AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage(getString(R.string.VidCachMapConfirm))
							.setCancelable(true)
							.setNegativeButton(R.string.Annul, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							})
							.setPositiveButton(getString(R.string.Val), new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog, final int id) {
									CheckMyFacUtils.clearCacheMap(context);
									if (prefs.getBoolean(CheckMyFacConstants.PREF_MESSAGE, true))
										Toast.makeText(context, "\t\t\t\t\t"+getString(R.string.CachSup), Toast.LENGTH_SHORT).show();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();

					return true;
				}
			});

			////////////////////// CHANGEMENT DE FAC
			Preference pref_changeFac = findPreference(CheckMyFacConstants.CHANGE_FAC);
			String fac = prefs.getString(CheckMyFacConstants.THE_FAC,"");
			if(fac.isEmpty()) pref_changeFac.setTitle(pref_changeFac.getTitle());
			else pref_changeFac.setTitle(pref_changeFac.getTitle()+" ("+fac+')');

			pref_changeFac.setOnPreferenceClickListener(new OnPreferenceClickListener() {   // Si on clique sur le bouton "vider les caches de la carte"

				@Override
				public boolean onPreferenceClick(Preference preference) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage(getString(R.string.EtesVousSur))
							.setCancelable(true)
							.setNegativeButton(R.string.Annul, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							})
							.setPositiveButton(getString(R.string.Oui), new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog, final int id) {
									CheckMyFacUtils.changeFacCleaner(context, prefs);
									launchChoiceActivity();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();

					return true;
				}
			});


			////////////////////////// MESSAGES
			final CheckBoxPreference check_pref_msg = (CheckBoxPreference) findPreference(CheckMyFacConstants.MSG);
			check_pref_msg.setOnPreferenceClickListener(new OnPreferenceClickListener() { // Si on clique sur la Checkbox "Affichage des messages" (sous-entendu Toast)
				@Override
				public boolean onPreferenceClick(Preference preference) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean(CheckMyFacConstants.PREF_MESSAGE, check_pref_msg.isChecked());    // Met un boolean a vrai/faux, et permet de bloquer les affichages de Toast dans les activites
					editor.apply();
					return false;
				}
			});


			//////////////////////// COULEURS
			PropertiesLoaderInterface loader = new PropertiesLoader(context);

			{
				ListPreference couleurDistr = (ListPreference) findPreference(CheckMyFacConstants.TYPE_DISTR);
				if (couleurDistr.getValue() == null) {
					String colorDistr = prefs.getString(CheckMyFacConstants.PREF_COLOR_DISTR, null);
					if (colorDistr == null) colorDistr = loader.getCouleurDistr();
					if (!colorDistr.isEmpty()) couleurDistr.setValue(colorDistr);
				}
				// Pour l'initialisation des icones au lancement de l'activite
				couleurDistr.setIcon(ColorUtils.getIcon(couleurDistr.getValue(), couleurDistr.getKey()));
				couleurDistr.setOnPreferenceChangeListener(this);
			}
			{
				ListPreference couleurBU = (ListPreference) findPreference(CheckMyFacConstants.TYPE_BU);
				if (couleurBU.getValue() == null) {
					String colorBu = prefs.getString(CheckMyFacConstants.PREF_COLOR_BU, null);
					if (colorBu == null) colorBu = loader.getCouleurBu();
					if (!colorBu.isEmpty()) couleurBU.setValue(colorBu);
				}
				couleurBU.setIcon(ColorUtils.getIcon(couleurBU.getValue(), couleurBU.getKey()));
				couleurBU.setOnPreferenceChangeListener(this);
			}
			{
				ListPreference couleurRest = (ListPreference) findPreference(CheckMyFacConstants.TYPE_REST);
				if (couleurRest.getValue() == null) {
					String colorRest = prefs.getString(CheckMyFacConstants.PREF_COLOR_REST, null);
					if (colorRest == null) colorRest = loader.getCouleurRest();
					if (!colorRest.isEmpty()) couleurRest.setValue(colorRest);
				}
				couleurRest.setIcon(ColorUtils.getIcon(couleurRest.getValue(), couleurRest.getKey()));
				couleurRest.setOnPreferenceChangeListener(this);
			}
			{
				ListPreference couleurBus = (ListPreference) findPreference(CheckMyFacConstants.TYPE_BUS);
				if (couleurBus.getValue() == null) {
					String colorBus = prefs.getString(CheckMyFacConstants.PREF_COLOR_BUS, null);
					if (colorBus == null) colorBus = loader.getCouleurBus();
					if (!colorBus.isEmpty()) couleurBus.setValue(colorBus);
				}
				couleurBus.setIcon(ColorUtils.getIconTransports(couleurBus.getValue(), couleurBus.getKey()));
				couleurBus.setOnPreferenceChangeListener(this);
			}
			{
				ListPreference couleurMetro = (ListPreference) findPreference(CheckMyFacConstants.TYPE_METRO);
				if (couleurMetro.getValue() == null) {
					String colorMetro = prefs.getString(CheckMyFacConstants.PREF_COLOR_METRO, null);
					if (colorMetro == null) colorMetro = loader.getCouleurMetro();
					if (!colorMetro.isEmpty()) couleurMetro.setValue(colorMetro);
				}
				couleurMetro.setIcon(ColorUtils.getIconTransports(couleurMetro.getValue(), couleurMetro.getKey()));
				couleurMetro.setOnPreferenceChangeListener(this);
			}
		}

		/**
		 * Lance la page de choix d'universite si les caches sont vidés.
		 */
		private void launchChoiceActivity() {
			Intent intent = new Intent(context, ChoiceActivity.class);
			startActivity(intent);
			Log.d(TAG, "launchChoiceActivity");
			((Activity)context).finish();
		}

		/**
		 * Changement de l'icone d'un type de point d'intérêt
		 * @param couleur nouvel couleur du point d'intérêt
         * @param type type du point d'intérêt (BU, distributeur, ...)
         */
		private void changementIconEtColorPI(ListPreference preference, final String couleur, final String type) {
			final PointInteretDAO ptsInteretsDAO = new PointInteretDAO(context); // database utilisateur
			if (!ptsInteretsDAO.isEmpty(false)) {
				preference.setIcon(ColorUtils.getIcon(couleur, type)); // Change l'icone de l'item

				new Thread(new Runnable() {
					@Override
					public void run() {
						ptsInteretsDAO.updateAllColorPILikeType(couleur, type);    // Change la couleur des PI de ce type en base
						String pref_color = ColorUtils.getPrefColorByType(type);
						if (pref_color!=null && !pref_color.isEmpty()) {
							SharedPreferences prefs = getSharedPreferences();
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(pref_color, couleur);  // Met les preferences à jour avec la couleur choisie
							editor.apply();
						}
					}
				}).start();
			}
		}

		/**
		 * Changement de l'icone d'un type de transport
		 * @param couleur nouvel couleur du marker
		 * @param type type de transport (métro, bus, ...)
		 */
		private void changementIconEtColorTransports(ListPreference preference, final String couleur, final String type) {

			final TransportDAO transportsDAO = new TransportDAO(context); // database utilisateur
			if (!transportsDAO.isEmpty()) {
				preference.setIcon(ColorUtils.getIconTransports(couleur, type)); // Change l'icone de l'item

				new Thread(new Runnable() {
					@Override
					public void run() {
						transportsDAO.updateAllColorLikeType(couleur, type);    // Change la couleur des transports de ce type en base
						String pref_color = ColorUtils.getPrefColorByType(type);
						if (pref_color != null && !pref_color.isEmpty()) {
							SharedPreferences prefs = getSharedPreferences();
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(pref_color, couleur);  // Met les preferences à jour avec la couleur choisie
							editor.apply();
						}
					}
				}).start();
			}
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			Log.d(TAG, "onPreferenceChange :"+key);
			addNewHasChangedPreference(key);

			if(CheckMyFacUtils.isAPointType(key)) {
				// pour éviter de duppliquer le code pour chaque préference concernant les couleurs de points
				if (key.equalsIgnoreCase(CheckMyFacConstants.TYPE_METRO) || key.equalsIgnoreCase(CheckMyFacConstants.TYPE_BUS)) {
					changementIconEtColorTransports((ListPreference) preference, (String) newValue, preference.getKey());
				} else {
					changementIconEtColorPI((ListPreference) preference, (String) newValue, preference.getKey());
				}
			}
			return true;
		}

		private SharedPreferences getSharedPreferences(){
			return context.getSharedPreferences(CheckMyFacConstants.SHARED_PREFERENCES, MODE_PRIVATE);
		}

		/** Permet à MapActivity d'effectuer des modifications (vue de la map, couleurs, etc ...)
		 * sans recharger l'application */
		private void addNewHasChangedPreference(String key){
			if(key!=null && !key.isEmpty()) {
				SharedPreferences prefs = getSharedPreferences();
				SharedPreferences.Editor editor = prefs.edit();
				Set<String> changedPrefs = prefs.getStringSet(PREF_HAS_CHANGED, new HashSet<String>(1));
				changedPrefs.add(key);
				editor.putStringSet(PREF_HAS_CHANGED, changedPrefs);
				editor.apply();
			}
		}

	}

}
