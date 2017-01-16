package com.checkmyfac.dao.pointInteret;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.checkmyfac.dao.DAO;
import com.checkmyfac.objet.PointInteret;
import com.checkmyfac.properties.PropertiesLoaderInterface;
import com.checkmyfac.utils.CheckMyFacConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PointInteretDAO extends DAO {

	private static final String TAG = "PointInteretDAO";

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "pts_interets.db";
	private static final String TABLE_PI = "table_pi";
	private static final String COL_ID = "id";
	private static final int NUM_COL_ID = 0;
	private static final String COL_TYPE = "Type";
	private static final int NUM_COL_TYPE = 1;
	private static final String COL_NOM = "Nom";
	private static final int NUM_COL_NOM = 2;
	private static final String COL_DESC = "Description";
	private static final int NUM_COL_DESC = 3;
	private static final String COL_LAT = "Lat";
	private static final int NUM_COL_LAT = 4;
	private static final String COL_LON = "Lon";
	private static final int NUM_COL_LON = 5;
	private static final String COL_COUL = "Couleur";
	private static final int NUM_COL_COUL = 6;
	private static final String COL_VIS = "Visible";
	private static final int NUM_COL_VIS = 7;
	private static final String COL_DATE = "Date";
	private static final int NUM_COL_DATE = 8;

	private static final String CREATE_BDD = "CREATE TABLE " + TABLE_PI + " ("
			+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_TYPE +" TEXT NOT NULL, "
			+ COL_NOM + " TEXT NOT NULL, "
			+ COL_DESC +" TEXT NOT NULL, "
			+ COL_LAT + " REAL NOT NULL, "
			+ COL_LON + " REAL NOT NULL, "
			+ COL_COUL + " TEXT NOT NULL, "
			+ COL_VIS + " INTEGER NOT NULL, "
			+ COL_DATE + " TEXT NOT NULL );";

	public PointInteretDAO(Context context) {
		super(context, NOM_BDD, null, VERSION_BDD);
		Log.d(TAG, "create bdd PointInteret");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + TABLE_PI + ';');
		Log.d(TAG, "onUpgrade bdd");
		onCreate(db);
		db.setVersion(PointInteretDAO.VERSION_BDD);
	}

	/**
	 * Mise à jour des point d'intérêt par défaut à partir d'un fichier properties
     */
	public synchronized void updateDBWithProperties(PropertiesLoaderInterface loader,
													SharedPreferences prefs) {
		openDb();
		clearAllDefaultPts();
		if(loader!=null) loader.insertionInDBandPrefsFromProperties(prefs);
		closeDb();
	}

	private synchronized void clearAllDefaultPts() {
		openDb();
		mybase.delete(TABLE_PI, COL_TYPE + "!='" + CheckMyFacConstants.TYPE_USER + '\'', null);
		closeDb();
	}

	public synchronized void clearAllUserPts() {
		openDb();
		mybase.delete(TABLE_PI, COL_TYPE + "='" + CheckMyFacConstants.TYPE_USER +'\'', null);
		closeDb();
	}

	public synchronized void clearAll() {
		openDb();
		mybase.execSQL("DROP TABLE IF EXISTS " + TABLE_PI);
		onCreate(mybase);
		closeDb();
	}

	/**
	 * Retourne vrai si la base compte au moins un PointInteret. Si le paramètre est a vrai, la fonction compte les PointInteret de type USER,
	 * si faux, les autres types de PointInteret. Si null, la fonction compte tous les PointInteret.
	 */
	public synchronized boolean isEmpty(Boolean userPts) {
		openDb();
		String selector;
		if (userPts == null)
			selector = "SELECT COUNT(*) FROM TABLE_PI";
		else if (userPts)
			selector = "SELECT COUNT(*) FROM TABLE_PI WHERE " +COL_TYPE+ "='" +CheckMyFacConstants.TYPE_USER + '\'';
		else
			selector = "SELECT COUNT(*) FROM TABLE_PI WHERE " +COL_TYPE+ "!='" +CheckMyFacConstants.TYPE_USER + '\'';

		try(Cursor cur = mybase.rawQuery(selector, null)){
			if (cur != null) {
				cur.moveToFirst();
				if (cur.getInt(0) == 0) {
					return true;
				}
			}
		}
		closeDb();
		return false;
	}

	public synchronized void insertPI(PointInteret pi) {
		ContentValues values = getContentValuesPI(pi);
		openDb();
		pi.setId(Long.toString(mybase.insert(TABLE_PI, null, values)));
		closeDb();
	}

	/**
	 * Récupération d'un ContentValue à partir d'un point d'intérêt passé en argument
     */
	private static ContentValues getContentValuesPI(PointInteret pi) {
		if(pi==null) return null;

		ContentValues values = new ContentValues();

		if (pi.getType() == null) values.put(COL_TYPE, "");
		else values.put(COL_TYPE, escapeAllApostrophe(pi.getType()) );

		if (pi.getName() == null) values.put(COL_NOM, "");
		else values.put(COL_NOM, escapeAllApostrophe(pi.getName()) );

		if (pi.getDesc() == null) values.put(COL_DESC, "");
		else values.put(COL_DESC, escapeAllApostrophe(pi.getDesc()) );

		if (pi.getLat() == null) values.put(COL_LAT, "");
		else values.put(COL_LAT, pi.getLat());

		if (pi.getLon() == null) values.put(COL_LON, "");
		else values.put(COL_LON, pi.getLon());

		if (pi.getCoul() == null) values.put(COL_COUL, "");
		else values.put(COL_COUL, escapeAllApostrophe(pi.getCoul()));

		values.put(COL_VIS, pi.isVisible());

		if (pi.getDate() == null) values.put(COL_DATE, "");
		else values.put(COL_DATE, pi.getDate());
		return values;
	}

	/** Insertion d'une liste de point d'intérêt en base de donnée */
	public synchronized void insertListPI(List<PointInteret> pts) {
		openDb();
		mybase.beginTransaction(); // use with multiple insert

		try {
			for(PointInteret pi : pts) {
				pi.setId(Long.toString(mybase.insert(TABLE_PI, null, getContentValuesPI(pi))));
			}
			mybase.setTransactionSuccessful();
		}
		finally {
			mybase.endTransaction();
			closeDb();
		}
	}

	/** Mise à jour de la position d'un point d'intérêt en base de donnée */
	public synchronized int updatePIPosition(PointInteret pi) {
		ContentValues values = new ContentValues();
		if ( getPI(pi.getId()) == null ) return -1;

		if (pi.getLat() != null)  values.put(COL_LAT, pi.getLat());
		if (pi.getLon() != null) values.put(COL_LON, pi.getLon());

		openDb();
		int res = mybase.update(TABLE_PI, values, COL_ID + "='" + pi.getId() + '\'', null);
		closeDb();
		return res;
	}

	/** Mise à jour des données d'un point d'intérêt */
	public synchronized int updatePI(PointInteret oldPi, PointInteret newPi) {
		ContentValues values = new ContentValues();
		if (getPI(oldPi.getId()) == null) return -1;

		if (newPi.getType() != null) values.put(COL_TYPE, newPi.getType());
		if (newPi.getName() != null) values.put(COL_NOM, newPi.getName());
		if (newPi.getDesc() != null) values.put(COL_DESC, newPi.getDesc());
		if (newPi.getLat() != null) values.put(COL_LAT, newPi.getLat());
		if (newPi.getLon() != null) values.put(COL_LON, newPi.getLon());
		if (newPi.getCoul() != null) values.put(COL_COUL, newPi.getCoul());
		values.put(COL_VIS, newPi.isVisible());
		if (newPi.getDate() != null) values.put(COL_DATE, newPi.getDate());
		else values.put(COL_DATE, "");

		openDb();
		int res = mybase.update(TABLE_PI, values, COL_ID + '=' + oldPi.getId(), null);
		closeDb();
		return res;
	}

	/** Suppression d'un PI de la BDD */
	public synchronized int removePIWithNomAndDescr(String name, String desc) {
		openDb();
		int res = mybase.delete(TABLE_PI, COL_NOM + "='" + escapeAllApostrophe(name) + "' AND "
				+ COL_DESC + "='" + escapeAllApostrophe(desc) +'\'', null);
		closeDb();
		return res;
	}

	/** Retourne un PI par son id, null s'il n'existe pas */
	public synchronized PointInteret getPI(String id) {
		openDb();
		Cursor c = mybase.query(TABLE_PI, new String[]{COL_ID, COL_TYPE, COL_NOM, COL_DESC, COL_LAT, COL_LON, COL_COUL, COL_VIS, COL_DATE},
				COL_ID + '=' + id, null, null, null, null);

		PointInteret pi = cursorToPI(c);
		closeDb();
		return pi;
	}

	/** Retourne la couleur d'un PI à partir de son id si il existe, null sinon */
	public synchronized String getColorPI(String id) {
		String selectQuery = "SELECT "+COL_COUL+" FROM " + TABLE_PI + " WHERE "+COL_ID+'='+ id;

		openDb();
		String res = null;
		try(Cursor cursor = mybase.rawQuery(selectQuery, null)) {
			if (cursor.moveToFirst()) {
				res = cursor.getString(0);
			}
		}
		closeDb();
		return res;
	}

	/** Retourne tous les noms de PointInteret de ce type. Si type==null, alors on retourne tous les noms. */
	public synchronized List<String> getAllNamePiByType(String type){
		List<String> noms = new ArrayList<>();

		String selectQuery;
		if(type==null || type.isEmpty()) selectQuery = "SELECT "+COL_NOM+" FROM " + TABLE_PI;
		else selectQuery = "SELECT "+COL_NOM+" FROM " + TABLE_PI + " WHERE "+COL_TYPE+"='"+ escapeAllApostrophe(type.toUpperCase()) + '\'';

		openDb();
		try(Cursor cursor = mybase.rawQuery(selectQuery, null)) {
			if (cursor.moveToFirst()) {
				do {
					noms.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
		}
		closeDb();
		return noms;
	}

	/** Retourne la description de tous les PI du type passé en argument */
	public synchronized List<String> getAllDescPiByType(String type){
		List<String> noms = new ArrayList<>();

		String selectQuery = "SELECT "+COL_DESC+" FROM " + TABLE_PI + " WHERE "+COL_TYPE+"='"+ escapeAllApostrophe(type.toUpperCase()) + '\'';

		openDb();
		try(Cursor cursor = mybase.rawQuery(selectQuery, null)){
			if (cursor.moveToFirst()) {
				do {
					noms.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
		}
		closeDb();
		return noms;
	}

	/** Récupération de l'ensemble des points d'intérêts */
	public synchronized List<PointInteret> getAll() {
		openDb();
		Cursor c = mybase.query(TABLE_PI, new String[]{COL_ID, COL_TYPE, COL_NOM, COL_DESC, COL_LAT, COL_LON, COL_COUL, COL_VIS, COL_DATE}, null, null, null, null, null, null);
		List<PointInteret> res = cursorToPIList(c);
		closeDb();
		return res;
	}

	/** Récupération de l'ensemble des points d'intérêts d'un type passé en argument */
	public synchronized List<PointInteret> getAllPiByType(String type) {
		openDb();
		Cursor c = mybase.query(TABLE_PI, new String[]{COL_ID, COL_TYPE, COL_NOM, COL_DESC, COL_LAT, COL_LON, COL_COUL, COL_VIS, COL_DATE},
				COL_TYPE + "='" + type + '\'', null, null, null, null, null);
		List<PointInteret> res = cursorToPIList(c);
		closeDb();
		return res;
	}

	/** Récupération de l'ensemble des id des points d'intérêts d'un type passé en argument */
	public List<String> getAllPiIdsByType(String type) {
		List<String> ids = new ArrayList<>();

		String selectQuery = "SELECT "+COL_ID+" FROM " + TABLE_PI + " WHERE "+COL_TYPE+"='"+ escapeAllApostrophe(type.toUpperCase()) + '\'';

		openDb();
		try(Cursor cursor = mybase.rawQuery(selectQuery, null)){
			if (cursor.moveToFirst()) {
				do {
					ids.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
		}
		closeDb();
		return ids;
	}

	private static List<PointInteret> cursorToPIList(Cursor c) {
		c.moveToFirst();
		List<PointInteret> liste = new ArrayList<>();
		int nb = 0;
		int size = c.getCount(); // Nombre de rang
		PointInteret pi;
		while (nb < size) {
			pi = new PointInteret();
			pi.setId(c.getString(NUM_COL_ID));
			pi.setType(c.getString(NUM_COL_TYPE));
			pi.setName(c.getString(NUM_COL_NOM));
			pi.setDesc(c.getString(NUM_COL_DESC));
			pi.setLat(c.getFloat(NUM_COL_LAT));
			pi.setLon(c.getFloat(NUM_COL_LON));
			pi.setCoul(c.getString(NUM_COL_COUL));
			pi.setVisible((c.getInt(NUM_COL_VIS) == 1));
			pi.setDate(c.getString(NUM_COL_DATE));

			liste.add(pi);
			c.moveToNext();
			nb++;
		}

		c.close();
		return liste;
	}

	/** Cette methode permet de convertir un cursor en un PI
	 * @param c Cursor
	 * @return un PtsInteret
	 */
	private static PointInteret cursorToPI(Cursor c) {
		//si aucun element n'a ete retourne dans la requete, on renvoie null
		if (c.getCount() == 0)
			return null;

		//Sinon on se place sur le premier element
		c.moveToFirst();
		//On cree un PI
		PointInteret pi = new PointInteret();
		//on lui affecte toutes les infos grace aux infos contenues dans le Cursor
		pi.setId(c.getString(NUM_COL_ID));
		pi.setType( removeAllBackslashWithQuote(c.getString(NUM_COL_TYPE))  );
		pi.setName(removeAllBackslashWithQuote(c.getString(NUM_COL_NOM)));
		pi.setDesc(removeAllBackslashWithQuote(c.getString(NUM_COL_DESC)));
		pi.setLon(c.getFloat(NUM_COL_LON));
		pi.setLat(c.getFloat(NUM_COL_LAT));
		pi.setCoul(removeAllBackslashWithQuote(c.getString(NUM_COL_COUL))) ;
		pi.setVisible(c.getInt(NUM_COL_VIS) == 1);
		pi.setDate(c.getString(NUM_COL_DATE));

		//On ferme le cursor
		c.close();

		//On retourne le PI
		return pi;
	}

	/** Mise à jour de toutes les couleurs des PI pour un type de PI donné */
	public synchronized void updateAllColorPILikeType(String color, String type) {
		ContentValues values = new ContentValues();
		color = escapeAllApostrophe(color);
		type = escapeAllApostrophe(type.toUpperCase());

		values.put(COL_COUL, color);
		openDb();
		mybase.update(TABLE_PI, values, COL_TYPE + "=\'" + type + '\'', null);
		closeDb();
	}

	/** Mise à jour de toutes les visibilité des PI pour un type de PI donné */
	public synchronized void updateAllVisibilityPILikeType(String type, boolean visibility) {
		ContentValues values = new ContentValues();
		type = escapeAllApostrophe(type.toUpperCase());

		values.put(COL_VIS, visibility);
		openDb();
		mybase.update(TABLE_PI, values, COL_TYPE + "=\'" + type + '\'', null);
		closeDb();
	}

	/** Mise à jour de la visibilité d'un PI à partir de son id */
	public synchronized void updateVisibilityById(String id, boolean visibility) {
		ContentValues values = new ContentValues();

		values.put(COL_VIS, visibility);
		openDb();
		mybase.update(TABLE_PI, values, COL_ID + "='" + id + '\'', null);
		closeDb();
	}

	private static String escapeAllApostrophe(String str) {
		if(str!=null) return str.replaceAll("'", "\'");
		return "";
	}

	private static String removeAllBackslashWithQuote(String str) {
		if(str!=null) return str.replaceAll("\'", "'");
		return "";
	}

	/** Suppression des point d'intérêt dont la date est expiré */
	public synchronized void deletePIExpirate(){
		String currentDateandTime = CheckMyFacConstants.sdf.format(new Date());
		openDb();
		mybase.delete(TABLE_PI,COL_TYPE + "='" + CheckMyFacConstants.TYPE_USER + "' AND "
				+ COL_DATE + " < '"+currentDateandTime+"' AND "+ COL_DATE + " != ''" , null);
		closeDb();

	}

}