package com.evifin.kerailymestari;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "results" aktiviteetti. Näyttää kaikki keräilyt muokattavissa olevalla listalla.
public class results extends AppCompatActivity {

    // Aktiviteetin muuttujat.
    public int kid;
    public String asiakasn;
    public String kerailyNimi;
    public String tuoteTyyppi;
    private ListView ListView;
    private DatabaseHelper databaseHelper;
    private CustomAdapter customAdapter;
    private ArrayList<ViivakoodiModel> viivakoodiModelArrayList;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Aktiviteeti luontimetodi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hakee aktiviteetin layoutin.
        setContentView(R.layout.activity_results);

        // Hakee listanäkymän laytoutin.
        ListView = findViewById(R.id.lv);

        // Luodaan yhteys "DatabaseHelper" classiin
        databaseHelper = new DatabaseHelper(this);

        // Haetaan metodi jolla muodostetaan lista kaikista keräilyistä.
        viivakoodiModelArrayList = databaseHelper.getAllUsers();

        // Luodaan yhteys "customAdapter" classiin käyttäen muodostettua keräilylistaa.
        customAdapter = new CustomAdapter(this,viivakoodiModelArrayList);

        // Määrittä listanäkymän käyttäen adapteria
        ListView.setAdapter(customAdapter);

        // Kuuntelee jos keräilyä on painettu listanäkymässä ja hakee sille keräilynimen, keräily ID:n, Tuotetyypin sekä asiakasnumeron.
        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                kerailyNimi =((ViivakoodiModel) ListView.getItemAtPosition(position)).getKerailyNimi();
                kid = ((ViivakoodiModel) ListView.getItemAtPosition(position)).getKerailyId();
                tuoteTyyppi = databaseHelper.getTuoteTyyppi(kid);
                Log.e("tuoteTyyppi: ", tuoteTyyppi);
                asiakasn = databaseHelper.getAsiakasn(kid);
                Log.e("asiakasn: ", asiakasn);

