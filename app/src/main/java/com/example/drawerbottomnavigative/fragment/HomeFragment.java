package com.example.drawerbottomnavigative.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.widget.Switch;
import android.widget.TextView;
import com.example.drawerbottomnavigative.R;
import com.example.drawerbottomnavigative.MqttHandler;
import org.apache.commons.lang3.math.NumberUtils;
public class HomeFragment extends Fragment {
    private MqttHandler mqttHandler;

    private TextView phTextView;
    private TextView temperatureTextView;
    private TextView turbidityTextView;
    private TextView statusTextView;



    private static final String CLIENT_ID = "vovilak10202002";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        temperatureTextView = view.findViewById(R.id.temperature); // Replace with the actual ID of your Switch widget
        turbidityTextView   = view.findViewById(R.id.Turbidity);
        statusTextView=view.findViewById(R.id.status);
        phTextView = view.findViewById(R.id.PH);

        return view;
    }
   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

          mqttHandler = new MqttHandler();

        // Connect to HiveMQ
        mqttHandler.connectToHiveMQ(CLIENT_ID);

        // Subscribe to MQTT topics and update switches when messages are received
        subscribe();

    }
    private void subscribe() {
        mqttHandler.subscribe("collection-station", new MqttHandler.MessageCallback() {
        @Override
        public void onMessageReceived(String receivedTopic, String message) {
            if ("collection-station".equals(receivedTopic)) {
                String Mess = message;
                Log.d("MQTT_MESSAGE", "Received message: " + Mess);

                String[] values = Mess.split(",");
                if (values.length >= 4) {
                    double ph = NumberUtils.isCreatable(values[0]) ? Double.parseDouble(values[0]) : Double.NaN;
                    double temperature = NumberUtils.isCreatable(values[1]) ? Double.parseDouble(values[1]) : Double.NaN;
                    double turbidity = NumberUtils.isCreatable(values[2]) ? Double.parseDouble(values[2]) : Double.NaN;
                    String status = values[3].equals("Stable") ? "Stable" : values[3].equals("Unstable") ? "Unstable" : "N/A";

                    // Log the variables
                    Log.d("TAG1", "pH: " + ph);
                    Log.d("TAG2", "Temperature: " + temperature);
                    Log.d("TAG3", "Turbidity: " + turbidity);
                    Log.d("TAG4", "Status: " + status);

                    phTextView.setText("" + ph);
                    temperatureTextView.setText(temperature+" °C" );
                    turbidityTextView.setText( turbidity+" NTU");
                    statusTextView.setText("" + status);

                    // Do whatever processing or display you need with the variables
                } else {
                    Log.e("TAG_ERROR", "Invalid message format: " + Mess);
                }
            }
        }


            @Override
            public void onConnectionLost(Throwable cause) {
            }
             });
    }












}
