package com.evifin.kerailymestari;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "DatabaseHelper" Luo applikaation sisäisem tietokannan sekä sen taulukot.
// Sisältää tietokanta metodeita joita kutsutaan muista classeista.

public class DatabaseHelper extends SQLiteOpenHelper {

    // Tietokannan nimi ja versio
    public static String DATABASE_NAME = "viivakoodit_database";
    private static final int DATABASE_VERSION = 1;

    // "kerailyt" taulukon muuttujat
    private static final String TABLE_KERAILYT = "kerailyt";
    private static final String KEY_KERAILY_ID = "kerailyID";
    private static final String KEY_KERAILY_NIMI = "nimi";
    private static final String KEY_KERAILY_STATUS = "status";

    // "tuotteet" taulukon muuttujat
    private static final String TABLE_TUOTTEET = "tuotteet";
    private static final String KEY_TUOTTEET_ID = "tuoteID";
    private static final String KEY_TUOTTEET_TYYPPI = "tyyppi";
    private static final String KEY_TUOTTEET_ASIAKASN = "asiakasn";
    private static final String KEY_TUOTTEET_VIIVAKOODI = "viivakoodi";
    private static final String KEY_TUOTTEET_MAARA = "maara";
    private static final String KEY_TUOTTEET_KID = "kid";


    // Luo "kerailyt" sekä "tuotteet" taulukot.
    private static final String CREATE_TABLE_KERAILYT = "CREATE TABLE " + TABLE_KERAILYT +
            "(" + KEY_KERAILY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_KERAILY_NIMI + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + KEY_KERAILY_STATUS + " INTEGER);";

    private static final String CREATE_TABLE_TUOTTEET = "CREATE TABLE " + TABLE_TUOTTEET +
            "(" + KEY_TUOTTEET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ KEY_TUOTTEET_TYYPPI + " TEXT, "+ KEY_TUOTTEET_ASIAKASN + " TEXT, "+ KEY_TUOTTEET_VIIVAKOODI + " TEXT, " + KEY_TUOTTEET_MAARA + " INTEGER, " + KEY_TUOTTEET_KID + " INTEGER, " +
            "FOREIGN KEY(kid) REFERENCES kerailyt(kerailyID));";


