package com.os.workshop.infrastructure.monitoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesSubmittingThreadContextToWorker() throws Exception {
        MDC.put("request_id", "abc-123");
        MDC.put("user", "alice");

        Map<String, String> seen = new HashMap<>();
        Runnable decorated = decorator.decorate(() -> {
            seen.put("request_id", MDC.get("request_id"));
            seen.put("user", MDC.get("user"));
        });

        // Run on a different thread to prove the context is copied, not shared.
        Future<?> task = Executors.newSingleThreadExecutor().submit(decorated);
        task.get();

        assertThat(seen).containsEntry("request_id", "abc-123").containsEntry("user", "alice");
    }

    @Test
    void restoresWorkerContextAfterRun() {
        // Worker already carries context from a previous task on the pool.
        MDC.put("request_id", "previous");
        Runnable decorated = decorator.decorate(() -> MDC.put("request_id", "during"));

        decorated.run();

        // After the task, the worker's prior context is restored (no leak).
        assertThat(MDC.get("request_id")).isEqualTo("previous");
    }

    @Test
    void clearsContextWhenSubmitterHadNone() {
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
        Runnable decorated = decorator.decorate(() ->
                assertThat(MDC.get("request_id")).isNull());

        decorated.run();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
