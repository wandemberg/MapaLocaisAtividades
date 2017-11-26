package mdcc.ufc.br.mapalocaisatividades;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.LocationManager;
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

import java.io.FileReader;
import java.io.InputStream;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    //SupportMapFragment é usada para exibir o mapa
    //OnMapReadyCallback quando o mapa estiver pronto para o uso

    private GoogleMap mMap; //

    //Responsável por localizar o dispositivo
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
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

        if (result != null) {

//            Toast.makeText(getActivity(), "JSON: " + result, Toast.LENGTH_LONG).show();

            Gson gson = new Gson();
            LugarEsportivo[] lugar = gson.fromJson(result, LugarEsportivo[].class);

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
                //Adiciona o marcador
                mMap.addMarker(markerJson);

            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lugar[0].getLat(), lugar[0].getLng())));

        }



    }



}
