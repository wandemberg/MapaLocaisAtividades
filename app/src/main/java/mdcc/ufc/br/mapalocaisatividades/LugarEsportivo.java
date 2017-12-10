package mdcc.ufc.br.mapalocaisatividades;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by wandemberg on 26/11/17.
 */

public class LugarEsportivo {


    private int id;
    private float lat;
    private float lng;
    private String title;
    private boolean localApropriado;
    private int qualidadeAr;
    private int ruido;

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isLocalApropriado() {
        return localApropriado;
    }

    public void setLocalApropriado(boolean localApropriado) {
        this.localApropriado = localApropriado;
    }

    public int getQualidadeAr() {
        return qualidadeAr;
    }

    public void setQualidadeAr(int qualidadeAr) {
        this.qualidadeAr = qualidadeAr;
    }

    public int getRuido() {
        return ruido;
    }

    public void setRuido(int ruido) {
        this.ruido = ruido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
