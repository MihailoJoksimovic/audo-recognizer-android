package com.android.mihailojoksimovic.audiorecognizer.libs.windowing;

import java.util.HashMap;
import java.util.Map;

/**
 * Hann Window function
 * <p>
 * Threading : this class is thread safe
 * </p>
 * @see <a href="http://en.wikipedia.org/wiki/Hann_function">Hann function<a/>
 * @see WindowFunction
 * @author Amaury Crickx
 */
public final class HannWindow extends WindowFunction {

    private static final Map<Integer, double[]> factorsByWindowSize = new HashMap<Integer, double[]>();

    /**
     * Constructor imposed by WindowFunction
     * @param windowSize the windowSize
     * @see WindowFunction#WindowFunction(int)
     */
    public HannWindow(int windowSize) {
        super(windowSize);
    }

    @Override
    protected double[] getPrecomputedFactors(int windowSize) {
        // precompute factors for given window, avoid re-calculating for several instances
        synchronized (HannWindow.class) {
            double[] factors;
            if(factorsByWindowSize.containsKey(windowSize)) {
                factors = factorsByWindowSize.get(windowSize);
            } else {
                factors = new double[windowSize];
                int sizeMinusOne = windowSize - 1;
                for(int i = 0; i < windowSize; i++) {
                    factors[i] = 0.5d * (1 - Math.cos((TWO_PI * i) / sizeMinusOne));
                }
                factorsByWindowSize.put(windowSize, factors);
            }
            return factors;
        }
    }

}