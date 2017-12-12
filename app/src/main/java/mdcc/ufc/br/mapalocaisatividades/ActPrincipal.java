package mdcc.ufc.br.mapalocaisatividades;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ActPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MqttCallback {

    private FragmentManager fragmentManager; //Recupera os fragmentos de janelas

    private static final String TAG = "Teste";
    private IMqttToken token;
    private MqttConnectOptions options;
    private MqttAndroidClient client;
    private String ipServidor = "192.168.25.20";
    private String portaServidor = "1883";
    private String protocoloServidor = "tcp";
    //Tópico que deseja se inscrever
    private final String topic = "HelloWorld";
    //Tópico do segundo sensor que deseja se inscrever
    private final String topicSensor2 = "noise2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_principal);

        //Aplicação se conecta ao Broker e se inscrever nos tópicos selecionados
        conectarBrokerMqtt();

        //Barra principal lá de cima
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Menu
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Item que está selecionando
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();
        //Responsável por iniciar uma transação para poder adicionar um fragmento a activity
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //Adicionar o fragmento a activity
        transaction.add(R.id.container, new MapsFragment(), "MapsFragment");
        //Confirmar a transação
        transaction.commitAllowingStateLoss();

    }

    //Método que conecta ao Broker
    public void conectarBrokerMqtt(){

        //MQTTConnect options : setting version to MQTT 3.1.1
        options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
//        options.setUserName("your_mqtt_username_here");
//        options.setPassword("your_mqtt_password_here".toCharArray());

        //Below code binds MainActivity to Paho Android Service via provided MqttAndroidClient
        // client interface
        //Todo : Check why it wasn't connecting to test.mosquitto.org. Isn't that a public broker.
        //Todo : .check why client.subscribe was throwing NullPointerException  even on doing subToken.waitForCompletion()  for Async                  connection estabishment. and why it worked on subscribing from within client.connect’s onSuccess(). SO
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), protocoloServidor+"://"+ipServidor+":"+ portaServidor,
                        clientId);

        try {
            token = client.connect(options);

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(ActPrincipal.this, "Connection successful", Toast.LENGTH_SHORT).show();
                    //Subscribing to a topic door/status on broker.hivemq.com
                    client.setCallback((MqttCallback) ActPrincipal.this);

                    int qos = 1; //At least once (1) receber pelo menos uma mensagem
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // successfully subscribed
                                Toast.makeText(ActPrincipal.this, "Successfully subscribed to: " + topic, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Toast.makeText(ActPrincipal.this, "Couldn't subscribe to: " + topic, Toast.LENGTH_SHORT).show();

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                        Toast.makeText(ActPrincipal.this, "1 Erro conectar conectarBrokerMqtt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (NullPointerException e) {
                        Toast.makeText(ActPrincipal.this, "2 Erro conectar conectarBrokerMqtt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    try {
                        IMqttToken subToken = client.subscribe(topicSensor2, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // successfully subscribed
                                Toast.makeText(ActPrincipal.this, "Successfully subscribed to: " + topicSensor2, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Toast.makeText(ActPrincipal.this, "Couldn't subscribe to: " + topicSensor2, Toast.LENGTH_SHORT).show();

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                        Toast.makeText(ActPrincipal.this, "1 Erro conectar conectarBrokerMqtt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (NullPointerException e) {
                        Toast.makeText(ActPrincipal.this, "2 Erro conectar conectarBrokerMqtt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }



                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    Toast.makeText(ActPrincipal.this, "Connection failed", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (MqttException e) {
            Toast.makeText(ActPrincipal.this, "3 Erro conectar conectarBrokerMqtt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //Quando o botão de voltar é pressionado para fechar o menu.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) { //Fecha o menu caso estiver aberto
            drawer.closeDrawer(GravityCompat.START);
        } else {//Finaliza a aplicação ou Volta para uma activity anterior
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        }
        //Para fechar o menu após clicar em um item.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Métodos interface MQTT
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        clientHandle = null; // avoid reuse!
//
//        if (token != null) {
//            ((MqttTokenAndroid) token).notifyComplete();
//        }
//        if (callback != null) {
//            callback.connectionLost(null);
//        }

        if (client != null) {
            try {
                //Identifica que não quer mais estar inscrito nos tópicos
                client.unsubscribe(topic);
                client.unsubscribe(topicSensor2);
                //Fecha a conexão
                client.disconnect();
                Toast.makeText(ActPrincipal.this, "Fechou a conexão", Toast.LENGTH_LONG).show();

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        /*
        * To test ,publish  "open"/"close" at topic you subscibed app to in above .
        * */
//        ImageView doorImage = (ImageView)findViewById(R.id.door_image);

        Log.d("door",message.toString());

//        if(message.toString().equals("close")){
//            doorImage.setImageResource(R.drawable.closed_door);
//        }
//        else {
//            doorImage.setImageResource(R.drawable.open_door);
//        }

//        Toast.makeText(ActPrincipal.this, "Topic: "+topic+"\nMessage: "+message, Toast.LENGTH_LONG).show();
        Log.i(TAG,"Topic: "+topic+"\nMessage: "+message);

                String result;
//        try {
//            AssetManager assets = ActPrincipal.this.getAssets();
//            InputStream in_s = assets.open("lugares.json");
//
//            byte[] b = new byte[in_s.available()];
//            in_s.read(b);
//            result = new String(b);
//        } catch (Exception e) {
//            // e.printStackTrace();
//            result = "Error: can't show file.";
//        }

        result = getData(ActPrincipal.this);

        if (result != null) {

//            Toast.makeText(getActivity(), "JSON: " + result, Toast.LENGTH_LONG).show();

            Gson gson = new Gson();
            LugarEsportivo[] lugar = gson.fromJson(result, LugarEsportivo[].class);


            String msgRecebida = new String(message.getPayload());
            String[] parteRecebida = msgRecebida.split(";");
            String nomeSensor = parteRecebida[0].split("=")[1];
            String idSensor = parteRecebida[1].split("=")[1];
            String valorMsgSensor = parteRecebida[2].split("=")[1];
            String timeStampSensor = parteRecebida[3].split("=")[1];

//            Toast.makeText(ActPrincipal.this, "Vai atualizar dados de idSensor: " + idSensor, Toast.LENGTH_LONG).show();


//            Toast.makeText(ActPrincipal.this, "parteRecebida[0]: " + parteRecebida[0], Toast.LENGTH_LONG).show();
//            Toast.makeText(ActPrincipal.this, "parteRecebida[1]: " + parteRecebida[1], Toast.LENGTH_LONG).show();
//            Toast.makeText(ActPrincipal.this, "parteRecebida[2]: " + parteRecebida[2], Toast.LENGTH_LONG).show();
//            Toast.makeText(ActPrincipal.this, "parteRecebida[3]: " + parteRecebida[3], Toast.LENGTH_LONG).show();

            for (int i = 0; i < lugar.length; i++) {

                //Encontrar um sensor com mesmo ID
                if (idSensor.equals(String.valueOf(lugar[i].getId()))) {
//                    Toast.makeText(ActPrincipal.this, "Encontrou um sensor com idSensor: " + idSensor, Toast.LENGTH_LONG).show();

                    if (nomeSensor.equals("Air")) {
                        lugar[i].setQualidadeAr(Integer.parseInt(valorMsgSensor));
                    } else if(nomeSensor.equals("noise2")) {
                        lugar[i].setRuido(Integer.parseInt(valorMsgSensor));
                    } else { //Tratar a mensagem de HelloWorld
                        lugar[i].setQualidadeAr(Integer.parseInt(valorMsgSensor));
                    }

                    Log.i("Teste", "local proprio: " + calcularLocalProprio(lugar[i]));

                    lugar[i].setLocalApropriado(calcularLocalProprio(lugar[i]));

//                    gson.

                    //Atualizar os dados no JSON
//                    JSONObject jsonResultado = new JSONObject(result);

                    JSONArray arrResultado = new JSONArray(result);

                    JSONObject jsonObject = (JSONObject) arrResultado.get(i);

                    //Atualiza os dados que estão no JSON
                    jsonObject.put("qualidadeAr", lugar[i].getQualidadeAr());
                    jsonObject.put("ruido", lugar[i].getRuido());
                    jsonObject.put("localApropriado", lugar[i].isLocalApropriado());

                    arrResultado.put(jsonObject);

////                    Lista que vai salvar
//                    List listaSalvar = new ArrayList<JsonObject>();
//                    listaSalvar.add(jsonObject);
//
//                    myWriter(listaSalvar, gson);

                    Log.i("Teste", "JSON lugar[i].getQualidadeAr(): " + lugar[i].getQualidadeAr());
                    Log.i("Teste", "JSON arrResultado.get(i).toString(): " + arrResultado.get(i).toString());
                    Log.i("Teste", "JSON resultado: " + arrResultado.toString());

                    saveData(ActPrincipal.this, arrResultado.toString());

//                    String filename = "lugares.json";
//                    String string = arrResultado.toString();
//                    FileOutputStream outputStream;
//
//                    try {
//                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                        outputStream.write(string.getBytes());
//                        outputStream.close();
//                    } catch (Exception e) {
//                        Log.e("Teste", "Erro " + e.getMessage());
//                        e.printStackTrace();
//                    }

//                    String resultSalvar;
//                    try {
//                        AssetManager assets = ActPrincipal.this.getAssets();
//                        InputStream in_s = assets.open("lugares.json");
//
//                        byte[] b = new byte[in_s.available()];
//                    Toast.makeText(ActPrincipal.this, "Atualizou dados de idSensor: " + idSensor + ", Ar: " + lugar[i].getQualidadeAr(), Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Atualizou dados de idSensor: " + idSensor + ", Ar: " + lugar[i].getQualidadeAr());

                    break;
                }



            }
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

    private void myWriter(List<JsonObject> jsonObjectHolder, Gson gson) throws IOException
    {
        JsonWriter writer = new JsonWriter(new FileWriter(new File("lugares.json")));

        writer.beginArray();

        for (JsonObject jsonObject : jsonObjectHolder)
        {
            gson.toJson(jsonObject, writer);
        }

        writer.endArray();

        writer.close();
    }

    public boolean calcularLocalProprio(LugarEsportivo lugar){
        if (lugar.getQualidadeAr() <= 50 || lugar.getRuido() >= 200) {
//                        in_s.read(b);
//                        result = new String(b);
//                    } catch (Exception e) {
//                        // e.printStackTrace();
//                        result = "Error: can't show file.";
//                    }

            return false;
        } else  {
            return true;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
