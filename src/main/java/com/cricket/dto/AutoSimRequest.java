package com.cricket.dto;

public class AutoSimRequest {
    private boolean enabled;
    private Integer intervalMs;

    public AutoSimRequest() {}

    public AutoSimRequest(boolean enabled, Integer intervalMs) {
        this.enabled = enabled;
        this.intervalMs = intervalMs;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getIntervalMs() { return intervalMs; }
    public void setIntervalMs(Integer intervalMs) { this.intervalMs = intervalMs; }
}
