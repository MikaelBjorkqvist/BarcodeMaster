/**
 * "KERÄILYMESTARI" by Mikael Björkqvist
 * Viivakoodien keräily & lähetysapplikaatio.
 */

package com.evifin.kerailymestari;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "MainActivity" = aloitusaktiviteetti. Siirrytään "camera" sekä "results" aktiviteetteihin.
// Kysyy tarvittavat luvat käyttäjältä applikaation käyttöön
public class MainActivity extends AppCompatActivity {

    //aktiviteetin muuttujat.
    private DatabaseHelper databaseHelper;
    private String kerailyNimi;
    private String kerailyTyyppi;
    private String asiakasn;
    private int kerailyID;
    private int status;

    //"padAsiakasn" lisää välilyöntejä asiakasnumeroon riippuen asiasnumeron pituudesta.
    public static String padAsiakasn(String s, int n) { return String.format("%-" + n + "s", s); }

    //applikaatiolupien tarkistusmuuttuja sekä permissions lista.
    int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

///////////////////////////////////////////////////////////////////////////////////////////////////
//Aktiviteetin luontimetodi.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hakee "activity_main.xml" layoutin sisällön
        setContentView(R.layout.activity_main);

        // Initialisoidaan tietokanta käyttäen "DatabaseHelper" classia.
        databaseHelper = new DatabaseHelper(this);

        // Hakee painikkeet "activity_main.xml" layoutista
        ImageButton btnKeraa = findViewById(R.id.keraa);
        ImageButton btnListat = findViewById(R.id.listat);

        // Kuuntelijat painikkeille.
        btnKeraa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog1();
            }
        });
        btnListat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, results.class));
            }
        });

        //Keräilytyypin sekä keräilyn lähetysstatuksen aloitusmuuttujat
        kerailyTyyppi = "";
        status = 0;

        //Tarkistaa applikaatioon tarvittavat luvat "hasPermissions" metodilla.
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Keräilytyypin valintadialogi
    public void openDialog1() {

        // Määrittelee dialogin.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Valitse tyyppi");

        // Lisää dialogiin "simple" listan.
        String[] tyyppi = {"Lähete", "Ostotilaus", "Lainaus", "Inventointi", "Tuloutus"};

        // Asettaa oletus valinnan listaan. -1 = ei mikään vaihtoehdoista.
        int checkedItem= -1;

        // onClicklistener kuuntelee mitä vaihtehtoa painetaan
        builder.setSingleChoiceItems(tyyppi, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Listan vaihtoehtojen string output "kerailyTyyppi" muuttujalle.
                if (which == 0) {
                        kerailyTyyppi = "L";
                    }
                    if (which == 1) {
                        kerailyTyyppi = "O";
                    }
                    if (which == 2) {
                        kerailyTyyppi = "V";
                    }
                    if (which == 3) {
                        kerailyTyyppi = "I";
                    }
                    if (which == 4) {
                        kerailyTyyppi = "S";
                }
            }
        });

        // Lisää dialogiin "OK" painikkeen ja sen kuuntelijan.
        // JOS "kerailyTyyppi" on "I" tai "S" ja painetaan "OK", lisää "KerailyNimi" sekä "kerailyTyyppi" "KERAILYT" tauluun ja aloittaa "camera" aktiviteetin.
        // Muussa tapauksessa avaa "openDialog2" dialogin.
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (kerailyTyyppi.equals("I") || kerailyTyyppi.equals("S")) {

                    // Asettaa ajan formaatin, hakee ajan. Muutetaan kerailyNimi ajaksi.
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    String aika = simpleDateFormat.format(new Date());
                    kerailyNimi = aika;

                    // Asettaa asiakasnumeron tyhjäksi.
                    asiakasn = padAsiakasn("", 20);

                    // Lisää uuden keräilyn "kerailyt" tauluun.
                    databaseHelper.addKeraily(kerailyNimi, status);

                    //Hakee lisätyn keräilyn ID:n.
                    kerailyID = databaseHelper.getKID();

                    // Tallentaa asiakasnumeron, KerailyID:n sekä kerailyTyypin ja lähettää ne "camera" aktiviteettiin. Käynnistää "camera" aktiviteetin.
                    Intent intent = new Intent(MainActivity.this, camera.class);
                    intent.putExtra("ASIAKASN", asiakasn);
                    intent.putExtra("KID", kerailyID);
                    intent.putExtra("TYYPPI", kerailyTyyppi);
                    startActivity(intent);

                } else {
                    openDialog2();
                }
            }
        });

        // Lisää dialogiin "Takaisin" painikkeen, painettaessa sulkee dialogin.
        builder.setNegativeButton("Takaisin", null);

        // Luo dialogin määritetyillä asetuksilla.
        AlertDialog dialog = builder.create();

        // Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin.
        dialog.show();

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // asiakasnumeron kyselydialogi
    public void openDialog2() {

        // Määrittelee dialogin
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Anna asiakasnumero");

        // Määrittää dialogille tekstinsyöttörivin.
        final EditText input = new EditText(this);

        // Rivien maksimi määrä.
        input.setMaxLines(1);

        //Muuttaa näppäimistön "return" näppäimen "Done" actioniksi
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        builder.setView(input);

        // Lisää dialogiin "Ok" painikkeen sekä kuuntelijan.
        // Painettaessa "Ok" painiketta, lisää "kerailyt" tauluun uuden keräilyn.
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Asettaa asiakasnumeroksi syöttökentän tekstin.
                asiakasn = input.getText().toString();

                //tarkistaa jos asiakasnumero on tyhjä, antaa ilmoitustekstin ja avaa dialogin uudestaan.
                if (asiakasn.equals("")) {
                    Toast.makeText(getApplicationContext(), "Asiakasnumero ei voi olla tyhjä", Toast.LENGTH_LONG).show();
                    openDialog2();
                } else {
                    // Asettaa asiakasnumeroksi syöttökentäntekstin ja lisää tarvittavat välilyönnit (maksimi merkkien määärä = 20).
                    asiakasn = padAsiakasn(input.getText().toString(), 20);

                    // Asettaa ajan formaatin, hakee ajan. Muutetaan kerailyNimi ajaksi.
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    String aika = simpleDateFormat.format(new Date());
                    kerailyNimi = aika;

                    // Lisää uuden keräilyn "kerailyt" tauluun.
                    databaseHelper.addKeraily(kerailyNimi, status);

                    // Hakee lisätyn keräilyn ID:n
                    kerailyID = databaseHelper.getKID();

                    // Tallentaa asiakasnumeron, KerailyID:n sekä kerailyTyypin ja lähettää ne "camera" aktiviteettiin. Käynnistää "camera" aktiviteetin.
                    Intent intent = new Intent(MainActivity.this, camera.class);
                    intent.putExtra("KID", kerailyID);
                    intent.putExtra("ASIAKASN", asiakasn);
                    intent.putExtra("TYYPPI", kerailyTyyppi);
                    startActivity(intent);
                }
            }
        });

        //luo dialogiin takaisin painikkeen, poistuu dialogista.
        builder.setNegativeButton("Takaisin", null);


        final AlertDialog dialog = builder.create();

        //näyttää automaattisesti näppäimistön
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();

        // Asettaa kursorin syöttökenttään.
        input.requestFocus();

        //kuuntelee mitä tekstikentässä tapahtuu. JOS käyttäjä painaa "done" näppäintä, suoritetaan dialogin "ok" painike.
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            //Painettaessa "done" näppäintä, Suorittaa dialogin OK painikkeen.
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
                return true;
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Tarvittavien lupien tarkastus metodi.
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
}


