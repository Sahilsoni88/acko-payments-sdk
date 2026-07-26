package com.acko.payment.sdk.common;

import com.acko.payment.sdk.config.RetrySettings;
import com.acko.payment.sdk.exception.DownstreamException;
import com.acko.payment.sdk.exception.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Retries only on {@link RetryableException} when the call is marked retry-safe.
 */
public final class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);

    private final ExceptionMapper exceptionMapper;

    public RetryExecutor(ExceptionMapper exceptionMapper) {
        this.exceptionMapper = Objects.requireNonNull(exceptionMapper, "exceptionMapper");
    }

    public <T> T execute(RequestContext context, Supplier<T> call) {
        RetrySettings settings = context.getRetrySettings();
        int maxAttempts = (!settings.isEnabled() || !context.isRetrySafe())
                ? 1
                : settings.getMaxAttempts();

        RetryableException lastRetryable = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return call.get();
            } catch (RetryableException retryable) {
                lastRetryable = retryable;
                if (attempt >= maxAttempts) {
                    break;
                }
                log.warn("Retrying operation={} attempt={}/{} reason={}",
                        context.getOperation(), attempt, maxAttempts, retryable.getMessage());
                sleep(settings);
            } catch (RuntimeException runtime) {
                RuntimeException mapped = exceptionMapper.map(runtime);
                if (mapped instanceof RetryableException retryable) {
                    lastRetryable = retryable;
                    if (attempt >= maxAttempts) {
                        break;
                    }
                    log.warn("Retrying operation={} attempt={}/{} reason={}",
                            context.getOperation(), attempt, maxAttempts, retryable.getMessage());
                    sleep(settings);
                    continue;
                }
                throw mapped;
            }
        }
        throw new DownstreamException(
                "Retries exhausted for operation=" + context.getOperation(),
                lastRetryable);
    }

    private static void sleep(RetrySettings settings) {
        try {
            Thread.sleep(settings.getBackoff().toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DownstreamException("Retry interrupted", e);
        }
    }
}
