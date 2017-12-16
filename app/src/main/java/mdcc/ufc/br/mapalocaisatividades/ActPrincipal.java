package mdcc.ufc.br.mapalocaisatividades;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.List;

public class ActPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MqttCallback {

    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_FILE = 20;
    private FragmentManager fragmentManager; //Recupera os fragmentos de janelas

    private static final String TAG = "Teste";
    private IMqttToken token;
    private MqttConnectOptions options;
    private MqttAndroidClient client;
//    private String ipServidor = "10.99.12.69";
    private String ipServidor = "192.168.25.20";

    private String portaServidor = "1883";
    private String protocoloServidor = "tcp";
    //Tópico que deseja se inscrever
    private final String topic = "air2";
    //Tópico do segundo sensor que deseja se inscrever
    private final String topicSensor2 = "noise2";
    private int limeteQualidadeAr = 50;
    private int limeteRuido = 180;
    private int maxQualidadeAr = 100;
    private  int maxRuido = 300;
    private EditText valorSensorAr;
    private EditText valorSensorRuido;
    //Configurar valores
    private AlertDialog alerta;
    private String nomeArquivoMsgRecebidas = "senseTeste100Sensores20min.csv";
    //Contador do número de msgs recebidas.
    private long count = 0;
    private File arquivoMsgRecebidas;

    @RequiresApi(api = Build.VERSION_CODES.M)
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

        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_camera){

                    AlertDialog.Builder configurar = new AlertDialog.Builder(ActPrincipal.this);
                    View view = getLayoutInflater().inflate(R.layout.dialog_sensor, null);
                    valorSensorAr = (EditText) view.findViewById(R.id.valorAr);
                    valorSensorRuido = (EditText) view.findViewById(R.id.valorRuido);
                    Button botaoOk = (Button) view.findViewById(R.id.btOk);
                    botaoOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!valorSensorAr.getText().toString().isEmpty()
                                    && !valorSensorRuido.getText().toString().isEmpty()) {

                                    if ( Integer.parseInt(valorSensorAr.getText().toString()) >= 0 &&
                                            Integer.parseInt(valorSensorAr.getText().toString()) <= maxQualidadeAr &&
                                        Integer.parseInt(valorSensorRuido.getText().toString()) >= 0 &&
                                        Integer.parseInt(valorSensorRuido.getText().toString()) <= maxRuido) {
                                        limeteQualidadeAr = Integer.parseInt(valorSensorAr.getText().toString());
                                        limeteRuido = Integer.parseInt(valorSensorRuido.getText().toString());
                                        alerta.cancel();
                                    } else {
                                        AlertDialog.Builder dialogo = new AlertDialog.Builder(ActPrincipal.this);
                                        dialogo.setTitle("Aviso");
                                        dialogo.setMessage("Valor máximo 100 para sensor de ar e 300 para sensor de ruido." );
                                        dialogo.setNeutralButton("OK", null);
                                        dialogo.show();
                                    }
                            } else {
                                AlertDialog.Builder dialogo = new AlertDialog.Builder(ActPrincipal.this);
                                dialogo.setTitle("Aviso");
                                dialogo.setMessage("Valores dos sensores estão vazios." );
                                dialogo.setNeutralButton("OK", null);
                                dialogo.show();
                            }

                        }
                    });

                    configurar.setView(view);
                    alerta = configurar.create();
                    alerta.show();

                    return false;
                }

                return true;
            }
        });

      //Para forçar a permissão de gravar o arquivo externo
        int permission = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.e(TAG, "Permissao: " + permission);
        Toast.makeText(ActPrincipal.this, "Permissao: " + permission, Toast.LENGTH_SHORT).show();
        if (permission == -1) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_WRITE_FILE);

        }
        permission = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.e(TAG, "Após Permissao: " + permission);
        //Criar o arquivo que será gravadas as mensagens recebidas
        arquivoMsgRecebidas = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nomeArquivoMsgRecebidas);

