package com.os.workshop.infrastructure.monitoring;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * Copies the submitting thread's MDC (request_id, dd.trace_id, dd.span_id, user)
 * onto @Async worker threads so background logs keep the same correlation as the
 * originating request. MDC is thread-local, so without this the context is lost
 * the moment work hops to a pool thread.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            setContext(captured);
            try {
                runnable.run();
            } finally {
                // Restore the worker thread's prior context so pooled threads
                // never leak correlation ids between tasks.
                setContext(previous);
            }
        };
    }

    private static void setContext(Map<String, String> context) {
        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }
}
