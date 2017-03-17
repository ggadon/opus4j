package com.opus4j.core;

import com.opus4j.core.errors.ErrorCode;
import com.opus4j.core.errors.OpusNativeException;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * An OpusDecoder state.
 * Initialized with sample rate and channels.
 *
 * No thread-safe guaranteed.
 *
 * Created by ggadon on 11/03/2017.
 */
public class OpusDecoder {

    /** The native decoder state */
    private NativeMappings.OpusDecoder decoderState;

    /** The maximum pcm buffer to decode. Based on the maximum packet size (allocates twice than the max size) */
    private ShortBuffer pcmBuffer;

    /**
     * c'tor.
     *
     * @param decoderState the native decoder state to use
     */
    OpusDecoder(NativeMappings.OpusDecoder decoderState, int maxPacketSize) {
        this.decoderState = decoderState;
        this.pcmBuffer = ShortBuffer.allocate(maxPacketSize);
    }


    /**
     * Decode a frame.
     * @see NativeMappings#opus_decode(NativeMappings.OpusDecoder, byte[], int, ShortBuffer, int, int)
     * @return a buffer contains the decoded frame
     * @throws OpusNativeException in case of native exception
     */
    public ShortBuffer decode(byte[] data, int samplesPerChannel, boolean decodeFec) throws OpusNativeException {
        int framesDecoded = NativeMappings.opus_decode(decoderState, data, data.length, pcmBuffer,
                samplesPerChannel, decodeFec ? 1: 0);
        if (framesDecoded < 0) {
            throw new OpusNativeException(ErrorCode.fromErrorNum(framesDecoded));
        }

        try {
            short[] outputBuffer = new short[framesDecoded];
            pcmBuffer.get(outputBuffer);

            return ShortBuffer.wrap(outputBuffer);
        } finally {
            pcmBuffer.position(0);
        }
    }

    /**
     * Destroy the native state.
     * MUST be called in order to avoid memory leaks.
     */
    public void destroy() {
        NativeMappings.opus_decoder_destroy(decoderState);
    }

    /**
     * Create a new OpusDecoder with the given parameters.
     *
     * @param sampleRate Sample rate. Must be one of: 8000/12000/16000/24000/32000/44100/48000
     *
     * @param channels The number of channels. Must be one of 1/2
     *
     * @param maxPacketSize the maximum packet size that the decoder is going to deal with. Determines the size of the
     *                      buffer to allocate.
     *
     * @return a new OpusDecoder state
     * @throws OpusNativeException in case of internal or api error
     */
    public static OpusDecoder create (int sampleRate, int channels, int maxPacketSize) throws OpusNativeException {
        IntBuffer errBuf = IntBuffer.allocate(1);
        NativeMappings.OpusDecoder decoderState = NativeMappings.opus_decoder_create(sampleRate, channels, errBuf);
        ErrorCode errorCode = ErrorCode.fromErrorNum(errBuf.get());
        if (errorCode != ErrorCode.OPUS_OK) {
            throw new OpusNativeException(errorCode);
        }

        return new OpusDecoder(decoderState, maxPacketSize);
    }
}
