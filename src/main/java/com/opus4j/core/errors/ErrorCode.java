package com.opus4j.core.errors;

import lombok.Data;
import lombok.Getter;

/**
 * Error codes used in native library.
 * Mapped here to exceptions.
 */
public enum ErrorCode {
    OPUS_OK (0, "Opus OK"),
    OPUS_BAD_ARG(-1, "Bad Argument"),
    OPUS_BUFFER_TOO_SMALL (-2, "The buffer given is too small"),
    OPUS_INTERNAL_ERROR (-3, "Opus Native Internal Error"),
    OPUS_INVALID_PACKET (-4, "Invalid Packet"),
    OPUS_UNIMPLEMENTED (-5, "Unimplemented Error"),
    OPUS_INVALID_STATE (-6, "Opus Native State is invalid"),
    OPUS_ALLOC_FAILED (-7, "Could not allocate memory");

    /** The native error number */
    private final int errorNum;

    /** The message to use for exceptions */
    private final String msg;

    ErrorCode(int errorNum, String msg) {
        this.errorNum = errorNum;
        this.msg = msg;
    }

    /**
     * Get the message for exceptions
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Get native error number
     * @return the native error number for the given error code
     */
    public int getErrorNum() {
        return errorNum;
    }

    public static ErrorCode fromErrorNum(int errorNum) {
        switch (errorNum) {
            case 0:
                return OPUS_OK;
            case -1:
                return OPUS_BAD_ARG;
            case -2:
                return OPUS_BUFFER_TOO_SMALL;
            case -3:
                return OPUS_INTERNAL_ERROR;
            case -4:
                return OPUS_INVALID_PACKET;
            case -5:
                return OPUS_UNIMPLEMENTED;
            case -6:
                return OPUS_INVALID_STATE;
            case -7:
                return OPUS_ALLOC_FAILED;
            default:
                throw new IllegalArgumentException("No Such Error!");
        }
    }
}