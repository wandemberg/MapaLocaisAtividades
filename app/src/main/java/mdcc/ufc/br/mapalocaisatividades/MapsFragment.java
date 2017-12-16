package mdcc.ufc.br.mapalocaisatividades;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    //SupportMapFragment é usada para exibir o mapa
    //OnMapReadyCallback quando o mapa estiver pronto para o uso

    private GoogleMap mMap; //
    private ArrayList<MarkerOptions> mMarkers = new ArrayList<MarkerOptions>();
    private ArrayList<Marker> todosMarkers = new ArrayList<Marker>();

    //Responsável por localizar o dispositivo
    private LocationManager locationManager;

    //Timer que atualiza o mapa
    private Timer myTimer;
    private UpdateMarkerTask updateMarkerTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (updateMarkerTask != null)
            updateMarkerTask.cancel();
        if (myTimer != null)
            myTimer.cancel();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) { //Procedimentos realizados quando o mapa estiver pronto

//        try {
        //Obter a referência dos dados de localização
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //Permite realizar buscar relacionadas ao provider, buscas específicas
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //Busca o melhor provider
        String provider = locationManager.getBestProvider(criteria, true);

        Toast.makeText(getActivity(), "Provider: " + provider, Toast.LENGTH_LONG).show();

        mMap = googleMap;
        //Habilita o botão de zoom no mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);

//            mMap.getUiSettings().setMyLocationButtonEnabled(true);

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


//            //Mostra a localização do dispositivo. Obs.: Deve configurar em tempo de execução a partir da versão 6.0 do android.
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                mMap.setMyLocationEnabled(true);
//            } else {
//                // Show rationale and request permission.
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//
//            }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

//            mMap.setMyLocationEnabled(true);
//        } catch (SecurityException ex) {
//            Log.e(TAG, "Error", ex);
//        }

//        // Coordenadas mucuripe
//        LatLng mucuripe = new LatLng(-3.71667, -38.4667);
//        MarkerOptions marker = new MarkerOptions();
//        marker.position(mucuripe);
//        marker.title("Mucuripe");
//        mMap.addMarker(marker);
//
//        // Coordenadas Trilha Cocó
//        LatLng trilhaCoco = new LatLng(-3.752325, -38.4826098);
//        MarkerOptions markerTrilhaCoco = new MarkerOptions();
//        markerTrilhaCoco.position(trilhaCoco);
//        markerTrilhaCoco.title("Trilha Cocó");
//        mMap.addMarker(markerTrilhaCoco);
//
//        // Coordenadas Aterro da Praia de Iracema
//        LatLng praiaIracema = new LatLng(-3.7217, -38.4826098);
//        MarkerOptions markerPraiaIracema = new MarkerOptions();
//        markerPraiaIracema.position(trilhaCoco);
//        markerPraiaIracema.title("Aterro da Praia de Iracema");
//        mMap.addMarker(markerPraiaIracema);

//        String arquivoJson = "{\"title\":\"Praça do Calçadão da Crasa\",\n" +
//                "\"lat\":-3.7317822,\n" +
//                "\"lng\":-38.5489838,\n" +
//                "\"localApropriado\":true,\n" +
//                "\"qualidadeAr\":80,\n" +
//                "\"ruido\":20}";

        String result;
        try {
            AssetManager assets = getContext().getAssets();
            InputStream in_s = assets.open("lugares.json");

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            result = new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            result = "Error: can't show file.";
        }

        //Salva no Json local sem ser no arquivo
        saveData(getContext(), result);

        if (result != null) {

//            Toast.makeText(getActivity(), "JSON: " + result, Toast.LENGTH_LONG).show();

            Gson gson = new Gson();
            //Busca do JSON local
            LugarEsportivo[] lugar = gson.fromJson(getData(getContext()), LugarEsportivo[].class);

            for (int i = 0; i < lugar.length; i++) {
//                Toast.makeText(getActivity(), "Lugar adicionado: " + lugar[i].getTitle()
//                        +" lat: " + lugar[i].getLat() +"e lng: " + lugar[i].getLng(), Toast.LENGTH_LONG).show();

                MarkerOptions markerJson = new MarkerOptions();
                markerJson.position(new LatLng(lugar[i].getLat(), lugar[i].getLng()));
                markerJson.title(lugar[i].getTitle());

                markerJson.snippet("Qualidade do Ar: " + lugar[i].getQualidadeAr()
                        + "\nRuido: " + lugar[i].getRuido());

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        LinearLayout info = new LinearLayout(getContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

                if (lugar[i].isLocalApropriado()) {
                    //Modifica a cor para azul
                    markerJson.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else {

                }
                //Guarda o markers para atualizá-lo depois
                mMarkers.add(markerJson);
                //Adiciona o marcador no mapa
               Marker mk = mMap.addMarker(markerJson);
                todosMarkers.add(mk);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lugar[0].getLat(), lugar[0].getLng())));

        }

        Log.d(TAG, "Vai disparar o Timer");

        long delay = 0;
        long period = 10000;

        //Removi para gerar o gráfico do artigo
        myTimer = new Timer();
        updateMarkerTask = new UpdateMarkerTask();
        myTimer.schedule(updateMarkerTask, delay, period);

        Log.d(TAG, "Disparou o Timer");

//        AtualizarMarker atualizarMarker = new AtualizarMarker();
//        atualizarMarker.execute();

    }

    private boolean finalizar = false;

    private class AtualizarMarker extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... v) {

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
//            setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //            showDialog("Downloaded " + result + " bytes");
            Log.e("Teste", "Entrou no doInBackground");
//            while (finalizar != true) {
            String result;
//                try {
//                    AssetManager assets = getContext().getAssets();
//                    InputStream in_s = assets.open("lugares.json");
//
//                    byte[] b = new byte[in_s.available()];
//                    in_s.read(b);
//                    result = new String(b);
//                } catch (Exception e) {
//                    // e.printStackTrace();
//                    result = "Error: can't show file.";
//                }

            //Obtém a string do JSON local
            result = getData(MapsFragment.this.getContext());
            if (result != null) {

//            Toast.makeText(getActivity(), "JSON: " + result, Toast.LENGTH_LONG).show();

                Gson gson = new Gson();
                LugarEsportivo[] lugar = gson.fromJson(result, LugarEsportivo[].class);


                    mMap.clear();

                for (int i = 0; i < lugar.length; i++) {
//                Toast.makeText(getActivity(), "Lugar adicionado: " + lugar[i].getTitle()
//                        +" lat: " + lugar[i].getLat() +"e lng: " + lugar[i].getLng(), Toast.LENGTH_LONG).show();

                    Log.e("Teste", "Lugar Atualizado: " + lugar[i].getTitle()
                            + " Ar: " + lugar[i].getQualidadeAr() + "e ruido: " + lugar[i].getRuido());

                    if (i < todosMarkers.size()) {
                        Log.i("Teste", "i < mMarkers.size() ");
                        //Obtem o marker com indice passado
                        MarkerOptions markerJson = mMarkers.get(i);
//                            Log.e("Teste", "Todos markers: " + mMarkers.toString());
//                            Log.e("Teste", "Vai buscar marker: " + markerJson);

//                        Collection<MarkerOptions> ma = mMarkers.values();
//                        for (MarkerOptions m: ma) {
//                            Log.e("Teste", "Valores dos markers: " +m.getTitle() + ",  " +m.getSnippet());
//                        }

                        //Se encontrar o marker
                        if (markerJson != null) {

//                                Log.e("Teste", "valor markerJson" + markerJson);
                            markerJson.position(new LatLng(lugar[i].getLat(), lugar[i].getLng()));
                            markerJson.title(lugar[i].getTitle());

                            markerJson.snippet("Qualidade do Ar: " + lugar[i].getQualidadeAr()
                                    + "\nRuido: " + lugar[i].getRuido()
                                    + "\nBom local : " + lugar[i].isLocalApropriado());


                            if (lugar[i].isLocalApropriado()) {
                                //Modifica a cor para azul
                                markerJson.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            } else {
                                markerJson.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                            }

                            Log.e("Teste", "Valor do marker: "+markerJson.getSnippet());
                            mMap.addMarker(markerJson);

//                            todosMarkers.get(i).showInfoWindow();

                        }


                        Log.e("Teste", "Atualizou todos markers");
                    }
//                    Toast.makeText(MapsFragment.this.getContext(), "Atualizou Todos Markers", Toast.LENGTH_LONG).show();
                }


            }
//                try {
//                    Thread.sleep(15000);//Your Interval after which you want to refresh the screen
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }


    static String fileName = "myBlog.json";

    public static void saveData(Context context, String mJsonResponse) {
        try {
            FileWriter file = new FileWriter(context.getFilesDir().getPath() + "/" + fileName);
            file.write(mJsonResponse);
            file.flush();
            file.close();
        } catch (IOException e) {
            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
        }
    }

    public static String getData(Context context) {
        try {
            File f = new File(context.getFilesDir().getPath() + "/" + fileName);
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }

    class UpdateMarkerTask extends TimerTask {

        public void run() {
            AtualizarMarker atualizarMarker = new AtualizarMarker();
            atualizarMarker.execute();
        }
    }

}