    // Julkistaa tietokannan kontekstin (tietokannan nimen sekä version)
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Applikaation käynnistyessä luo taulukot.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_KERAILYT);
        db.execSQL(CREATE_TABLE_TUOTTEET);
    }

    // Poistaa taulukot jos tietokannalla on uusi versionumero.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_KERAILYT + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_TUOTTEET + "'");
        onCreate(db);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Keräilyn lisäysmetodi
    public void addKeraily(String nimi, int status) {

        // Luo muuttujan joka mahdollistaa tietokantaan kirjoittamisen
        SQLiteDatabase db = this.getWritableDatabase();

        // Luo muuttujan johon varastoidaan keräilyn nimi sekä lähetysstatus.
        ContentValues valuesKeraily = new ContentValues();
        valuesKeraily.put(KEY_KERAILY_NIMI, nimi);
        valuesKeraily.put(KEY_KERAILY_STATUS, status);

        // Lisää määritetellyn keräilyn
        db.insertWithOnConflict(TABLE_KERAILYT, null, valuesKeraily, SQLiteDatabase.CONFLICT_IGNORE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotteen lisäysmetodi
    public int addTuotteet(String tyyppi, String asiakasn, String viivakoodi, int maara, int kid) {

        // Luo muuttujan joka mahdollistaa tietokantaan kirjoittamisen
        SQLiteDatabase db = this.getWritableDatabase();

        // Luo muuttujan johon varastoidaan tuotteen tyyppi, asiakasnumero, viivakoodi, määrä sekä keräilyID.
        ContentValues valuesTuote = new ContentValues();
        valuesTuote.put(KEY_TUOTTEET_TYYPPI, tyyppi);
        valuesTuote.put(KEY_TUOTTEET_ASIAKASN, asiakasn);
        valuesTuote.put(KEY_TUOTTEET_VIIVAKOODI, viivakoodi);
        valuesTuote.put(KEY_TUOTTEET_MAARA, maara);
        valuesTuote.put(KEY_TUOTTEET_KID, kid);

        // Lisää määritellyn tuotteen.
        long newRowId = db.insert(TABLE_TUOTTEET, null, valuesTuote);
        return (int) Integer.parseInt(""+newRowId);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // KeräilyID:n hakumetodi
    public int getKID () {

        // Luodaan muuttuja
        int kid = -1;

        // Muuttuja jolla haetaan "kerailyt" taulukosta kerailyn ID.
        String selectQuery = "SELECT " + KEY_KERAILY_ID + " FROM " + TABLE_KERAILYT + " ORDER BY " + KEY_KERAILY_ID + " DESC LIMIT 1 ";

        // Muuttuja jolla mahdollistetaan tietokannan lukeminen
        SQLiteDatabase db = this.getReadableDatabase();

        // haetaan rivi määritetyillä muuttujilla. ja palautetaan kid(keräilyn ID)
        Cursor cTuote = db.rawQuery(selectQuery, null);
        if (cTuote.moveToFirst()) {
                kid = Integer.parseInt(cTuote.getString(cTuote.getColumnIndex(KEY_KERAILY_ID)));
        }
        return kid;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lähetysstatuksen hakumetodi
    public int getStatus (int kid) {

        // Luodaan muuttuja
        int status = -1;

        // Muuttuja jolla haetaan "kerailyt" taulukosta kerailyn status.
        String selectQuery = "SELECT " + KEY_KERAILY_STATUS + " FROM " + TABLE_KERAILYT + " WHERE " + KEY_KERAILY_ID + " = " + kid;

        // Muuttuja jolla mahdollistetaan tietokannan lukeminen
        SQLiteDatabase db = this.getReadableDatabase();

        // haetaan rivi määritetyillä muuttujilla. ja palautetaan status (0 tai 1)
        Cursor cTuote = db.rawQuery(selectQuery, null);
        if (cTuote.moveToFirst()) {
            status = Integer.parseInt(cTuote.getString(cTuote.getColumnIndex(KEY_KERAILY_STATUS)));
        }
        return status;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotetyypin hakumetodi
    public String getTuoteTyyppi (int kid) {

        // Luodaan muuttuja
        String tuoteTyyppi = "";

        // Muuttuja jolla haetaan "tuotteet" taulukosta tuotteen tyyppi.
        String selectQuery = "SELECT " + KEY_TUOTTEET_TYYPPI + " FROM " + TABLE_TUOTTEET + " WHERE KID = " + kid;

        // Muuttuja jolla mahdollistetaan tietokannan lukeminen
        SQLiteDatabase db = this.getReadableDatabase();

        // haetaan rivi määritetyillä muuttujilla. ja palautetaan tuoteTyyppi.
        Cursor cTuote = db.rawQuery(selectQuery, null);
        if (cTuote.moveToFirst()) {
            tuoteTyyppi = cTuote.getString(cTuote.getColumnIndex(KEY_TUOTTEET_TYYPPI));
        }
        return tuoteTyyppi;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Asiakasnumeron hakumetodi
    public String getAsiakasn (int kid) {

        // Luodaan muuttuja
        String asiakasn = "";

        // Muuttuja jolla haetaan "tuotteet" taulukosta tuotteen asiakasnumero.
        String selectQuery = "SELECT " + KEY_TUOTTEET_ASIAKASN + " FROM " + TABLE_TUOTTEET + " WHERE KID = " + kid;

        // Muuttuja jolla mahdollistetaan tietokannan lukeminen
        SQLiteDatabase db = this.getReadableDatabase();

        // haetaan rivi määritetyillä muuttujilla. ja palautetaan asiakasnumero.
        Cursor cTuote = db.rawQuery(selectQuery, null);
        if (cTuote.moveToFirst()) {
            asiakasn = cTuote.getString(cTuote.getColumnIndex(KEY_TUOTTEET_ASIAKASN));
        }
        return asiakasn;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Kaikkien keräilyjen hakumetodi
    public ArrayList<ViivakoodiModel> getAllUsers() {

        // Määrittelee muuttujan johon luodaan lista keräilyistä
        ArrayList<ViivakoodiModel> viivakoodiModelArrayList = new ArrayList<ViivakoodiModel>();

        // Määrittää muuttuja jolla haetaan kaikki keräilyt "kerailyt" taulukosta.
        String selectQuery = "SELECT  * FROM " + TABLE_KERAILYT;

        // Muuttuja jolla mahdollistetaan tietokannan lukeminen
        SQLiteDatabase db = this.getReadableDatabase();

        // kursorimuuttuja joka suorittaa määritetyt muuttujat.
        Cursor c = db.rawQuery(selectQuery, null);

        // Looppi joka hakee rivi kerrallaan keräilyt ja lisää ne listaan.
        if (c.moveToFirst()) {
            do {
                ViivakoodiModel viivakoodiModel = new ViivakoodiModel();
                viivakoodiModel.setKerailyId(c.getInt(c.getColumnIndex(KEY_KERAILY_ID)));
                viivakoodiModel.setKerailyNimi(c.getString(c.getColumnIndex(KEY_KERAILY_NIMI)));
                viivakoodiModel.setKerailyStatus(c.getInt(c.getColumnIndex(KEY_KERAILY_STATUS)));

                String selectTuoteQuery = "SELECT  * FROM " + TABLE_TUOTTEET +" WHERE "+KEY_TUOTTEET_KID+" = "+ viivakoodiModel.getKerailyId();

                Cursor cTuote = db.rawQuery(selectTuoteQuery, null);
                if (cTuote.moveToFirst()) {
                    do {
                        viivakoodiModel.setTuoteViivakoodi(cTuote.getString(cTuote.getColumnIndex(KEY_TUOTTEET_VIIVAKOODI)));
                    } while (cTuote.moveToNext());
                }
                viivakoodiModelArrayList.add(viivakoodiModel);
            } while (c.moveToNext());
        }
        return viivakoodiModelArrayList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Kaikkien tuotteiden hakumetodi
    public ArrayList<TuoteModel> getAllTuotteet(int kid) {

        // Määrittelee muuttujan johon luodaan lista tuotteista
        ArrayList<TuoteModel> tuoteModelArrayList = new ArrayList<TuoteModel>();

        // Määrittää muuttuja jolla haetaan kaikki keräilyt "tuotteet" taulukosta.
        String selectQuery = "SELECT  * FROM " + TABLE_TUOTTEET + " WHERE " + KEY_TUOTTEET_KID + " = " + kid;

        // Muuttuja jolla mahdollistetaan tietokannan lukeminen
        SQLiteDatabase db = this.getReadableDatabase();

        // kursorimuuttuja joka suorittaa määritetyt muuttujat.
        Cursor c = db.rawQuery(selectQuery, null);

        // Looppi joka hakee rivi kerrallaan tuotteet ja lisää ne listaan.
        if (c.moveToFirst()) {
            do {
                TuoteModel tuoteModel = new TuoteModel();
                tuoteModel.setId(Integer.parseInt(c.getString(c.getColumnIndex(KEY_TUOTTEET_ID))));
                tuoteModel.setTyyppi(c.getString(c.getColumnIndex(KEY_TUOTTEET_TYYPPI)));
                tuoteModel.setAsiakasn(c.getString(c.getColumnIndex(KEY_TUOTTEET_ASIAKASN)));
                tuoteModel.setViivakoodi(c.getString(c.getColumnIndex(KEY_TUOTTEET_VIIVAKOODI)));
                tuoteModel.setMaara(c.getString(c.getColumnIndex(KEY_TUOTTEET_MAARA)));
                tuoteModel.setKid(kid);

                tuoteModelArrayList.add(tuoteModel);
            } while (c.moveToNext());
        }
        return tuoteModelArrayList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Määrän päivitysmetodi
    public void updateMaara(int id, int maara) {

        // Muuttuja jolla mahdollistetaan tietokantaan kirjoittaminen
        SQLiteDatabase db = this.getWritableDatabase();

        // Päivittää tuotteen määrän "tuotteet" taulukkoon
        ContentValues valuesMaara = new ContentValues();
        valuesMaara.put(KEY_TUOTTEET_MAARA, maara);
        db.update(TABLE_TUOTTEET, valuesMaara, KEY_TUOTTEET_ID + " = ?", new String[]{String.valueOf(id)});

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Statuksen päivitysmetodi
    public void updateStatus(int kid, int status) {

        // Muuttuja jolla mahdollistetaan tietokantaan kirjoittaminen
        SQLiteDatabase db = this.getWritableDatabase();

        // Päivittää keräilyn statuksen "kerrailyt" taulukkoon
        ContentValues valuesStatus = new ContentValues();
        valuesStatus.put(KEY_KERAILY_STATUS, status);
        db.update(TABLE_KERAILYT, valuesStatus, KEY_KERAILY_ID + " = ?", new String[]{String.valueOf(kid)});

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Asiakasnumeron päivitysmetodi
    public void updateAsiakasn(int id, String asiakasnUpdate) {

        // Muuttuja jolla mahdollistetaan tietokantaan kirjoittaminen
        SQLiteDatabase db = this.getWritableDatabase();

        // Päivittää tuotteen asiakasnumeron "tuotteet" taulukkoon
        ContentValues valuesAsiakasnUpdate = new ContentValues();
        valuesAsiakasnUpdate.put(KEY_TUOTTEET_ASIAKASN, asiakasnUpdate);
        db.update(TABLE_TUOTTEET, valuesAsiakasnUpdate, KEY_TUOTTEET_ID + " = ?", new String[]{String.valueOf(id)});

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Keräilyn poistometodi
    public void deleteKeraily(int id) {

        // Muuttuja jolla mahdollistetaan tietokantaan kirjoittaminen
        SQLiteDatabase db = this.getWritableDatabase();

        // Poistaa keräilyn "kerailyt" taulukosta.
        db.delete(TABLE_KERAILYT, KEY_KERAILY_ID+ " = ?", new String[]{String.valueOf(id)});

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotteen poistometodi
    public void deleteTuote(int id) {

        // Muuttuja jolla mahdollistetaan tietokantaan kirjoittaminen
        SQLiteDatabase db = this.getWritableDatabase();

        // Poistaa tuotteen "tuotteet" taulukosta
        db.delete(TABLE_TUOTTEET, KEY_TUOTTEET_ID + " = ?", new String[]{String.valueOf(id)});

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Kaikkien keräilyjen poistometodi
    public void deleteAllKerailyt() {

        // Muuttuja jolla mahdollistetaan tietokantaan kirjoittaminen
        SQLiteDatabase db = this.getWritableDatabase();

        // Poistaa kaikki keräilyt "kerailyt" taulukosta
        // HUOM: Käyttää "vacuum" toimintoa joka "siivoaa turhat rivit" ja "palauttaa" ne uudelleen käyttöön. Pitää tietokannan pienenä ja pienentää tietokannan pirstaloitumisen riskiä
        // (Rivien poisto ei normaalisti poista dataa vaan merkkaa sen käyttökelvottomaksi).
        db.delete(TABLE_KERAILYT, null, null);
        db.execSQL("vacuum");
    }


}