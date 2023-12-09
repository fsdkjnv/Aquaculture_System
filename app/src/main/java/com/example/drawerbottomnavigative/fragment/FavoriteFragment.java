package com.example.drawerbottomnavigative.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.drawerbottomnavigative.MainActivity;
import com.example.drawerbottomnavigative.MqttHandler;
import com.example.drawerbottomnavigative.R;

public class FavoriteFragment extends Fragment {

    private static final String CLIENT_ID = "vovilak10202002";
    private static final String OXYGEN_PUMP_TOPIC = "control-oxygen-pump";
    private static final String  WATER_PUMP_TOPIC = "control-water-pump";
     private static final String RESTORE_TOPIC = "control-restore";
    private MqttHandler mqttHandler;
    private Switch switchOxy;
    private Switch switchWater;

    // Flags to prevent recursive calls during switch state changes
    private boolean oxySwitchChanging;
    private boolean waterSwitchChanging;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        switchOxy = view.findViewById(R.id.switchoxy);
        switchWater = view.findViewById(R.id.switchwater);

        // Set up listeners for user-initiated changes to the switches
        setupSwitchListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mqttHandler = new MqttHandler();
        mqttHandler.connectToHiveMQ(CLIENT_ID);

        // Subscribe to MQTT topics
        subscribeToSwitch(WATER_PUMP_TOPIC);
        subscribeToSwitch(OXYGEN_PUMP_TOPIC);
        publishMessage(RESTORE_TOPIC, "ENABLE");

    }

    @Override
    public void onDestroyView() {
        mqttHandler.disconnect();
        super.onDestroyView();
    }

    private void setupSwitchListeners() {
        // Set up listener for the Oxygen Pump switch
        publishMessageOnClick(switchOxy, OXYGEN_PUMP_TOPIC);

        // Set up listener for the Water Pump switch
        publishMessageOnClick(switchWater, WATER_PUMP_TOPIC);
    }

private void publishMessageOnClick(Switch aSwitch, String topic) {
    aSwitch.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isChecked = ((Switch) view).isChecked();

            if (!oxySwitchChanging && !waterSwitchChanging) {
                // Only publish if the change is user-initiated
                String message = isChecked ? "ON" : "OFF";
                publishMessage(topic, message);
            }
        }
    });
}


    private void subscribeToSwitch(String topic) {
        mqttHandler.subscribe(topic, new MqttHandler.MessageCallback() {
            @Override
            public void onMessageReceived(String receivedTopic, String message) {
                boolean isChecked = "ON".equals(message);
                switchChanging(receivedTopic, isChecked);
            }

            @Override
            public void onConnectionLost(Throwable cause) {
                // Handle connection lost event
            }
        });
    }

    private void switchChanging(String receivedTopic, boolean isChecked) {
        if (WATER_PUMP_TOPIC.equals(receivedTopic)) {
            waterSwitchChanging = true;
            requireActivity().runOnUiThread(() -> switchWater.setChecked(isChecked));
            waterSwitchChanging = false;
        } else if (OXYGEN_PUMP_TOPIC.equals(receivedTopic)) {
            oxySwitchChanging = true;
            requireActivity().runOnUiThread(() -> switchOxy.setChecked(isChecked));
            oxySwitchChanging = false;
        }
    }

    private void publishMessage(String topic, String message) {
        Toast.makeText(requireContext(), "Publishing message: " + message, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic, message);
    }
}
