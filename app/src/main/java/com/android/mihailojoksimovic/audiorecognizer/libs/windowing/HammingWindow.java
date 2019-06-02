package com.android.mihailojoksimovic.audiorecognizer.libs.windowing;

import java.util.HashMap;
import java.util.Map;

/**
 * Hamming Window Function
 * <p>
 * Threading : this class is thread safe
 * </p>
 * @see <a href="http://en.wikipedia.org/wiki/Window_function#Hamming_window">Hamming window<a/>
 * @see WindowFunction
 * @author Amaury Crickx
 */
public class HammingWindow  extends WindowFunction {

    private static final Map<Integer, double[]> factorsByWindowSize = new HashMap<Integer, double[]>();

    /**
     * Constructor imposed by WindowFunction
     * @param windowSize the windowSize
     * @see WindowFunction#WindowFunction(int)
     */
    public HammingWindow(int windowSize) {
        super(windowSize);
    }

    @Override
    protected double[] getPrecomputedFactors(int windowSize) {
        // precompute factors for given window, avoid re-calculating for several instances
        synchronized (HammingWindow.class) {
            double[] factors;
            if(factorsByWindowSize.containsKey(windowSize)) {
                factors = factorsByWindowSize.get(windowSize);
            } else {
                factors = new double[windowSize];
                int sizeMinusOne = windowSize - 1;
                for(int i = 0; i < windowSize; i++) {
                    factors[i] = 0.54d - (0.46d * Math.cos((TWO_PI * i) / sizeMinusOne));
                }
                factorsByWindowSize.put(windowSize, factors);
            }
            return factors;
        }
    }

}
