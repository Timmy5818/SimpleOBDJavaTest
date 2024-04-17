package com.example.simpleobdjavatest;

import android.util.Log;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import java.util.Arrays;
import java.util.List;

public final class DataCollector extends ReplyObserver<Reply<?>> {

    private final MultiValuedMap<Command, Reply<?>> data = new ArrayListValuedHashMap<Command, Reply<?>>();

    private final MultiValuedMap<PidDefinition, ObdMetric> metrics = new ArrayListValuedHashMap<PidDefinition, ObdMetric>();

//    private val metricsProcessors = mutableSetOf<MetricsProcessor>();

    public ObdMetric findSingleMetricBy(PidDefinition pidDefinition) {
        List<ObdMetric> list = (List<ObdMetric>) metrics.get(pidDefinition);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<ObdMetric> findMetricsBy(PidDefinition pidDefinition) {
        return (List<ObdMetric>) metrics.get(pidDefinition);
    }

//    @Override
//    public void onStopped() {
//        metrics.postValue(null);
//        metricsProcessors.forEach { it.onStopped() };
//    }
//
//    @Override
//    public void onRunning(vehicleCapabilities:VehicleCapabilities?) {
//        metricsProcessors.forEach { it.onRunning(vehicleCapabilities) }
//    }

    @Override
    public void onNext(Reply<?> reply) {
        Log.e("Receive data: {}", Arrays.toString(reply.getCommand().getData()));

        Log.e("Receive data: {}", reply.toString());

        Log.e("ObdMetricData", "Get Label:" +reply.getCommand().getLabel());
        data.put(reply.getCommand(), reply);

//        if (reply instanceof ObdMetric) {
//            metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
//        }

        if (reply instanceof ObdMetric) {
            Log.e("ObdMetricData","測試顯示if內容");
            ObdMetric obdMetric = (ObdMetric) reply;
            // 取得PID定义，这可能包括PID的ID、名称等
            PidDefinition pidDefinition = obdMetric.getCommand().getPid();

            if (pidDefinition != null) {
                Log.e("ObdMetricData","測試顯示if內容2");
                // 構建一個日志消息，展示PID的一些关键信息和对应的度量值
                String logMessage = String.format("PID: %s, Name: %s, Value: %s",
                        pidDefinition.getId(),
                        pidDefinition.getDescription(),
                        obdMetric.getValue());

                // 使用Log.e印出錯誤
                Log.e("ObdMetricData", "顯示PID資訊:" + logMessage);

                // 如果有其他處理器需要處理這些度量值，可以在這邊以for迴圈尋找
//                for (MetricsProcessor processor : metricsProcessors) {
//                    try {
//                        processor.processMetric(obdMetric);
//                    } catch (Exception e) {
//                        Log.e("MetricsObserver", "Failed to process metric", e);
//                    }
//                }
            }
        }

        if (reply instanceof ObdMetric) {
            ObdMetric obdMetric = (ObdMetric) reply;
            Log.e("DataCollector", String.valueOf(obdMetric));
            // Display data
            // (defined from this file https://github.com/tzebrowski/ObdMetrics/blob/main/src/main/resources/mode01.json )
            // Calculated Engine Load
            if (obdMetric.getCommand().getPid().getId() == 5L) {
                Log.e("DataCollector", "Calculated Engine Load: " + obdMetric.getValue());

                // Coolant
            } else if (obdMetric.getCommand().getPid().getId() == 6L) {
                Log.e("DataCollector", "coolant: " + obdMetric.getValue());

                // Engine Speed
            } else if (obdMetric.getCommand().getPid().getId() == 13L) {
                double rpm = obdMetric.getValue().doubleValue();
                Log.e("DataCollector", "Vehicle RPM: " + rpm);

                // Speed
            } else if (obdMetric.getCommand().getPid().getId() == 14) {
                Log.e("DataCollector", "Vehicle Speed: " + obdMetric.getValue());

                // Control Module Voltage
            } else if (obdMetric.getCommand().getPid().getId() == 67) {
                Log.e("DataCollector", "Control Module Voltage: " + obdMetric.getValue());
            }


            metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
        }

    }
}