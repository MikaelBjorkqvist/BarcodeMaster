package com.evifin.kerailymestari;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "CustomAdapter" classia käytetään luomaan "databaseHelper.getAllUsers" metodilla muodostetun
// keräilylistan keräilylistanäkymä "results" aktiviteettiin
public class CustomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ViivakoodiModel> viivakoodiModelArrayList;

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Listan hakumetodi
    public CustomAdapter(Context context, ArrayList<ViivakoodiModel> viivakoodiModelArrayList) {
        this.context = context;
        this.viivakoodiModelArrayList = viivakoodiModelArrayList;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Palauttaa listan suuruuden
    @Override
    public int getCount() {
        return viivakoodiModelArrayList.size();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Palauttaa listasta valitun itemin (keräilyn)
    @Override
    public Object getItem(int position) {
        return viivakoodiModelArrayList.get(position);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Palauttaa listasta valitun keräilyn itemin.
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

            // Layoutiksi määritetään "lv_item"
            convertView = inflater.inflate(R.layout.lv_item, null, true);

            // Haetaan muuttujille layoutista ID:t
            holder.tvkeraily = (TextView) convertView.findViewById(R.id.lv);
            holder.tvstatus = (ImageView) convertView.findViewById(R.id.status);

            convertView.setTag(holder);

        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        // Luodaan muuttuja joka hakee keräilyn statuksen
        int status = viivakoodiModelArrayList.get(position).getKerailyStatus();

        // Lista itemin "nimi" muutetaan keräilyn nimeksi (joka on aika)
        holder.tvkeraily.setText(viivakoodiModelArrayList.get(position).getKerailyNimi());

        // Määritetään lista itemin (keräilyn) "status" kuva. Tarkistaa onko status 0 tai 1.
        if (status == 1)
        {
            holder.tvstatus.setImageResource(R.drawable.ic_positive);
        }else {
            holder.tvstatus.setImageResource(R.drawable.ic_negative);
        }

        return convertView;
    }

    private class ViewHolder {

        protected TextView tvkeraily;
        protected ImageView tvstatus;
    }



}