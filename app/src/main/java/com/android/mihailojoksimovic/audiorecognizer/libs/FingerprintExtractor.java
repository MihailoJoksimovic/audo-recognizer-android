package com.android.mihailojoksimovic.audiorecognizer.libs;

import com.android.mihailojoksimovic.audiorecognizer.libs.math.Complex;
import com.android.mihailojoksimovic.audiorecognizer.libs.math.FFT;
import com.android.mihailojoksimovic.audiorecognizer.libs.windowing.HammingWindow;

public class FingerprintExtractor
{
    private static final int WINDOW_SIZE_IN_MS      = 25; // in milliseconds
    private static final int WINDOW_BUCKET_RATIO    = 1000 / WINDOW_SIZE_IN_MS;

    private static int LOWER_RANGE                  = 40;
    private static int UPPER_RANGE                  = 300;
    public final int[] RANGE                        = new int[] { LOWER_RANGE, 80, 120, 180, UPPER_RANGE+1 };

    private static final int FUZ_FACTOR             = 2;

    private static FingerprintExtractor instance;

    public static FingerprintExtractor getInstance() {
        if (instance == null) {
            instance = new FingerprintExtractor();
        }

        return instance;
    }

    /**
     * Extract array of fingerprints from the song
     *
     * @param samples
     * @return
     */
    public double[] extractFingerprints(short[] samples, int sampleRate) {
        final int WINDOW_SIZE           = largestPowerOf2(sampleRate / WINDOW_BUCKET_RATIO);
        final int HALF_WINDOW_SIZE      = WINDOW_SIZE / 2;

        HammingWindow windowingFunction = new HammingWindow(WINDOW_SIZE);

        double[] fingerprints           = new double[(int) (samples.length / HALF_WINDOW_SIZE) + 1];

        int counter                     = 0;

        for (int i = 0; (i+WINDOW_SIZE) <= samples.length; i+=HALF_WINDOW_SIZE) {
            short[] chunks = new short[WINDOW_SIZE];

            System.arraycopy(samples, i, chunks, 0, WINDOW_SIZE);

            windowingFunction.applyFunction(chunks);

            Complex[] complexChunks = new Complex[chunks.length];

            for (int j = 0; j < chunks.length; j++) {
                complexChunks[j] = new Complex(chunks[j], 0);
            }

            Complex[] bins = FFT.fft(complexChunks);

            double highestMags[]    = new double[RANGE.length];
            double highestFreqs[]   = new double[RANGE.length];

            for (int j = 0; j < bins.length / 2; j++) {
                double freq = j;

                if (freq > UPPER_RANGE) {
                    break;
                }

                int index   = getIndex(freq);

                if (bins[j].abs() > highestMags[index]) {
                    highestMags[index]  = bins[j].abs();
                    highestFreqs[index] = freq;
                }
            }

            // Compute the median amplitude for highest freqs.
            // Get rid of all freqs below median amplitude level

            double avgMag       = 0;
            double avgSum       = 0;

            for (int j = 0; j < highestMags.length; j++) {
                avgSum += highestMags[j];
            }

            avgMag  = avgSum / highestMags.length;

            // Get rid of freqs below the limit
            for (int j = 0; j < highestMags.length; j++) {
                if (highestMags[j] < avgMag) {
                    highestFreqs[j] = 0;
                }
            }

            double hash = hash(highestFreqs);

            fingerprints[counter++] = hash;
        }

        return fingerprints;
    }

    private int largestPowerOf2 (int n)
    {
        int res = 2;
        while (res < n) {
            res *= 2;
        }

        return res;
    }

    private int getIndex(double freq) {
        int i = 0;
        while (RANGE[i] < freq)
            i++;
        return i;
    }

    private static double hash(double[] freqs) {
        int size    = freqs.length;

        double hash   = 0;

        for (int i = 0; i < size; i++) {
            int pow = (i < 3) ? i * 2 : i * 3;

            hash += (freqs[i] - (freqs[i] % FUZ_FACTOR)) * Math.pow(10, pow); //

        }

        return hash;

    }
}
