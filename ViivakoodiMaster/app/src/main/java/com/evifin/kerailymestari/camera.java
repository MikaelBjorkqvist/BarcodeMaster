package com.evifin.kerailymestari;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "camera" aktiviteetti. Skannaa viivakoodeja ja lisää niitä muokattavissa olevaan tuotelistaan.
public class camera extends AppCompatActivity {

    // Aktiviteetin muuttujat.
    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;
    private ListView ListView;
    private CustomAdapter2 customAdapter2;
    private ArrayList<TuoteModel> tuoteModelArrayList;
    private DatabaseHelper databaseHelper;
    private String viivakoodi;
    private String asiakasn;
    private String tyyppi;
    private String asiakasnUpdate;
    private int kid;
    private int maara;
    private int tuoteID;
    private TuoteModel tuoteModel;
    //"padAsiakasn" sekä "padViivakoodi" lisää välilyöntejä "viivakoodi", "asiakasnUpdate" sekä "asiakasn" muuttujiin riippuen muuttujien pituudesta.
    public static String padAsiakasn(String s, int n) { return String.format("%-" + n + "s", s); }
    public static String padViivakoodi(String s, int n) { return String.format("%-" + n + "s", s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Aktiviteetin luonti metodi.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hakee aktiviteetin layoutin.
        setContentView(R.layout.activity_camera);

        // Hakee "activity_camera.xml" layoutista kamerakentän, tekstikentän sekä tuotelistan.
        surfaceView = findViewById(R.id.camerapreview);
        textView = findViewById(R.id.textView);
        ListView = findViewById(R.id.lv2);

        // Hakee "main_activity" aktiviteetissa tallennetut muuttujat
        Intent mIntent = getIntent();
        kid = mIntent.getIntExtra("KID", -1);
        tyyppi = mIntent.getStringExtra("TYYPPI");
        asiakasn =mIntent.getStringExtra("ASIAKASN");

        // Luodaan yhteys "DatabaseHelper" sekä "TuoteModel" classeihin.
        databaseHelper = new DatabaseHelper(this);
        tuoteModel = new TuoteModel();

        // Hakee metodin jolla kutsutaan tuotelistaan kaikki tuotteet "kid"(keräilyID) mukaan.
        tuoteModelArrayList = databaseHelper.getAllTuotteet(kid);

        // Luodaan yhteys "customAdapter2" classiin käyttäen muodostettua tuotelistaa.
        customAdapter2 = new CustomAdapter2(this, tuoteModelArrayList);

        // Määrittää listanäkymän käyttäen adapteria.
        ListView.setAdapter(customAdapter2);

        // Kuuntelee jos tuotetta on painettu listanäkymässä ja hakee niille "tuoteID"
        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tuoteID = ((TuoteModel) ListView.getItemAtPosition(position)).getId();
                Log.e("tuoteID: ", String.valueOf(tuoteID));
                // Avaa tuotteen valintametodin
                showAlertDialogButtonClicked();
            }
        });

        // Asentaa "action bar"(yläosan työkalupalkkiin) "takaisin" painikkeen
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Asentaa Google vision API:n viivakooditunnistimen. Hyväksyy kaikki viivakoodityypit.
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS).build();

        // Määrittelee kameranäkymän resoluution sekä aktivoi autofokusoijan
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480).setAutoFocusEnabled(true).build();

        // Määrittelee äänitiedoston viivakoodin lukua varten
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.beep);

        // Google vision API:n kameranäkymä.
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            // Käynnistää kameranäkymän.
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Näkymän muutosmetodi (ei käytössä)
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            // Näkymän pysäytysmetodi (ei käytössä)
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        // Google vision API:n viivakooditunnistin
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            // pysäyttää kameranäkymän
            @Override
            public void release() {
                cameraSource.stop();
            }

            // Skannaa viivakoodeja kameranäkymästä.
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> codes = detections.getDetectedItems();

                if (codes.size() != 0) {

                    textView.post(new Runnable() {

                        @Override
                        public void run() {

                            // Soittaa asetetun äänitiedoston (piipaus).
                            mp.start();

                            // Muuttaa kameranäkymässä olevan tekstikentän tekstin skannatuksi viivakoodiksi.
                            textView.setText(codes.valueAt(0).displayValue);

                            // Lisää skannattuun viivakoodiin tarvittava määrä välilyöntejä. Maksimi merkkimäärä = 29.
                            viivakoodi = padViivakoodi(textView.getText().toString(), 29);

                            // Pysäyttää kameranäkymän
                            release();

                            // Avaa viivakoodi kappalemäärä kyselyn.
                            openDialogMaara();
                        }
                    });
                }
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotelistan tuotteen valintametodi.
    public void showAlertDialogButtonClicked() {

        // Pysäyttää kameranäkymän
        cameraSource.stop();

        // Määrittää dialogin
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko.
        builder.setTitle("Valitse toiminto");

        // Luo "simple" listan vaihtoehtoja sekä kuuntelijan painikkeille.
        String[] vaihtoehdot = {"Määrä", "Asiakasnumero", "Poista", "Peruuta"};
        builder.setItems(vaihtoehdot, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        // Maara: Tuotteen määrän muuttaminen, avaa dialogin.
                        openDialogUpdateMaara();
                        break;
                    }
                    case 1: {
                        // Asiakasnumero: Tuotteen asialasnumeron vaihtaminen, avaa dialogin.
                        openDialogUpdateAsiakasn();
                        break;
                    }
                    case 2: {
                        // Poista: Poistaa tuotteen, avaa dialogin
                        openDialogPoistaTuote();
                        break;
                    }
                    case 3: {
                        // peruuta: Sulkee dialogin ja käynnistää kameranäkymän
                        dialog.dismiss();
                        surfaceView.getHolder();
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        try {
                            cameraSource.start(surfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        textView.setText("Kohdista viivakoodiin");
                        break;
                    }
                }
            }
        });
        // Luo dialogin.
        AlertDialog dialog = builder.create();

        // Estää dialogia sulkeutmasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotteen poistometodi.
    public void openDialogPoistaTuote() {

        // Määrittelee dialogin
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Dialogin otsikko
        builder.setTitle("Poistetaanko tuote?");

        // Lisää dialogin OK painikkeet ja sen kuuntelijan.
        // Poistaa tuotteen käyttäen tuoteID:tä sekä päivittää listanäkymän.
        builder.setPositiveButton("POISTA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteTuote(tuoteID);
                updateListview();

                 // Käynnistää kameranäkymän
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText("Kohdista viivakoodiin");
            }
        });

        // Asentaa "peruuta" painikkeen ja sen kuuntelijan. Käynnistää kameranäkymän uudelleen. Muuttaa tekstikentän tekstin sekä sulkee dialogin.
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText("Kohdista viivakoodiin");

            }
        });

        // Luo dialogin.
        final AlertDialog dialog = builder.create();

        // Estää dialogia sulkeutmasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotteen määrän kyselymetodi.
    public void openDialogMaara() {

        // Määrittelee dialogin
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Anna määrä");

        // Määrittää tekstinsyöttökentän.
        final EditText input = new EditText(this);

        // Syöttökentän maksimi rivimäärä.
        input.setMaxLines(1);

        // valitsee(maalaa) esitäytetyn tekstin jun kursori focusoi syöttökenttään.
        input.setSelectAllOnFocus(true);

        // Näppäimistö muutetaan vain numeraaliseksi.
        input.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);

        // Asetetaan valmiiksi teksti.
        input.setText("1");

        // Asettaa dialogin näkymään tesktinsyötön.
        builder.setView(input);

        // Lisää dialogiin "Ok" painikkeen sekä sen kuuntelijan
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Asetetaan määräksi 0.
                maara = 0;

                // Muuttaa tyhjän string inputin integeriksi, palauttaa nollan.
                try {
                    maara = Integer.parseInt(input.getText().toString());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                // Tarkastaa onko määrä tyhjä/nolla, JOS on, näyttää varoitustekstin ja avaa dialogin uudestaan
                if (maara == 0) {
                    Toast.makeText(getApplicationContext(), "Määrä ei voi olla 0", Toast.LENGTH_LONG).show();
                    openDialogMaara();

                } else if (maara>0){

                    // Lisää tuotteen "tuotteet" taulukkoon.
                    databaseHelper.addTuotteet(tyyppi, asiakasn, viivakoodi, maara, kid);

                    // Päivittää listanäkymän
                    updateListview();

                    // Käynnistää kameranäkymän uudelleen.
                    surfaceView.getHolder();
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Muuttaa kameranäkymässä olevan tekstikentän tekstin.
                    textView.setText("Kohdista viivakoodiin");

                    // Sulkee dialogin.
                    dialog.dismiss();
                }
            }
        });

        // Asentaa "peruuta" painikkeen ja sen kuuntelijan. Käynnistää kameranäkymän uudelleen. Muuttaa tekstikentän tekstin sekä sulkee dialogin.
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText("Kohdista viivakoodiin");

            }
        });

        // Luo dialogin.
        final AlertDialog dialog = builder.create();

        // Näyttää näppäimistön automaattisesti.
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // Estää dialogia sulkeutmasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();

        // Asettaa kursorin syöttökenttään
        input.requestFocus();

        // Syöttökentän kuuntelija. Painettaessa "done" näppäintä, Suorittaa dialogin "ok" painikkeen
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
                return true;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotteen määrän päivittämisen metodi.
    public void openDialogUpdateMaara() {

        // dialogi builderi
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko
        builder.setTitle("Anna uusi määrä");

        // Määrittää dwialoginlle tekstinsyöttökentän
        final EditText input = new EditText(this);

        // Syöttökentän maksimi rivimäärä.
        input.setMaxLines(1);

        // Muuttaa näppäimistön numeraaliseksi.
        input.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);

        // Asettaa dialogin näkymään syöttökentän
        builder.setView(input);

        // lisää dialogin "ok" painikkeen ja sen kuuntelijan
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Asetetaan määräksi 0.
                maara = 0;

                // Muuttaa tyhjän string inputin integeriksi, palauttaa nollan.
                try {
                    maara = Integer.parseInt(input.getText().toString());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                // Tarkastaa onko määrä tyhjä/nolla, JOS on, näyttää varoitustekstin ja avaa dialogin uudestaan
                if (maara == 0) {
                    Toast.makeText(getApplicationContext(), "Määrä ei voi olla 0", Toast.LENGTH_LONG).show();
                } else if (maara>0) {
                    // Päivittää muutetun määrän tuotteelle.
                    databaseHelper.updateMaara(tuoteID, maara);

                    // Päivittää listanäkymän
                    updateListview();
                }

                // Käynnistää kameranäkymän uudelleen.
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Muuttaa kameranäkymässä olevan tekstikentän tekstin.
                textView.setText("Kohdista viivakoodiin");
            }

        });

        // Asettaa dialogiin "peruuta" näppäimen ja sen kuuntelijan. Käynnistää kameranäkymän uudelleen, muuttaa näkymän tekstikentän teksti ja sulkee dialogin.
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                textView.setText("Kohdista viivakoodiin");
                dialog.dismiss();
            }
        });

        // Luo dialogin.
        final AlertDialog dialog = builder.create();

        // Näyttää näppäimistön automaattisesti.
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // Estää dialogia sulkeutmasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        //Näyttää dialogin
        dialog.show();

        //Asettaa kursorin syöttökenttään
        input.requestFocus();

        // Syöttökentän kuuntelija, painettaessa "done" näppäintä, suoritetan dialogin "ok" painike.
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
                return true;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotelistan päivittämisen metodi.
    public void updateListview() {

        // Hakee metodin jolla kutsutaan listaan kaikki tuotteet käyttäen keräilyID:tä (kid).
        tuoteModelArrayList = databaseHelper.getAllTuotteet(kid);

        // Muodostettu lista käytetään adapterilla
        customAdapter2 = new CustomAdapter2(this, tuoteModelArrayList);

        // Listanäkymä luodaan adapterin muokkaamalla listalla.
        ListView.setAdapter(customAdapter2);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Viivakoodin manuaalisen syötön metodi.
    public void openDialogViivakoodi() {

        // Pysäyttää kameranäkymän
        cameraSource.stop();

        // Määrittää dialogin.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko.
        builder.setTitle("Anna viivakoodi");

        // Määrittää tekstinsyöttökentän.
        final EditText input = new EditText(this);

        // Syöttökentän maksimi rivimäärä.
        input.setMaxLines(1);

        // Muuttaa näppäimistön "return" näppäimen "done" actioniksi.
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Asettaa suöttökentän dialogiin näkymään.
        builder.setView(input);

        // lisää dialogiin "Ok painikkeen" ja sen kuuntelijan.
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Muuttaa viivakoodi muuttujan syöttökentän tekstiksi
                viivakoodi = input.getText().toString();

                // Tarkistaa onko syötetty viivakoodi tyhjä, Antaa varoituksen ja käynnistää dialogin uudestaan.
                if (viivakoodi.equals("")) {
                    Toast.makeText(getApplicationContext(), "Viivakoodi ei voi olla tyhjä", Toast.LENGTH_LONG).show();
                    openDialogViivakoodi();
                } else {
                    // Muuttaa viivakoodi muuttujan syöttökentän tekstiksi, ja lisää siihen tarvittavat välilyönnit. Maksimimerkkimäärä = 29.
                    viivakoodi = padViivakoodi(input.getText().toString(), 29);
                    openDialogMaara();
                }
            }
        });

        // Luo "takaisin" painikkeen ja sen kuuntelijan. Käynnistää kameranäkymän uudelleen, muuttaa näkymän tekstin ja sulkee dialogin.
        builder.setNegativeButton("Takaisin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                textView.setText("Kohdista viivakoodiin");
            }
        });

        // Asettaa kursorin syöttökenttään.
        input.requestFocus();
        // Luo dialogi.
        final AlertDialog dialog = builder.create();

        //näyttää automaattisesti näppäimistön
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Estää dialogia sulkeutmasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();

        //kuuntelee mitä tekstikentässä tapahtuu.
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            //muuttaa "return" näppäimen "done" actioniksi. Painettaessa suorittaa dialogin "ok" painikkeen.
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
                return true;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Asiakasnnumeron vaihtamisen metodi.
    public void openDialogAsiakasn() {

        // Pysäyttää kameranäkymän
        cameraSource.stop();

        //Määrittää dialogin
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Dialogin otsikko
        builder.setTitle("Anna asiakasnumero");

        // Määrittää tekstinsyöttökentän.
        final EditText input = new EditText(this);

        // Syöttökentän maksimi rivimäärä.
        input.setMaxLines(1);

        // Muuttaa "return" näppäimen "done" actioniksi.
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Asettaa syöttökentän dialogiin.
        builder.setView(input);

        // lisää dialogiin Ok painikkeen ja sen kuuntelijan.
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Asiakasnumero = syöttökenttään syötetty teksti.
                asiakasn = input.getText().toString();

                // tarkistaa jos asiakasnumero on tyhjä, antaa tarvittaessa varoitustekstin ja avaa dialogin uudestaan.
                if (asiakasn.equals("")) {
                    Toast.makeText(getApplicationContext(), "Asiakasnumero ei voi olla tyhjä", Toast.LENGTH_LONG).show();
                    openDialogAsiakasn();

                } else {
                    // Antaa imoituksen asiakasnumeron vaihdosta.
                    Toast.makeText(getApplicationContext(), "Asiakasnumero vaihdettu", Toast.LENGTH_LONG).show();

                    // Asiakasnumero = syöttökenttään syötetty teksti + tarvittava määrä välilyöntejä (Maksimi merkkimäärä = 20).
                    asiakasn = padAsiakasn(input.getText().toString(), 20);
                }

                // Sulkee dialogin.
                dialog.dismiss();

                // Käynnistää kameranäkymän uudelleen.
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Luo "takaisin" painikkeen ja sen kuuntelijan. Käynnistää kameranäkymän uudelleen, muuttaa näkymän tekstin ja sulkee dialogin.
        builder.setNegativeButton("Takaisin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                textView.setText("Kohdista viivakoodiin");
                dialog.dismiss();
            }
        });


        // Luo dialogin.
        final AlertDialog dialog = builder.create();

        //näyttää automaattisesti näppäimistön.
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin
        dialog.show();

        // Asettaa kursorin syöttökenttään
        input.requestFocus();

        // Syöttökentän kuuntelija. Painettaessa "done" näppäintä, suorittaa dialogin "ok" painikkeen
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            //muuttaa Enter näppäimen done actioniksi. Enter suorittaa dialogin OK painikkeen.
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
                return true;
            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Tuotteen asiakasnumeron päivittämisen metodi.
    public void openDialogUpdateAsiakasn() {

        // Pysäyttää kameranäkymän
        cameraSource.stop();

        // Määrittää dialogin.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dialogin otsikko.
        builder.setTitle("Anna uusi asiakasnumero");

        // Määrittää tekstinsyöttökentän dialogiin.
        final EditText input = new EditText(this);

        // syöttökentän maksimi rivimäärä.
        input.setMaxLines(1);

        // Muuttaa näppäimistön "return" näppäimen "done" actioniksi.
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Asettaa syöttökentäm dialogiin.
        builder.setView(input);

        // lisää dialogiin "Ok" painikkeen ja sen kuuntelijan.
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Asiakasnumeron päivitysmuuttuja = syöttökentän teksti.
                asiakasnUpdate = input.getText().toString();

                // tarkistaa jos asiakasnumero on tyhjä, tarvittaessa antaa varoitus tekstin ja avaa dialogin uudestaan.
                if (asiakasnUpdate.equals("")) {
                    Toast.makeText(getApplicationContext(), "Asiakasnumero ei voi olla tyhjä", Toast.LENGTH_LONG).show();
                    openDialogUpdateAsiakasn();
                } else {

                    // Asiakasnumeron päivitysmuuutuja = syöttökentän teksti sekä tarvittava määrä välilyöntejä. Maksimi merkkimäärä = 20.
                    asiakasnUpdate = padAsiakasn(input.getText().toString(), 20);

                    // Päivittää asiakasnumeron tuotteelle.
                    databaseHelper.updateAsiakasn(tuoteID, asiakasnUpdate);

                    // Päivittää listanäkymän
                    updateListview();

                    // Antaa ilmoitus tesktin asiakasnumeron vaihtumisesta.
                    Toast.makeText(getApplicationContext(), "Asiakasnumero vaihdettu", Toast.LENGTH_LONG).show();
                }

                // Käynnistää kameranäkymän uudelleen.
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        // Luo "takaisin" painikkeen ja sen kuuntelijan. Käynnistää kameranäkymän uudelleen, muuttaa näkymän tekstin ja sulkee dialogin.
        builder.setNegativeButton("Takaisin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                surfaceView.getHolder();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                textView.setText("Kohdista viivakoodiin");
            }
        });

        // Luo määritetyn dialogin
        final AlertDialog dialog = builder.create();

        //näyttää automaattisesti näppäimistön
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Estää dialogia sulkeutumasta jos käyttäjä painaa dialogin ulkopuolelle.
        dialog.setCanceledOnTouchOutside(false);

        // Näyttää dialogin.
        dialog.show();

        // Asettaa kursorin tekstikenttään
        input.requestFocus();

        //kuuntelee syöttökenttää. "done" näppäintä painettaessa, suorittaa dialogin "ok" painikkeen
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            //muuttaa Enter näppäimen done actioniksi. Enter suorittaa dialogin OK painikkeen.
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                }
                return true;
            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Työkalupalkin ("ActionBar") layoutin määritys metodi.
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Hakee työkalupalkkiin "menu_camera" layoutin.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Työkalupalkin painikkeiden toiminnot.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Hakee "menu_camera" layoutista "itemit" painikkeiksi.
        // Kuuntelee mitä "itemiä" painetaan

        switch (item.getItemId()){

            // "Takaisin" painike palauttaa edelliseen aktiviteettiin.
            case android.R.id.home:
                int id = item.getItemId();
                if (id == android.R.id.home) {
                    this.finish();
                }
                break;

            // Hakee ikonin "asiakasnumeron muuttaminen" itemille ja avaa asiakasnnumeron vaihtamisen metodin
            case R.id.action_asiakasn:
                openDialogAsiakasn();
                break;

            // Hakee ikonin "viivakoodin lisäys" itemille ja avaa vivakoodin manuaalisen syötön metodin
            case R.id.action_lisaa:
                openDialogViivakoodi();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

