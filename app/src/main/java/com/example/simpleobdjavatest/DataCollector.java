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


    @Override
    public void onNext(Reply<?> reply) {
        Log.e("Receive data: {}", Arrays.toString(reply.getCommand().getData()));

        ObdMetric obdMetric = (ObdMetric) reply;
        Log.e("DataCollector", String.valueOf(obdMetric));
        // Display data
        // (defined from this file https://github.com/tzebrowski/ObdMetrics/blob/main/src/main/resources/mode01.json )
        // Calculated Engine Load
        if (obdMetric.getCommand().getPid().getId() == 5) {
            Log.e("DataCollector", "Calculated Engine Load: " + obdMetric.getValue());

            // Coolant
        } else if (obdMetric.getCommand().getPid().getId() == 6) {
            Log.e("DataCollector", "coolant: " + obdMetric.getValue());

            // Engine Speed
        } else if (obdMetric.getCommand().getPid().getId() == 13) {
            double rpm = obdMetric.getValue().doubleValue();
            Log.e("DataCollector", "Vehicle RPM: " + rpm);

            // Speed
        } else if (obdMetric.getCommand().getPid().getId() == 14) {
            Log.e("DataCollector", "Vehicle Speed: " + obdMetric.getValue());

            // Control Module Voltage
        } else if (obdMetric.getCommand().getPid().getId() == 67) {
            Log.e("DataCollector", "Control Module Voltage: " + obdMetric.getValue());
        }

        data.put(reply.getCommand(), reply);

//        if (reply instanceof ObdMetric) {
//            metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
//        }

    }
}