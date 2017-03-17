package com.opus4j.core.errors;

/**
 * Created by Home on 11/03/2017.
 */
public class OpusNativeException extends Exception {

    public OpusNativeException(ErrorCode errorCode) {
        super(errorCode.getMsg());
    }
}
