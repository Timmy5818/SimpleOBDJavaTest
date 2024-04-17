package com.example.simpleobdjavatest;

import org.obd.metrics.alert.Alerts;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowExecutionStatus;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.NonNull;

public interface WorkflowFinalizer {

    /**
     * Execute routine for already running workflow
     *
     * @param query       queried PID's (parameter is mandatory)
     * @param init        init settings of the Adapter
     */
    WorkflowExecutionStatus executeRoutine(@NonNull Query query, @NonNull Init init);

    /**
     * Updates query for already running workflow
     *
     * @param query       queried PID's (parameter is mandatory)
     * @param init        init settings of the Adapter
     * @param adjustments additional settings for process of collection the data
     */
    WorkflowExecutionStatus updateQuery(@NonNull Query query, @NonNull Init init, @NonNull Adjustments adjustments);

    /**
     * It starts the process of collecting the OBD metrics
     *
     * @param connection the connection to the Adapter (parameter is mandatory)
     * @param query      queried PID's (parameter is mandatory)
     */
    default WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query) {
        return start(connection, query, Init.DEFAULT, Adjustments.DEFAULT);
    }

    /**
     * It starts the process of collecting the OBD metrics
     *
     * @param connection  the connection to the Adapter (parameter is mandatory)
     * @param query       queried PID's (parameter is mandatory)
     * @param adjustments additional settings for process of collection the data
     */
    default WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query,
                                          Adjustments adjustments) {
        return start(connection, query, Init.DEFAULT, adjustments);
    }

    /**
     * It starts the process of collecting the OBD metrics
     *
     * @param adjustements additional settings for process of collection the data
     * @param connection   the connection to the Adapter (parameter is mandatory)
     * @param query        queried PID's (parameter is mandatory)
     * @param init         init settings of the Adapter
     */
    WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
                                  Adjustments adjustements);

    /**
     * Stops the current workflow.
     */
    default void stop() {
        stop(true);
    }

    /**
     * Stops the current workflow.
     *
     * @param gracefulStop indicates whether workflow should be gracefully stopped.
     */
    void stop(boolean gracefulStop);

    /**
     * Informs whether {@link Workflow} process is already running.
     *
     * @return true when process is already running.
     */
    boolean isRunning();

    /**
     * Rebuild {@link PidDefinitionRegistry} with new resources
     *
     * @param pids new resources
     */
    void updatePidRegistry(Pids pids);

    /**
     * Gets the current pid registry for the workflow.
     *
     * @return instance of {@link PidDefinitionRegistry}
     */
    PidDefinitionRegistry getPidRegistry();

    /**
     * Gets diagnostics collected during the session.
     *
     * @return instance of {@link Diagnostics}
     */
    Diagnostics getDiagnostics();

    /**
     * Gets allerts collected during the session.
     *
     * @return instance of {@link Alerts}
     */
    Alerts getAlerts();

//    /**
//     * It creates default {@link Workflow} implementation.
//     *
//     * @param pids                   PID's configuration
//     * @param formulaEvaluatorPolicy the instance of {@link FormulaEvaluatorPolicy}.
//     *                               Might be null.
//     * @param observer               the instance of {@link ReplyObserver}
//     * @param lifecycleList          the instance of {@link Lifecycle}
//     * @return instance of {@link Workflow}
//     */
//    @Builder(builderMethodName = "instance", buildMethodName = "initialize")
//    static Workflow newInstance(Pids pids, FormulaEvaluatorPolicy formulaEvaluatorPolicy,
//                                @NonNull ReplyObserver<Reply<?>> observer, @Singular("lifecycle") List<Lifecycle> lifecycleList) {
//
//        return new DefaultWorkflow(pids, formulaEvaluatorPolicy, observer, lifecycleList);
//    }

//    int DEFAULT_FINALIZE_TIME = 500;
//
//    static void finalizeAfter(final Workflow workflow, long sleepTime) throws InterruptedException {
//        final Callable<String> end = () -> {
//            TimeUnit.MILLISECONDS.sleep(sleepTime);
//            workflow.stop();
//            return "end";
//        };
//
//        final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
//        newFixedThreadPool.invokeAll(Arrays.asList(end));
//        newFixedThreadPool.shutdown();
//    }
//
//    static void finalizeAfter500ms(final Workflow workflow) throws InterruptedException {
//        finalizeAfter(workflow, DEFAULT_FINALIZE_TIME);
//    }
}