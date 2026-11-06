package com.santosschein.app.core.config;

public enum Environment {
    MOCK,
    REMOTE;

    public static Environment from(String value) {
        if ("mock".equalsIgnoreCase(value)) {
            return MOCK;
        }
        return REMOTE;
    }
}
