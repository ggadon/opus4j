package com.opus4j.core;

import com.opus4j.core.errors.OpusNativeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Home on 16/03/2017.
 */
public class OpusMain {

    public  static class Pair {
        private final int count;
        private final int frames;

        public Pair(int count, int frames) {
            this.count = count;
            this.frames = frames;
        }
    }

    public static void main(String[] args) throws OpusNativeException, IOException {
        Path inputPath = Paths.get("/Users/Home/Documents/Guy Documents/pcm_frames2.raw");
        Path encodedPath = Paths.get("/Users/Home/Documents/Guy Documents/encoded_frames2.opus");
        ByteBuffer pcmFrames = ByteBuffer.wrap(
                Files.readAllBytes(inputPath));
        List<Pair> allFrames = new LinkedList<>();

        System.out.println(pcmFrames.capacity());
        OpusEncoder enc = OpusEncoder.create(24000, 2, NativeMappings.EncodingApplication.OPUS_APPLICATION_VOIP, 1000000);
        while (pcmFrames.remaining() >= 120) {
            ShortBuffer inputBuffer = ShortBuffer.allocate(60);
            for (int i = 0; i<60;++i) {
                inputBuffer.put(pcmFrames.getShort());
            }
            ByteBuffer ans = enc.encode(inputBuffer, 60);
            allFrames.add(new Pair(ans.capacity(), 60));
            Files.write(encodedPath, ans.array(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }

        if (pcmFrames.remaining() > 0) {
            int remaining = pcmFrames.remaining() / 2;
            System.out.println(remaining);
            ShortBuffer inputBuffer = ShortBuffer.allocate(remaining);
            for (int i = 0; i<remaining;++i) {
                inputBuffer.put(pcmFrames.getShort());
            }
            ByteBuffer ans = enc.encode(inputBuffer, 60);
            allFrames.add(new Pair(ans.capacity(), remaining));
            Files.write(encodedPath, ans.array(), StandardOpenOption.APPEND);
        }

        Path outputRawPath = Paths.get("/Users/Home/Documents/Guy Documents/output_raw.raw");
        ByteBuffer encodedFrames = ByteBuffer.wrap(Files.readAllBytes(encodedPath));
        OpusDecoder decoder = OpusDecoder.create(24000, 2, 10000);
        for (Pair p: allFrames) {
            byte[] encoded = new byte[p.count];
            encodedFrames.get(encoded);
            ShortBuffer buffer = decoder.decode(encoded, p.frames, false);
            ByteBuffer toWrite = ByteBuffer.allocate(buffer.capacity() * 2);
            while (buffer.hasRemaining()) {
                toWrite.putShort(buffer.get());
            }

            Files.write(outputRawPath, toWrite.array(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
//        byte[] encodedBytes = new byte[encoded.capacity()];
//        encoded.get(encodedBytes);
//        byte[] data = new byte[3];
//        encodedFrames.get(data);
//
//        System.out.println(buffer);
    }
}