//        //Criar notificação
//        NotificationCompat.Builder mBuilder =
//                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_menu_camera)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!");
//        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, ResultActivity.class);
//
//// The stack builder object will contain an artificial back stack for the
//// started Activity.
//// This ensures that navigating backward from the Activity leads out of
//// your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//// Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(ResultActivity.class);
//// Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//// mId allows you to update the notification later on.
//        mNotificationManager.notify(10, mBuilder.build());

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

        if (id == R.id.action_sobre) {

            AlertDialog.Builder dialogo = new AlertDialog.Builder(ActPrincipal.this);
            dialogo.setTitle("Aviso");
            dialogo.setMessage("Marcadores Azuis indicam bons locais e Vermelhos ruins locais para prática esportiva." );
            dialogo.setNeutralButton("OK", null);
            dialogo.show();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
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

        //REMOVI para teste
//        Log.d(TAG,message.toString());


//        Toast.makeText(ActPrincipal.this, "Topic: "+topic+"\nMessage: "+message, Toast.LENGTH_LONG).show();


        //REMOVI para teste
//        Log.i(TAG,"Topic: "+topic+"\nMessage: "+message);


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


            //Salvar dados no arquivo externo para testes de confiabilidade realizado
            //salvarTeste(nomeSensor,idSensor,timeStampSensor, valorMsgSensor);


//            Toast.makeText(ActPrincipal.this, "Vai atualizar dados de idSensor: " + idSensor, Toast.LENGTH_LONG).show();


//            Toast.makeText(ActPrincipal.this, "parteRecebida[0]: " + parteRecebida[0], Toast.LENGTH_LONG).show();
//            Toast.makeText(ActPrincipal.this, "parteRecebida[1]: " + parteRecebida[1], Toast.LENGTH_LONG).show();
//            Toast.makeText(ActPrincipal.this, "parteRecebida[2]: " + parteRecebida[2], Toast.LENGTH_LONG).show();
//            Toast.makeText(ActPrincipal.this, "parteRecebida[3]: " + parteRecebida[3], Toast.LENGTH_LONG).show();

            for (int i = 0; i < lugar.length; i++) {

                //Encontrar um sensor com mesmo ID
                if (idSensor.equals(String.valueOf(lugar[i].getId()))) {
//                    Toast.makeText(ActPrincipal.this, "Encontrou um sensor com idSensor: " + idSensor, Toast.LENGTH_LONG).show();

                    if (nomeSensor.equals("air2")) {
                        lugar[i].setQualidadeAr(Integer.parseInt(valorMsgSensor));
                    } else if(nomeSensor.equals("noise2")) {
                        lugar[i].setRuido(Integer.parseInt(valorMsgSensor));
                    } else { //Tratar a mensagem de HelloWorld
                        lugar[i].setQualidadeAr(Integer.parseInt(valorMsgSensor));
                    }

                    //REMOVI para teste
//                    Log.i("Teste", "local proprio: " + calcularLocalProprio(lugar[i]));

                    lugar[i].setLocalApropriado(calcularLocalProprio(lugar[i]));

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

                    //REMOVI para teste
//                    Log.i("Teste", "JSON lugar[i].getQualidadeAr(): " + lugar[i].getQualidadeAr());
//                    Log.i("Teste", "JSON arrResultado.get(i).toString(): " + arrResultado.get(i).toString());
//                    Log.i("Teste", "JSON resultado: " + arrResultado.toString());

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

                    //REMOVI para teste
//                    Log.i(TAG, "Atualizou dados de idSensor: " + idSensor + ", Ar: " + lugar[i].getQualidadeAr());

                    break;
                }



            }
        }
    }

//    protected void createNotification(String status, String title, String content) {
//        NotificationManager manager;
//        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setTicker(status);
//        builder.setContentTitle(title);
//        builder.setContentText(content);
//        builder.setSmallIcon(R.drawable.ic_launcher);
//        Intent intent = new Intent(this, NotificationHandler.class);
//        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        builder.setContentIntent(pIntent);
//        Notification notification = builder.build();
//        manager.notify(R.string.app_name, notification);
//    }

    public void salvarTeste(String nomeSensor, String idSensor, String timeStampSensor, String valorMsgSensor){
        String filename = nomeArquivoMsgRecebidas;
        count++;
        String string = "\"" + nomeSensor + "\"" + ";" + "\"" + idSensor + "\"" + ";" + "\"" + count + "\"" + ";" + "\"" + timeStampSensor + "\""
                + ";" + "\"" + valorMsgSensor + "\"";
        FileOutputStream outputStream;

        try {
            if (isExternalStorageWritable()){

//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream = new FileOutputStream(arquivoMsgRecebidas, true);
                StringBuffer texto = new StringBuffer();
                texto.append(string + " \n");

                outputStream.write(texto.toString().getBytes());
                outputStream.close();
                //REMOVI para teste
//                    Log.e(TAG, "Salvou dados no arquivo");
            }else {
                Log.e(TAG, "Não montado");
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
        if (lugar.getQualidadeAr() <= limeteQualidadeAr || lugar.getRuido() >= limeteRuido) {
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
