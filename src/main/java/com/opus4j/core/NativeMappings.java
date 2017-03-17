package com.opus4j.core;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Structure;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 04/03/2017.
 */
public class NativeMappings {

    static final String OPUS_LIB_NAME = "opus";

    static {
        Native.register(
                NativeMappings.class,
                NativeLibrary.getInstance(OPUS_LIB_NAME)
        );
    }

    /** Allocates and initializes a decoder state.
     * @param sampleRate Sample rate to decode at (Hz).
     *                                     This must be one of 8000, 12000, 16000,
     *                                     24000, or 48000.
     * @param channels <tt>int</tt>: Number of channels (1 or 2) to decode
     * @param errors <tt>int*</tt>: #OPUS_OK Success or @ref opus_errorcodes
     *
     * Internally OpusDecoder stores data at 48000 Hz, so that should be the default
     * value for Fs. However, the decoder can efficiently decode to buffers
     * at 8, 12, 16, and 24 kHz so if for some reason the caller cannot use
     * data at the full sample rate, or knows the compressed data doesn't
     * use the full frequency range, it can request decoding at a reduced
     * rate. Likewise, the decoder is capable of filling in either mono or
     * interleaved stereo pcm buffers, at the caller's request.
     */
    // OpusDecoder * opus_decoder_create (opus_int32 Fs, int channels, int *error)
    static native OpusDecoder opus_decoder_create(int sampleRate, int channels, IntBuffer errors);

    /** Decode an Opus packet.
     * @param decoder <tt>OpusDecoder*</tt>: Decoder state
     * @param data <tt>char*</tt>: Input payload. Use a NULL pointer to indicate packet loss
     * @param length <tt>opus_int32</tt>: Number of bytes in payload*
     * @param pcm <tt>opus_int16*</tt>: Output signal (interleaved if 2 channels). length
     *  is frame_size*channels*sizeof(opus_int16)
     * @param frameSize Number of samples per channel of available space in \a pcm.
     *  If this is less than the maximum packet duration (120ms; 5760 for 48kHz), this function will
     *  not be capable of decoding some packets. In the case of PLC (data==NULL) or FEC (decode_fec=1),
     *  then frame_size needs to be exactly the duration of audio that is missing, otherwise the
     *  decoder will not be in the optimal state to decode the next incoming packet. For the PLC and
     *  FEC cases, frame_size <b>must</b> be a multiple of 2.5 ms.
     * @param decodeFec: Flag (true or false) to request that any in-band forward error correction data be
     *  decoded. If no such data is available, the frame is decoded as if it were lost.
     * @return Number of decoded samples or @ref opus_errorcodes
     */
    // int 	opus_decode (OpusDecoder *st, const unsigned char *data, int len, opus_int16 *pcm, int frame_size, int decode_fec)
    static native int opus_decode(OpusDecoder decoder, byte[] data, int length, ShortBuffer pcm,
                                  int frameSize, int decodeFec);

    /** Frees an <code>OpusDecoder</code> allocated by opus_decoder_create().
     * @param decoder <tt>OpusDecoder*</tt>: State to be freed.
     */
    static native void opus_decoder_destroy(OpusDecoder decoder);

    /** Allocates and initializes an encoder state.
     * There are three coding modes:
     *
     * OPUS_APPLICATION_VOIP gives best quality at a given bitrate for voice
     *    signals. It enhances the  input signal by high-pass filtering and
     *    emphasizing formants and harmonics. Optionally  it includes in-band
     *    forward error correction to protect against packet loss. Use this
     *    mode for typical VoIP applications. Because of the enhancement,
     *    even at high bitrates the output may sound different from the input.
     *
     * OPUS_APPLICATION_AUDIO gives best quality at a given bitrate for most
     *    non-voice signals like music. Use this mode for music and mixed
     *    (music/voice) content, broadcast, and applications requiring less
     *    than 15 ms of coding delay.
     *
     * OPUS_APPLICATION_RESTRICTED_LOWDELAY configures low-delay mode that
     *    disables the speech-optimized mode in exchange for slightly reduced delay.
     *    This mode can only be set on an newly initialized or freshly reset encoder
     *    because it changes the codec delay.
     *
     * This is useful when the caller knows that the speech-optimized modes will not be needed (use with caution).
     * @param sampleRate <tt>opus_int32</tt>: Sampling rate of input signal (Hz)
     *                                     This must be one of 8000, 12000, 16000,
     *                                     24000, or 48000.
     * @param channels <tt>int</tt>: Number of channels (1 or 2) in input signal
     * @param application <tt>int</tt>: Coding mode (@ref OPUS_APPLICATION_VOIP/@ref OPUS_APPLICATION_AUDIO/@ref OPUS_APPLICATION_RESTRICTED_LOWDELAY)
     * @param error <tt>int*</tt>: @ref opus_errorcodes
     * @note Regardless of the sampling rate and number channels selected, the Opus encoder
     * can switch to a lower audio bandwidth or number of channels if the bitrate
     * selected is too low. This also means that it is safe to always use 48 kHz stereo input
     * and let the encoder optimize the encoding.
     */
    static native OpusEncoder opus_encoder_create(int sampleRate, int channels,
                                                  int application, IntBuffer error);


