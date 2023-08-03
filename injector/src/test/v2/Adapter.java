import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.io.IOException;
import java.net.URL;

public class Adapter {
    @WithBridgeMethods(value=int.class, adapterMethod="_i")
    public String i() { return "1"; }

    private Object _i(String o, Class type) {
        return Integer.parseInt(o);
    }

    @WithBridgeMethods(value=String.class, adapterMethod="_o")
    URL o() throws IOException { return new URL("http://kohsuke.org/"); }

    private Object _o(URL o, Class type) {
        return o.toString();
    }

    @WithBridgeMethods(value = String[].class, adapterMethod = "_array")
    URL[] array() throws IOException {
        return new URL[]{ new URL("http://kohsuke.org/") };
    }

    private Object _array(URL[] array, Class<?> type) {
        String[] result = new String[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i].toString();
        }
        return result;
    }

    interface SomeInterface {
        default void someMethod() {}
    }
    static class SomeClass implements SomeInterface {
        @Override
        public void someMethod() {
            SomeInterface.super.someMethod();
        }
    }

    @WithBridgeMethods(value=int.class, adapterMethod="l2i")
    public long l() { return 1L; }

    private Object l2i(long v, Class type) { return (int)v; }
}