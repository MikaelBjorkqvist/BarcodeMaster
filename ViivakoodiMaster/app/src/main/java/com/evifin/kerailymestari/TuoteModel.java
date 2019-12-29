package com.evifin.kerailymestari;

import java.io.Serializable;

///////////////////////////////////////////////////////////////////////////////////////////////////
// "TuoteModel" classia käytetään DatabaseHelperin tuotelistan luomisessa
// ("Kaikkien tuotteiden hakumetodi")
public class TuoteModel implements Serializable {


    private String tyyppi, asiakasn, viivakoodi, maara;
    private int id, kid;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getKid() {
        return kid;
    }
    public void setKid(int kid) {
        this.kid = kid;
    }

    public String getTyyppi() {
        return tyyppi;
    }
    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public String getAsiakasn() { return asiakasn; }
    public void setAsiakasn(String asiakasn) { this.asiakasn = asiakasn; }

    public String getViivakoodi() { return viivakoodi; }
    public void setViivakoodi(String viivakoodi) { this.viivakoodi = viivakoodi; }

    public String getMaara() { return maara; }
    public void setMaara(String maara) {
        this.maara = maara;
    }
}