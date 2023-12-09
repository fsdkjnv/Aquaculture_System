package com.example.drawerbottomnavigative.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.drawerbottomnavigative.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.example.drawerbottomnavigative.MqttHandler;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
public class SlideshowFragment extends Fragment {

    private LineChart lineChart1;
    private LineChart lineChart2;
    private LineChart lineChart3;
    private MqttHandler mqttHandler;
    private static final String CLIENT_ID = "vovilak10202002";
    private double ph = Double.NaN;
    private double temperature = Double.NaN;
    private double turbidity = Double.NaN;

    private float[] temperatureValues = new float[100];  // Adjust the size based on your needs
    private float[] pondusHydrogeniiValues = new float[100];
    private float[] turbidityValues = new float[100];
    private String[] timestamps = new String[100];  // Adjust the size based on your needs
    private int index = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sildeshow, container, false);
        // Initialize mqttHandler and connect to HiveMQ
        mqttHandler = new MqttHandler();
        mqttHandler.connectToHiveMQ(CLIENT_ID);
        // Subscribe to MQTT topics and update switches when messages are received
        subscribe();
        // Initialize LineCharts
        lineChart1 = view.findViewById(R.id.chartViewTemp1);
        lineChart2 = view.findViewById(R.id.chartViewTemp2);
        lineChart3 = view.findViewById(R.id.chartViewTemp3);

        // Customize the appearance of the LineCharts
        customizeLineChart(lineChart1, "Time", "Value");
        customizeLineChart(lineChart2, "Time", "Value");
        customizeLineChart(lineChart3, "Time", "Value");

        return view;
    }

    private void customizeLineChart(LineChart chart, String xAxisLabel, String yAxisLabel) {
        // Hide right YAxis
        chart.getAxisRight().setEnabled(false);

        // Customize XAxis (x-axis labels)
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timestamps)); // Use timestamps as X-axis labels

        // Customize YAxis (y-axis labels)
        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setTextColor(ColorTemplate.getHoloBlue());

        // Customize Legend (dataset labels)
        Legend legend = chart.getLegend();
        legend.setTextColor(ColorTemplate.getHoloBlue());
    }

    private void subscribe() {
        mqttHandler.subscribe("collection-station", new MqttHandler.MessageCallback() {
    @Override
    public void onMessageReceived(String receivedTopic, String message) {
        if ("collection-station".equals(receivedTopic)) {
            if (message != null) {
                String Mess = message;

                String[] values = Mess.split(",");
                if (values.length >= 4) {
                    ph = NumberUtils.isCreatable(values[0]) ? Double.parseDouble(values[0]) : Double.NaN;
                    temperature = NumberUtils.isCreatable(values[1]) ? Double.parseDouble(values[1]) : Double.NaN;
                    turbidity = NumberUtils.isCreatable(values[2]) ? Double.parseDouble(values[2]) : Double.NaN;
                    long currentTime = System.currentTimeMillis();

                    // Log timestamp
                    logTimestamp(currentTime);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    String formattedDate = sdf.format(new Date(currentTime));
                    Log.d("TAG_TIME", "Timestamp: " + formattedDate);
                    Log.d("TAG1  " + formattedDate, "pH: " + ph);
                    Log.d("TAG2", "Temperature: " + temperature);
                    Log.d("TAG3", "Turbidity: " + turbidity);
                    // Update chart entries with new data
                    addChartData(currentTime, temperature, ph, turbidity);

                    // Notify charts to update
                    updateCharts();
                }
            }
        }
    }

            @Override
            public void onConnectionLost(Throwable cause) {
                // Handle the event of a lost connection
            }

        });
    }

    private void logTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formattedDate = sdf.format(new Date(timestamp));
        Log.d("TAG_TIME", "Timestamp: " + formattedDate);
    }

    private void addChartData(long timestamp, double temperature, double ph, double turbidity) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formattedDate = sdf.format(new Date(timestamp));

        temperatureValues[index % 5] = (float) temperature;
        pondusHydrogeniiValues[index % 5] = (float) ph;
        turbidityValues[index % 5] = (float) turbidity;
        timestamps[index % 5] = formattedDate;

        index++;

        // Update chart data with the new LineDataSets
        updateCharts();
    }

    private void updateCharts() {
        // Update the LineDataSets with the new entries
        LineDataSet dataSet1 = new LineDataSet(getEntries(temperatureValues), "Temperature");
        LineDataSet dataSet2 = new LineDataSet(getEntries(pondusHydrogeniiValues), "Pondus Hydrogenii");
        LineDataSet dataSet3 = new LineDataSet(getEntries(turbidityValues), "Turbidity of Water");

        // Create LineData objects
        LineData lineData1 = new LineData(dataSet1);
        LineData lineData2 = new LineData(dataSet2);
        LineData lineData3 = new LineData(dataSet3);

        // Set data for each chart
        lineChart1.setData(lineData1);
        lineChart2.setData(lineData2);
        lineChart3.setData(lineData3);

        // Notify each chart to update its data
        lineChart1.invalidate();
        lineChart2.invalidate();
        lineChart3.invalidate();
    }

    private List<Entry> getEntries(float[] values) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            entries.add(new Entry(i, values[i]));
        }
        return entries;
    }
}