                // Aloittaa
                showAlertDialogButtonClicked();
            }
        });
        
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Keräilylistan keräilyn valintametodi.
    public void showAlertDialogButtonClicked() {

        // Määrittää dialogin
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Valitse toiminto");

        // Lisää "simple" lista painikkeita dialogiin.
        String[] vaihtoehdot = {"Editoi", "Lähetä", "Poista", "Peruuta"};
        builder.setItems(vaihtoehdot, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        //Editoi: Tallentaa keräilyn keräilyID:n, tuotetyypin sekä asiakasnumeron. Starttaa "camera" aktiviteetin ja lähettää tallennetut parametrit.
                        Intent intent = new Intent(results.this, camera.class);
                        intent.putExtra("KID", kid);
                        intent.putExtra("TYYPPI", tuoteTyyppi);
                        intent.putExtra("ASIAKASN", asiakasn);
                        startActivity(intent);
                        break;
                    }
                    case 1: {
                        // Lähetä: Suorittaa exportData metodin.
                        exportData();
                        break;
                    }
                    case 2: {
                        // Poista: Surittaa metodin
                        openDialogPoista();
                        break;
                    }
                    case 3: {
                        // Peruuta: sulkee dialogin
                        dialog.dismiss();
                        break;
                    }
                }
            }
        });

        // Luo määritetyn dialogin
        AlertDialog dialog = builder.create();

        // Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Työkalupalkin ("ActionBar") layoutin määritys metodi.
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Hakee työkalupalkkiin "menu_results" layoutin.
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Työkalupalkin painikkeiden toiminnot.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()){
            // "Takaisin" painike palauttaa edelliseen aktiviteettiin.
            case android.R.id.home:
                int id = item.getItemId();
                if (id == android.R.id.home) {
                    this.finish();
                }
                break;

            // Hakee layoutista ikonin "poista kaikki" painikkeelle, suorittaa metodin.
            case R.id.action_delete:
                openDialogPoistaKaikki();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Kaikkien keräilyjen poistometodi
    public void openDialogPoistaKaikki() {

        // Määritellään dialogi
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Poistetaanko kaikki keräilyt?");

        // Lisää dialogiin "POISTA" painikkeen ja sen kuuntelijan.
        builder.setPositiveButton("POISTA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Etsii muuttujalle polun josta keräilyn mahdolliset tiedostot poistetaan.
                File deleteFilesPath = new File(Environment.getExternalStorageDirectory(), "kerailymestari");

                // Luo loopilla listan poistettavista tiedostoista ja poistaa listan tiedostot.
                String[] allFiles;
                allFiles = deleteFilesPath.list();
                for (int i=0; i<allFiles.length; i++) {
                    File mFile = new File(deleteFilesPath, allFiles[i]);
                    mFile.delete();
                }

                // Hakee ja suorittaa metodin jolla poistetaan "kerailyt" taulukosta kaikki keräilyt.
                databaseHelper.deleteAllKerailyt();
                updateListview();

            }
        });

        // Luo "peruuta" painikkeen joka sulkee dialogin
        builder.setNegativeButton("Peruuta", null);

        // Luo näytä määritetty dialogi, Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle.
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Yhden eräilyn poistometodi
    public void openDialogPoista() {

        // Määritellään dialogi
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Poistetaanko keräily?");

        // lisää "POISTA" painikkeen ja sen kuuntelijan.
        builder.setPositiveButton("POISTA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Etsii muuttujalle polun josta keräilyn mahdollinen tiedosto poistetaan.
                File folder = Environment.getExternalStorageDirectory();

                // Hakee poistetavalle tiedostolle polun sekä tiedoston nimen.
                String fileName = folder.getPath() + "/kerailymestari/" +kerailyNimi +".csv";
                Log.e("poistopolku: ", fileName);

                // Määrittää polun tiedostoksi, JOS sellainen on olemmassa, se poistetaan..
                File mFile = new File(fileName);
                if(mFile.exists())
                    mFile.delete();

                // Poistaa "kerailyt" taulukosta valitun keräilyn käyttäen "kid"(keräilyn ID:tä)
                databaseHelper.deleteKeraily(kid);
                updateListview();
            }
        });

        // Luo "Peruuta" painikkeen. Painettaessa sulkee dialogin.
        builder.setNegativeButton("Peruuta", null);

        // Luo ja näyttää määritellyn dialogin. Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle.
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Listan päivitymetodi
    public void updateListview() {

        // Hakee metodin jolla kutsutaan listaan kaikki keräilyt "kerailyt" taulukosta.
        viivakoodiModelArrayList = databaseHelper.getAllUsers();

        // Muodostettu lista käytetään adapterilla
        customAdapter = new CustomAdapter(this, viivakoodiModelArrayList);

        // Listanäkymä luodaan adapterin muokkaamalla listalla.
        ListView.setAdapter(customAdapter);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tiedoston luontimetodi
    private void exportData() {

        // Luo muuttujan joka palauttaa "DatabaseHelper" kontekstin.
        DatabaseHelper dbhelper = new DatabaseHelper(getApplicationContext());

        // Luo tiedostolle kirjoituspolun sekä tarkistaa onko sellainen jo olemassa.
        File exportDir = new File(Environment.getExternalStorageDirectory(), "kerailymestari");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        // Luo muuttujan jossa on kirjoitettavan tiedoston polku sekä tiedostonimi.
        File file = new File(exportDir, kerailyNimi+".csv");
        try
        {
            // Luo tiedoston
            file.createNewFile();

            // Määritetään CSVWriterille kirjoitus asetukset. ( separaattori = ",", Ei lainausmerkkejä, Ei "escape_characteria", Vakio rivinvaihto rivin lopussa.)
            // HUOM: Ei näy katsoessa tiedostoa esim. notepadilla.
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            // Mahdollistaa tietokannan lukemisen
            SQLiteDatabase db = dbhelper.getReadableDatabase();

            // Luo muuttujan jolla haetaan kaikki tuotteet joilla on kirjoitettavan keräilyn ID(kid).
            Cursor curCSV = db.rawQuery("SELECT * FROM tuotteet WHERE KID = " + kid,null);

            // Looppi joka kirjoittaa tiedoston tuoterivi kerrallaan. Kirjoittaa valitut kolumnitietueet per tuoterivi. 1) Keräilytyyppi. 2) Asiakasnumero. 3) Viivakoodi. 4) tuotteen määrä.
            // HUOM: kolumni nro 0, olisi tuotteen ID ja kolumni nro 5, olisi (foreign key) keräilyn id.
            while(curCSV.moveToNext())
            {
                String arrStr[] ={curCSV.getString(1) + curCSV.getString(2) + curCSV.getString(3) +  curCSV.getString(4)};
                csvWrite.writeNext(arrStr);
            }
            // Lopettaa kirjoittajan loopin valmistuttua
            csvWrite.close();
            curCSV.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("Results", sqlEx.getMessage(), sqlEx);
        }

        Log.e("WritingPath", String.valueOf(file));

        // Suorittaa Tiedoston lähetysmetodin
        sendFile();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tiedoston lähetyssmetodi
    private void sendFile() {

        // Määrittää muuttujalle tiedoston nimi.
        // HUOM: Polut eivät saa sisältää "/" string merkkiä. Siksi käytössä "File.separator"
        String fileName = File.separator + kerailyNimi+".csv";

        // Määrittää muuttujalle tiedoston polun.
        File filePath = new File(Environment.getExternalStorageDirectory(), "kerailymestari" + fileName);

        // API 24 lähtien käytetään "FileProvideria", jolla voidaan antaa lähettävälle ohjelmalle erillisiä lupia tiedoston käsittelemiseen.
        Uri uriPath = FileProvider.getUriForFile(this, "com.example.kerailymestari.fileprovider", filePath);
        Log.e("LoadingPath: ", String.valueOf(filePath));

        // Määritellään muuttuja joka suorittaa androidin vakio "share" toiminnon.
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        // Annetaan tiedoston tyyppi. Kertoo androidille mitä ohjelmia voidaan käyttää tiedoston jakamiseen.
        sharingIntent.setType("text/plain");

        // Antaa luvan lukea tiedostoa.
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Antaa fileproviderin uri polun.
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uriPath);

        // Starttaa määritetyn "share" aktiviteetin annetulla otsikolla.
        this.startActivity(Intent.createChooser(sharingIntent, "Jaa keräily käyttäen..."));

        // Hakee ja suorittaa metodin jolla päivitetään keräilyn "lähetys status"
        databaseHelper.updateStatus(kid, 1);

        // Päivittää listanäkymän
        updateListview();
        Log.e("STATUS: ", String.valueOf(databaseHelper.getStatus(kid)));
    }

}
