package com.checkmyfac.dao.transport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.checkmyfac.dao.DAO;
import com.checkmyfac.objet.Transport;
import com.checkmyfac.utils.CheckMyFacConstants;
import com.checkmyfac.utils.CheckMyFacUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransportDAO extends DAO {

	private static final String TAG = "TransportDAO";

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "transports.db";
	private static final String TABLE_TRANSPORTS = "table_transports";
	private static final String COL_ID = "id";
	private static final int NUM_COL_ID = 0;
	private static final String COL_TYPE = "Type";
	private static final int NUM_COL_TYPE = 1;
	private static final String COL_DESCRIPTION = "Description";
	private static final int NUM_COL_DESCRIPTION = 2;
	private static final String COL_NUM = "Numeros";
	private static final int NUM_COL_NUM = 3;
	private static final String COL_ARRET = "Arret";
	private static final int NUM_COL_ARRET = 4;
	private static final String COL_LAT = "Lat";
	private static final int NUM_COL_LAT = 5;
	private static final String COL_LON = "Lon";
	private static final int NUM_COL_LON = 6;
	private static final String COL_COUL = "Couleur";
	private static final int NUM_COL_COUL = 7;

	private static final String CREATE_BDD = "CREATE TABLE " + TABLE_TRANSPORTS + " ("
			+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_TYPE +" TEXT NOT NULL, "
			+ COL_DESCRIPTION + " TEXT NOT NULL, "
			+ COL_NUM +" TEXT NOT NULL, "
			+ COL_ARRET +" TEXT NOT NULL, "
			+ COL_LAT + " REAL NOT NULL, "
			+ COL_LON + " REAL NOT NULL, "
			+ COL_COUL + " TEXT NOT NULL);";

	public TransportDAO(Context context) {
		super(context, NOM_BDD, null, VERSION_BDD);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + TABLE_TRANSPORTS + ';');
		Log.d(TAG, "onUpgrade bdd");
		onCreate(db);
		db.setVersion(TransportDAO.VERSION_BDD);
	}

	public synchronized void clearAll() {
		openDb();
		mybase.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSPORTS);
		onCreate(mybase);
		closeDb();
	}

	public synchronized boolean isEmpty() {
		openDb();
		try(Cursor cur = mybase.rawQuery("SELECT COUNT(*) FROM TABLE_TRANSPORTS", null)) {
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

	public synchronized List<Transport> getAll() {
		openDb();
		Cursor c = mybase.rawQuery("SELECT * FROM "+TABLE_TRANSPORTS, null);
		List<Transport> res = cursorToTransportList(c);
		closeDb();
		return res;
	}

	private synchronized void insertTransport(Transport transport) {

		ContentValues values = new ContentValues();

		if (transport.getType() == null) values.put(COL_TYPE, "");
		else values.put(COL_TYPE, transport.getType() );

		if (transport.getDescription() == null) values.put(COL_DESCRIPTION, "");
		else values.put(COL_DESCRIPTION, escapeAllApostrophe(transport.getDescription()) );

		if (transport.getNumero() == null) values.put(COL_NUM, "");
		else values.put(COL_NUM, CheckMyFacUtils.joinInteger(transport.getNumero()));

		if (transport.getArret() == null) values.put(COL_ARRET, "");
		else values.put(COL_ARRET, escapeAllApostrophe(transport.getArret()) );

		if (transport.getLat() == null) values.put(COL_LAT, "");
		else values.put(COL_LAT, transport.getLat());

		if (transport.getLon() == null) values.put(COL_LON, "");
		else values.put(COL_LON, transport.getLon());

		if (transport.getCouleur() == null) values.put(COL_COUL, "");
		else values.put(COL_COUL, transport.getCouleur());

		openDb();

		mybase.insert(TABLE_TRANSPORTS, null, values);
		closeDb();
	}

	public synchronized void insertListTransport(List<Transport> list){
		for(Transport transport : list) insertTransport(transport);
	}

	public synchronized void updateAllColorLikeType(String color, String type) {
		ContentValues values = new ContentValues();
		color = escapeAllApostrophe(color);
		type = escapeAllApostrophe(type.toUpperCase());

		values.put(COL_COUL, color);
		openDb();
		mybase.update(TABLE_TRANSPORTS, values, COL_TYPE + "='" + type + '\'', null);
		closeDb();
	}

	public Integer[] getNumerosTransportByDesc(String description) {

		String selectQuery = "SELECT "+ COL_NUM + " FROM " + TABLE_TRANSPORTS
				+ " WHERE "+COL_DESCRIPTION+"='"+ escapeAllApostrophe(description) +'\'';

		openDb();
		Integer[] res = null;
		try(Cursor cursor = mybase.rawQuery(selectQuery, null)) {
			if (cursor.moveToFirst()) {
				res = CheckMyFacUtils.splitInteger(cursor.getString(0));
			}
			if (res != null) Arrays.sort(res);
		}
		closeDb();

		return res;
	}

	public String getArretTransportByDesc(String description) {

		String selectQuery = "SELECT "+ COL_ARRET + " FROM " + TABLE_TRANSPORTS
				+ " WHERE "+COL_DESCRIPTION+"='"+ escapeAllApostrophe(description) +'\'';

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

	public String getArret(String id) {
		if(id==null){
			Log.w(TAG, "getArret: null id");
			return null;
		}
		String selectQuery = "SELECT "+ COL_ARRET + " FROM " + TABLE_TRANSPORTS
				+ " WHERE "+COL_ID+'='+id.replace(CheckMyFacConstants.DISCRIMINANT_TRANSPORT,"");

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

	private static List<Transport> cursorToTransportList(Cursor c) {
		c.moveToFirst();
		ArrayList<Transport> liste = new ArrayList<>();
		int nb = 0;
		int size = c.getCount(); // Nombre de rang
		while (nb < size) {
			Transport transport = new Transport();
			transport.setId(CheckMyFacConstants.DISCRIMINANT_TRANSPORT+c.getString(NUM_COL_ID));
			transport.setType(c.getString(NUM_COL_TYPE));
			transport.setDescription(c.getString(NUM_COL_DESCRIPTION));
			transport.setNumero(CheckMyFacUtils.splitInteger(c.getString(NUM_COL_NUM)));
			transport.setArret(c.getString(NUM_COL_ARRET));
			transport.setLat(c.getFloat(NUM_COL_LAT));
			transport.setLon(c.getFloat(NUM_COL_LON));
			transport.setCouleur(c.getString(NUM_COL_COUL));
			liste.add(transport);
			c.moveToNext();
			nb++;
		}
		c.close();
		return liste;
	}

	/** Cette methode permet de convertir un cursor en un Transport
	 * @param c Cursor
	 * @return un Transport
	 */
	private Transport cursorToTransport(Cursor c) {
		//si aucun element n'a ete retourne dans la requete, on renvoie null
		if (c.getCount() == 0)
			return null;

		//Sinon on se place sur le premier element
		c.moveToFirst();
		//On cree un Transport
		Transport transport = new Transport();
		//on lui affecte toutes les infos grace aux infos contenues dans le Cursor
		transport.setId(CheckMyFacConstants.DISCRIMINANT_TRANSPORT+c.getString(NUM_COL_ID));
		transport.setType(c.getString(NUM_COL_TYPE));
		transport.setDescription(removeAllBackslashWithQuote(c.getString(NUM_COL_DESCRIPTION)));
		transport.setNumero(CheckMyFacUtils.splitInteger(c.getString(NUM_COL_NUM)));
		transport.setArret(removeAllBackslashWithQuote(c.getString(NUM_COL_ARRET)));
		transport.setLat(c.getFloat(NUM_COL_LAT));
		transport.setLon(c.getFloat(NUM_COL_LON));
		transport.setCouleur(c.getString(NUM_COL_COUL));

		//On ferme le cursor
		c.close();

		//On retourne le Transport
		return transport;
	}

	private static String escapeAllApostrophe(String str) {
		return str.replaceAll("'", "\'");
	}

	private static String removeAllBackslashWithQuote(String str) {
		return str.replaceAll("\'", "'");
	}

}