    /** Encodes an Opus frame.
     * @param encoder <tt>OpusEncoder*</tt>: Encoder state
     * @param pcm <tt>opus_int16*</tt>: Input signal (interleaved if 2 channels). length is frame_size*channels*sizeof(opus_int16)
     * @param frameSize <tt>int</tt>: Number of samples per channel in the
     *                                      input signal.
     *                                      This must be an Opus frame size for
     *                                      the encoder's sampling rate.
     *                                      For example, at 48 kHz the permitted
     *                                      values are 120, 240, 480, 960, 1920,
     *                                      and 2880.
     *                                      Passing in a duration of less than
     *                                      10 ms (480 samples at 48 kHz) will
     *                                      prevent the encoder from using the LPC
     *                                      or hybrid modes.
     * @param outputData <tt>unsigned char*</tt>: Output payload.
     *                                            This must contain storage for at
     *                                            least \a max_data_bytes.
     * @param maxDataBytes <tt>opus_int32</tt>: Size of the allocated
     *                                                 memory for the output
     *                                                 payload. This may be
     *                                                 used to impose an upper limit on
     *                                                 the instant bitrate, but should
     *                                                 not be used as the only bitrate
     *                                                 control. Use #OPUS_SET_BITRATE to
     *                                                 control the bitrate.
     * @returns The length of the encoded packet (in bytes) on success or a
     *          negative error code (see @ref opus_errorcodes) on failure.
     */
    static native int opus_encode(OpusEncoder encoder, ShortBuffer pcm, int frameSize,
                                  ByteBuffer outputData, int maxDataBytes);


    /** Frees an <code>OpusEncoder</code> allocated by opus_encoder_create().
     * @param encoder <tt>OpusEncoder*</tt>: State to be freed.
     */
    static native void opus_encoder_destroy(OpusEncoder encoder);

    /**
     * OpusDecoder decoder state.
     * This contains the complete state of an OpusDecoder decoder.
     * It is position independent and can be freely copied.
     */
    static class OpusDecoder extends Structure {

        public int celt_dec_offset;
        public int silk_dec_offset;
        public int channels;
        public int Fs;
        public silk_DecControlStruct DecControl;
        public int decode_gain;
        public int stream_channels;
        public int bandwidth;
        public int mode;
        public int prev_mode;
        public int frame_size;
        public int prev_redundancy;
        public int last_packet_duration;


        @Override
        protected List<String> getFieldOrder() {
            List<String> list = new ArrayList<String>();
            list.add("celt_dec_offset");
            list.add("silk_dec_offset");
            list.add("channels");
            list.add("Fs");
            list.add("DecControl");
            list.add("decode_gain");
            list.add("stream_channels");
            list.add("bandwidth");
            list.add("mode");
            list.add("prev_mode");
            list.add("frame_size");
            list.add("prev_redundancy");
            list.add("last_packet_duration");
            return list;
        }
    }

    /**
     * Structure for controlling decoder operation and reading decoder status
     */
    static class silk_DecControlStruct extends Structure {

        /** I:   Number of channels; 1/2 */
        public int nChannelsAPI;

        /** I:   Number of channels; 1/2 */
        public int nChannelsInternal;

        /** I:   Output signal sampling rate in Hertz; 8000/12000/16000/24000/32000/44100/48000 */
        public int API_sampleRate;

        /** I:   Internal sampling rate used, in Hertz; 8000/12000/16000 */
        public int internalSampleRate;

        /** I:   Number of samples per packet in milliseconds; 10/20/40/60  */
        public int payloadSize_ms;

        /** O:   Pitch lag of previous frame (0 if unvoiced), measured in samples at 48 kHz */
        public int prevPitchLag;

        @Override
        protected List<String> getFieldOrder() {
            List<String> list = new ArrayList<String>();
            list.add("nChannelsAPI");
            list.add("nChannelsInternal");
            list.add("API_sampleRate");
            list.add("internalSampleRate");
            list.add("payloadSize_ms");
            list.add("prevPitchLag");


            return list;
        }
    }

    static class OpusEncoder extends Structure {

        public int celt_enc_offset;
        public int silk_enc_offset;
        public silk_EncControlStruct silk_mode;
        public int application;
        public int channels;
        public int delay_compensation;
        public int force_channels;
        public int signal_type;
        public int user_bandwidth;
        public int max_bandwidth;
        public int user_forced_mode;
        public int voice_ratio;
        public int Fs;
        public int use_vbr;
        public int vbr_constraint;
        public int variable_duration;
        public int bitrate_bps;
        public int user_bitrate_bps;
        public int lsb_depth;
        public int encoder_buffer;
        public int lfe;
        public int stream_channels;


