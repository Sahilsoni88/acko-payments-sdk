package com.acko.payment.sdk.common;

import com.acko.payment.sdk.auth.OAuthToken;
import com.acko.payment.sdk.auth.TokenManager;
import com.acko.payment.sdk.exception.PaymentException;
import com.acko.payment.sdk.util.CorrelationIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Full request lifecycle: token → ThreadLocal → retry → clear → metrics/logging.
 */
public class RequestExecutor {

    private static final Logger log = LoggerFactory.getLogger(RequestExecutor.class);

    private final TokenManager tokenManager;
    private final RetryExecutor retryExecutor;
    private final ExceptionMapper exceptionMapper;
    private final MetricsHook metricsHook;

    public RequestExecutor(
            TokenManager tokenManager,
            RetryExecutor retryExecutor,
            ExceptionMapper exceptionMapper,
            MetricsHook metricsHook) {
        this.tokenManager = Objects.requireNonNull(tokenManager, "tokenManager");
        this.retryExecutor = Objects.requireNonNull(retryExecutor, "retryExecutor");
        this.exceptionMapper = Objects.requireNonNull(exceptionMapper, "exceptionMapper");
        this.metricsHook = metricsHook == null ? MetricsHook.NOOP : metricsHook;
    }

    public <T> T execute(RequestContext context, Supplier<T> feignCall) {
        String correlationId = context.getCorrelationId() != null
                ? context.getCorrelationId()
                : CorrelationIdHolder.getOrCreate();
        CorrelationIdHolder.set(correlationId);

        long start = System.currentTimeMillis();
        try {
            OAuthToken token = tokenManager.getValidToken();
            AccessTokenHolder.set(token.authorizationHeader());

            T result = retryExecutor.execute(context, () -> {
                try {
                    return feignCall.get();
                } catch (RuntimeException e) {
                    throw exceptionMapper.map(e);
                }
            });

            long latency = System.currentTimeMillis() - start;
            metricsHook.onSuccess(context.getOperation(), latency);
            log.info("operation={} status=SUCCESS correlationId={} latencyMs={}",
                    context.getOperation(), correlationId, latency);
            return result;
        } catch (PaymentException e) {
            long latency = System.currentTimeMillis() - start;
            metricsHook.onFailure(context.getOperation(), latency, e.getErrorCode());
            log.error("operation={} status=ERROR correlationId={} latencyMs={} errorCode={} message={}",
                    context.getOperation(), correlationId, latency, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            RuntimeException mapped = exceptionMapper.map(e);
            long latency = System.currentTimeMillis() - start;
            String code = mapped instanceof PaymentException pe ? pe.getErrorCode() : "UNKNOWN";
            metricsHook.onFailure(context.getOperation(), latency, code);
            log.error("operation={} status=ERROR correlationId={} latencyMs={} errorCode={} message={}",
                    context.getOperation(), correlationId, latency, code, mapped.getMessage());
            throw mapped;
        } finally {
            AccessTokenHolder.clear();
            CorrelationIdHolder.clear();
        }
    }
}
