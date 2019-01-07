package knotten.kristian.computercontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.os.Handler;
import android.content.Intent;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ConnectToServer extends AppCompatActivity {

    // Få referanse til input til IP adressa og Port
    EditText serverIPInput;
    EditText serverPortInput;

    // Toast variabel til og sende beskjedar til brukaren
    Toast messageToast;



    // Lage til socket for tilkopling til serveren
    public static Socket clientSocket;
    public static DataOutputStream oStream;
    boolean connected = false;

    // Handler variabel som kalla etter klienten er ferdig og (prøve) kople til
    Handler connectHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_server);

        // Hent referansen til IP og port input
        serverIPInput = findViewById(R.id.serverIPInput);
        serverPortInput = findViewById(R.id.serverPortInput);

        // Prøv og hent ut tidligare brukte IP adresse og port
        try{
            LoadInfo(serverIPInput, serverPortInput);
        }catch (Exception e){}

        // Lag ein toast som skal bli brukt til og gi brukaren beskjeder
        messageToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        messageToast.setGravity(Gravity.TOP, 0, 300);
    }

    //Sjekk om IP adressa og porten er ein valid input
    public void checkIP(View view){
        List<Integer> ipCheck = new ArrayList<>();

        try {
            String ip = serverIPInput.getText().toString();
            String[] split_ip = ip.split(Pattern.quote("."));

            for (String num : split_ip) {
                ipCheck.add(Integer.parseInt(num));
            }
        }
        catch (Exception e){
            messageToast.setText("Something went wrong");
            messageToast.show();
        }

        if (ipCheck.size() != 4){
            messageToast.setText("The IP is the wrong size");
            messageToast.show();
        }

        else {
            boolean check = true;

            // Sjekk om porten ikkje er mellom 10000 og 60000
            int port = Integer.parseInt(serverPortInput.getText().toString());
            if (port <= 10000 || port >= 60000) {
                messageToast.setText("Port must be between 10000 and 60000");
                messageToast.show();

                check = false;
            }

            // Gå igjennom IP adressa og sjekk om nummera ikkje er mellom 0 og 254
            else {
                for (Integer num : ipCheck) {
                    if (num <= 0 || num >= 254) {
                        messageToast.setText("Your IP is not valid");
                        messageToast.show();
                        check = false;
                        break;
                    }
                }
            }

            // Vist alt av sjekkar stemmer, prøv og kople til serveren
            if (check) {
                connect(serverIPInput.getText().toString(), port);
            }
        }
    }


    // Metode til og prøve kople til serveren
    public void connect(final String ip, final Integer port) {
        messageToast.setText("Trying to connect..");
        messageToast.show();

        Runnable run_connect = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    // Prøv og sette opp socket mellom server og klient
                    try {
                        setClientSocket(new Socket(ip, port));
                        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        setoStream(dos);
                        connected = true;
                    }
                    catch (Exception e) {
                        connected = false;
                    }
                    finally {
                        // Utfører isConnected i en handler etter at klienten er ferdig og kople til
                        connectHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                isConnected();
                            }
                        });

                        // Lagre informasjonen som blei lagt inn til neste gang
                        saveInfo(serverIPInput, serverPortInput);
                    }
                }
            }
        };

        Thread connectThread = new Thread(run_connect);
        connectThread.start();
    }

    // Sjekk om brukaren klarte og kople til serveren
    public void isConnected(){
        if (connected) {
            messageToast.setText("Connected");
            messageToast.show();

            // Om brukaren klarte og kople til so start opp ControlPC klassa
            Intent controlPCIntent = new Intent(this, ControlPC.class);
            startActivity(controlPCIntent);
        }
        if (!connected) {
            messageToast.setText("Was not able to connect");
            messageToast.show();
        }
    }

    // Til og sette og gette oStream og clientSocket variabelen
    public void setoStream(DataOutputStream oStream) {
        this.oStream = oStream;
    }

    // Til og hente ut oStream variabelen
    public DataOutputStream getoStream() {
        return oStream;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public static void setClientSocket(Socket clientSocket) {
        ConnectToServer.clientSocket = clientSocket;
    }

    // Metode som lagerer IP adressa og porten som blei brukt til og kople til serveren
    public void saveInfo(EditText ip, EditText port){
        SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("serverIP", ip.getText().toString());
        editor.putString("serverPort", port.getText().toString());
        editor.apply();
        System.out.println("ip saved: " + ip.getText().toString());
    }

    // Metode som henter ut IP og porten som sist blei brukt
    public void LoadInfo(EditText ip, EditText port){
        SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE);

        String sIP = sharedPreferences.getString("serverIP", "");
        String sPort =  sharedPreferences.getString("serverPort", "");
        ip.setText(sIP);
        port.setText(sPort);
    }

}