        protected List<String> getFieldOrder() {
            List<String> list = new ArrayList<String>();
            list.add("celt_enc_offset");
            list.add("silk_enc_offset");
            list.add("silk_mode");
            list.add("application");
            list.add("channels");
            list.add("delay_compensation");
            list.add("force_channels");
            list.add("signal_type");
            list.add("user_bandwidth");
            list.add("max_bandwidth");
            list.add("user_forced_mode");
            list.add("voice_ratio");
            list.add("Fs");
            list.add("use_vbr");
            list.add("vbr_constraint");
            list.add("variable_duration");
            list.add("bitrate_bps");
            list.add("user_bitrate_bps");
            list.add("lsb_depth");
            list.add("encoder_buffer");
            list.add("lfe");
            list.add("stream_channels");
            return list;
        }
    }

    static class silk_EncControlStruct extends Structure {

        /**
         * I:   Number of channels; 1/2
         */
        public int nChannelsAPI;

        /**
         * I:   Number of channels; 1/2
         */
        public int nChannelsInternal;

        /**
         * I:   Input signal sampling rate in Hertz; 8000/12000/16000/24000/32000/44100/48000
         */
        public int API_sampleRate;

        /**
         * I:   Maximum internal sampling rate in Hertz; 8000/12000/16000
         */
        public int maxInternalSampleRate;

        /**
         * I:   Minimum internal sampling rate in Hertz; 8000/12000/16000
         */
        public int minInternalSampleRate;

        /**
         * I:   Soft request for internal sampling rate in Hertz; 8000/12000/16000
         */
        public int desiredInternalSampleRate;

        /**
         * I:   Number of samples per packet in milliseconds; 10/20/40/60
         */
        public int payloadSize_ms;

        /**
         * I:   Bitrate during active speech in bits/second; internally limited
         */
        public int bitRate;

        /**
         * I:   Uplink packet loss in percent (0-100)
         */
        public int packetLossPercentage;

        /**
         * I:   Complexity mode; 0 is lowest, 10 is highest complexity
         */
        public int complexity;

        /**
         * I:   Flag to enable in-band Forward Error Correction (FEC); 0/1
         */
        public int useInBandFEC;

        /**
         * I:   Flag to enable discontinuous transmission (DTX); 0/1
         */
        public int useDTX;

        /**
         * I:   Flag to use constant bitrate
         */
        public int useCBR;

        /**
         * I:   Maximum number of bits allowed for the frame
         */
        public int maxBits;

        /**
         * I:   Causes a smooth downmix to mono
         */
        public int toMono;

        /**
         * I:   Opus encoder is allowing us to switch bandwidth
         */
        public int opusCanSwitch;

        /**
         * I: Make frames as independent as possible (but still use LPC)
         */
        public int reducedDependency;

        /**
         * O:   Internal sampling rate used, in Hertz; 8000/12000/16000
         */
        public int internalSampleRate;

        /**
         * O: Flag that bandwidth switching is allowed (because low voice activity)
         */
        public int allowBandwidthSwitch;

        /**
         * O:   Flag that SILK runs in WB mode without variable LP filter (use for switching between WB/SWB/FB)
         */
        public int inWBmodeWithoutVariableLP;

        /**
         * O:   Stereo width
         */
        public int stereoWidth_Q14;

        /**
         * O:   Tells the Opus encoder we're ready to switch
         */
        public int switchReady;

        protected List<String> getFieldOrder() {
            List<String> list = new ArrayList<String>();
            list.add("nChannelsAPI");
            list.add("nChannelsInternal");
            list.add("API_sampleRate");
            list.add("maxInternalSampleRate");
            list.add("minInternalSampleRate");
            list.add("desiredInternalSampleRate");
            list.add("payloadSize_ms");
            list.add("bitRate");
            list.add("packetLossPercentage");
            list.add("complexity");
            list.add("useInBandFEC");
            list.add("useDTX");
            list.add("useCBR");
            list.add("maxBits");
            list.add("toMono");
            list.add("opusCanSwitch");
            list.add("reducedDependency");
            list.add("internalSampleRate");
            list.add("allowBandwidthSwitch");
            list.add("inWBmodeWithoutVariableLP");
            list.add("stereoWidth_Q14");
            list.add("switchReady");
            return list;
        }
    }

    /**
     * The encoding application is used when initializing a new encoder.
     * See different types to understand usage.
     * Used in {@link com.opus4j.core.OpusEncoder#create(int, int, EncodingApplication)}
     */
    public enum EncodingApplication {
        /** Best for most VoIP/videoconference applications where listening quality and intelligibility matter most */
        OPUS_APPLICATION_VOIP (2048),

        /**
         * Best for broadcast/high-fidelity application where the decoded audio should be
         * as close as possible to the input
         */
        OPUS_APPLICATION_AUDIO (2049),

        /** Only use when lowest-achievable latency is what matters most. Voice-optimized modes cannot be used */
        OPUS_APPLICATION_RESTRICTED_LOWDELAY (2051);

        /** The value of the encoding application. Used in native code. */
        private int value;

        EncodingApplication(int value) {
            this.value = value;
        }

        /**
         * Get the native value
         * @return the native value.
         */
        public int getValue() {
            return value;
        }
    }
}
