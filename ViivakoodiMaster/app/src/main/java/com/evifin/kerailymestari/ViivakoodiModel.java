package com.evifin.kerailymestari;

import java.io.Serializable;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "ViivakoodiModel" classia käytetään DatabaseHelperin keräilylistan luomisessa
// ("Kaikkien keräilyjen hakumetodi")
public class ViivakoodiModel implements Serializable {


    private String kerailyNimi, tuotteet;
    private int kerailyID, kerailyStatus;

    public void setTuoteViivakoodi(String viivakoodi) {
        this.tuotteet = viivakoodi;
    }

    public int getKerailyId() {
        return kerailyID;
    }

    public void setKerailyId(int id) {
        this.kerailyID = id;
    }

    public String getKerailyNimi() {
        return kerailyNimi;
    }

    public int getKerailyStatus() { return kerailyStatus; }

    public void setKerailyStatus(int status) { this.kerailyStatus = status; }

    public void setKerailyNimi(String nimi) { this.kerailyNimi = nimi; }
}