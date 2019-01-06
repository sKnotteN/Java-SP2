package knotten.kristian.computercontroll;

// Importer pakkar som blir brukt
import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ControlPC extends AppCompatActivity {

    // Variablar til og kontrollere bevegelse av musa
    Integer lastX = 0;
    Integer lastY = 0;
    String msg;
    Socket serverSocket;

    // Variabel med referanse til server
    public DataOutputStream oStream;

    // Variabel med referanse til toast som kan gi beskjed til brukar
    Toast messageToast;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_pc);

        // Lag ein toast som skal bli brukt til og gi beskjedar til brukaren
        messageToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Få referanse til serveren frå ConnectToServer klassa
        ConnectToServer connectToServer = new ConnectToServer();
        oStream = connectToServer.getoStream();
        serverSocket = connectToServer.getClientSocket();

        // Få referanse til knappane og viewen i GUI
        Button leftClickButton = findViewById(R.id.leftButton);
        Button rightClickButton = findViewById(R.id.rightButton);
        View mouseView = findViewById(R.id.mouseView);

        // Sjekker om brukaren har trykt på høgre museklikk knappen
        rightClickButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){

                    // Seier frå at høgre museklikk er trykt ned
                    case MotionEvent.ACTION_DOWN:
                        sendMessage("rightdown");
                        break;

                    // Seier frå at høgre museklikk ikkje lenger er trykt ned
                    case MotionEvent.ACTION_UP:
                        sendMessage("rightup");
                }
                return false;
            }
        });

        // Sjekker om brukaren har trykt på venstre museklikk knappen
        leftClickButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){

                    // Seier frå at venstre museklikk er trykt ned
                    case MotionEvent.ACTION_DOWN:
                        sendMessage("leftdown");
                        break;

                    // Seier frå at venstre museklikk ikkje lenger er trykt ned
                    case MotionEvent.ACTION_UP:
                        sendMessage("leftup");
                        break;
                }
                return false;
            }
        });

        // Sjekker om brukaren trykker på den delen av skjermen som skal styre musa
        mouseView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    controlEvents(event);
                    break;

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        lastX = (int)event.getX();
                        lastY = (int)event.getY();
                        break;
                }

                return true;
            }
        });

    }

    // Metode som skal kontrollere kva som skjer når brukaren prøver og flytte på musa
    public void controlEvents(MotionEvent event){

        int newX = (int)event.getX();
        int newY = (int)event.getY();

        // Finn ut kor mykje fingeren har flytte seg, gong det med 2 for og få meir fart
        // og flipp verdiane for og match x og y på pcen
        Integer rel_x = ((lastX - newX) * 2) * -1;
        Integer rel_y = ((lastY - newY) * 2) * -1;

        System.out.println("Test lastx: " + lastX + "  lasty: " + lastY);
        System.out.println("Test newx: " + newX + "  newy: " + newY);
        System.out.println("Test rel x: " + rel_x + "  rel y: " + rel_y);

        lastX = newX;
        lastY = newY;

        // Sjekk etter unaturlig store bevegelsar og stopp dei frå og skje
        if ((rel_x > 200 || rel_y > 200) || (rel_x < -200 || rel_y < -200))
            return;

        // Sjekk at fingeren har bevegt seg
        if (rel_y != 0 && rel_x != 0){
            System.out.println("rel x: " + rel_x + " rel y: " + rel_y);

            // Sjekk om det er ein finger på musa
            if(event.getPointerCount() == 1){
                msg = "; " + rel_x + ", " + rel_y;
            }

            // Sjekk om der er to fingrar på musa
            else if(event.getPointerCount() == 2){
                msg = "; " + rel_y * 2 + ", scroll";
            }

            sendMessage(msg);
        }

    }

    // Lukke tilkoplinga
    public void closeConnection(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sender beskjed til serveren
    public void sendMessage(final String message){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    oStream.writeUTF(message);
                } catch (IOException e) {
                    disconnected();
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    // Lukker og tilkoplinga og aktiviteten
    public void disconnected(){
        closeConnection();

        // Passer på at det er hovudtråden som prøver og lukke aktiviteten
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageToast.setText("Disconnected from server");
                messageToast.show();
                finish();
            }
        });
    }

    // Legg til at socketen skal bli disconnected når ein trykker på tilbake knappen

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnected();
    }
}
