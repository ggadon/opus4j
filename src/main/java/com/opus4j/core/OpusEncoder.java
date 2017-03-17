package com.opus4j.core;

import com.opus4j.core.errors.ErrorCode;
import com.opus4j.core.errors.OpusNativeException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * An opus encoder class that holds the state of the opus encoder and gives an option to initalize, encode
 * and free encoders.
 *
 * Don't forget to free your encoder at the end of usage in order to avoid memory leaks.
 *
 * Created by ggadon on 16/03/2017.
 */
public class OpusEncoder {

    /** The state of the encoder */
    private NativeMappings.OpusEncoder state;

    /** The output buffer to use when encoding */
    private ByteBuffer outputBuffer;

    /**
     * Internal c'tor
     * @param state the state to use while encoding
     */
    OpusEncoder(NativeMappings.OpusEncoder state, ByteBuffer outputBuffer) {
        this.state = state;
        this.outputBuffer = outputBuffer;
    }

    /**
     * Encode a frame.
     * @see NativeMappings#opus_encode(NativeMappings.OpusEncoder, ShortBuffer, int, ByteBuffer, int)
     * @return the output buffer, Copied for further usage.
     * @throws OpusNativeException in case of native error while encoding.
     */
    public ByteBuffer encode(ShortBuffer toEncode, int samplesPerChannel) throws OpusNativeException {
        int encodedFrames =
                NativeMappings.opus_encode(state, toEncode, samplesPerChannel, outputBuffer, outputBuffer.capacity());

        if (encodedFrames < 0) {
            throw new OpusNativeException(ErrorCode.fromErrorNum(encodedFrames));
        }

        try {
            byte[] toReturn = new byte[encodedFrames];
            outputBuffer.get(toReturn);

            return ByteBuffer.wrap(toReturn);
        } finally {
            outputBuffer.position(0);
        }
    }

    /**
     * Destroy the native state.
     * MUST be called in order to avoid memory leaks.
     */
    public void destroy() {
        NativeMappings.opus_encoder_destroy(state);
    }

    /**
     * Creates a new encoder.
     * @see NativeMappings#opus_encoder_create(int, int, int, IntBuffer)
     * @param maxEncodedFrameSize The maximum size of encoded frame to use for a given packet.
     *                            Used for allocating buffers in advance. Might impose on the output bitrate (an upper
     *                            limit for it)
     * @return a newly created Opus Encoder state.
     * @throws OpusNativeException in case of internal error while trying to create the encoder
     */
    public static OpusEncoder create(int sampleRate, int channels,
                                     NativeMappings.EncodingApplication application,
                                     int maxEncodedFrameSize) throws OpusNativeException {
        IntBuffer error = IntBuffer.allocate(1);
        NativeMappings.OpusEncoder encoder = NativeMappings.opus_encoder_create(sampleRate, channels,
                application.getValue(), error);

        ErrorCode errorCode = ErrorCode.fromErrorNum(error.get());
        if (errorCode != ErrorCode.OPUS_OK) {
            throw new OpusNativeException(errorCode);
        }
        return new OpusEncoder(encoder, ByteBuffer.allocate(maxEncodedFrameSize));
    }
}
