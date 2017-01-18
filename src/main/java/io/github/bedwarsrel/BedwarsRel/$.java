package io.github.bedwarsrel.BedwarsRel;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created on 16-12-15.
 */
public final class $ {

    static final Helper HELPER;

    private $() {
        throw new UnsupportedOperationException();
    }

    static {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        try {
            engine.eval("" +
                    "function getHandle(i) {" +
                    "   return i.handle;" +
                    "}");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        HELPER = Invocable.class.cast(engine).getInterface(Helper.class);
    }

    public interface Helper {

        Object getHandle(Object i);
    }

    public static Object getHandle(Object i) {
        return HELPER.getHandle(i);
    }

    public static boolean nil(Object i) {
        return i == null;
    }

    public static long now() {
        return System.currentTimeMillis();
    }

}
