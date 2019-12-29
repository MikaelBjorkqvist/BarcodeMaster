package com.evifin.kerailymestari;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "CustomAdapter2" classia käytetään luomaan "databaseHelper.getAllTuotteet" metodilla muodostetun
// tuotelistan tuotelistanäkymä "camera" aktiviteettiin
public class CustomAdapter2 extends BaseAdapter {

    private Context context;
    private ArrayList<TuoteModel> tuoteModelArrayList;

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Listan hakumetodi
    public CustomAdapter2(Context context, ArrayList<TuoteModel> tuoteModelArrayList) {

        this.context = context;
        this.tuoteModelArrayList = tuoteModelArrayList;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Palauttaa listan suuruuden
    @Override
    public int getCount() {
        return tuoteModelArrayList.size();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Palauttaa listasta valitun itemin (tuotteen)
    @Override
    public Object getItem(int position) {
        return tuoteModelArrayList.get(position);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Palauttaa listasta valitun itemin id:n
    @Override
    public long getItemId(int position) {
        return 0;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Määrittää ja palauttaa listanäkymän.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Layoutiksi määritetään "lv2_item"
            convertView = inflater.inflate(R.layout.lv2_item, null, true);

            // Haetaan muuttujille layoutista ID:t
            holder.tvasiakasn = (TextView) convertView.findViewById(R.id.asiakasn);
            holder.tvviivakoodi = (TextView) convertView.findViewById(R.id.viivakoodi);
            holder.tvmaara = (TextView) convertView.findViewById(R.id.maara);


            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        // Luo muuttuja jolla haetaan asiakasnumero.
        String asiakasn = tuoteModelArrayList.get(position).getAsiakasn();

        // Tarkistaa onko asiakasnumero null. Tarvittaessa muuttaa sen tyhjäksi stringiksi.
        if (asiakasn == null) {
            asiakasn = " ";
        }

        // Asettaa itemin asiakasnumeron määritellysti.
        holder.tvasiakasn.setText("asiakasn: " + asiakasn);

        // Hakee ja asettaa viivakoodin itemille
        holder.tvviivakoodi.setText("tuote: " +tuoteModelArrayList.get(position).getViivakoodi());

        // Hakee ja asettaa määrän itemille.
        holder.tvmaara.setText("kpl: "+tuoteModelArrayList.get(position).getMaara());

        return convertView;
    }

    private class ViewHolder {

        protected TextView tvasiakasn, tvviivakoodi, tvmaara;
    }

}