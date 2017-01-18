package io.github.bedwarsrel.BedwarsRel;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * Created on 17-1-16.
 */
@RequiredArgsConstructor
public class Timing {

    private static class MXTiming extends HashMap<String, Timing> {

        private final static MXTiming TIMING = new MXTiming();

        private Timing look(String key) {
            return computeIfAbsent(key, k -> new Timing(key));
        }
    }

    private void add(long t) {
        total = total + t;
        i++;
        max = Math.max(max, t);
        latest = t;
    }

    public static Timing timing(String key, Runnable code) {
        long time = System.nanoTime();
        try {
            code.run();
        } catch (Exception e) {
            Main.log("timing handler", e);
        }
        time = System.nanoTime() - time;
        Timing timing = MXTiming.TIMING.look(key);
        timing.add(time);
        return timing;
    }

    @Override
    public String toString() {
        return (key + " " +
                "(" +
                "latest:" + latest +
                ",total:" + total +
                ",avg:" + (total / i) +
                ",max:" + max +
                ",count:" + i +
                ")");
    }

    private final String key;
    private long total;
    private long max;
    private long i;
    private long latest;

}
