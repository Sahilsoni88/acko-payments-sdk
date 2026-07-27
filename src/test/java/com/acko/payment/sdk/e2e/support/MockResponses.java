package com.acko.payment.sdk.e2e.support;

public final class MockResponses {

    private MockResponses() {
    }

    public static ScriptedResponse token() {
        return ok("""
                {"access_token":"test-token","token_type":"Bearer","expires_in":3600}
                """);
    }

    public static ScriptedResponse ok(String body) {
        return json(200, body);
    }

    public static ScriptedResponse json(int status, String body) {
        return new ScriptedResponse(status, body, 0);
    }

    public static ScriptedResponse delayedJson(int status, long delayMs, String body) {
        return new ScriptedResponse(status, body, delayMs);
    }
}
