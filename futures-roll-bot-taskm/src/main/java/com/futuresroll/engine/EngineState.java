package com.futuresroll.engine;

/**
 * 引擎状态
 */
public enum EngineState {
    IDLE("idle"),
    CHECKING("checking"),
    ROLLING("rolling"),
    COMPLETED("completed"),
    ERROR("error"),
    STOPPED("stopped");
    
    private final String value;
    
    EngineState(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
