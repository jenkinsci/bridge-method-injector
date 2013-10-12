import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

public class Foo implements IFoo {
    @WithBridgeMethods(Object.class)
    public String getMessage() {
        return "bar";
    }

    /**
     * Widening String to Object.
     */
    @WithBridgeMethods(value=String.class,castRequired=true)
    public Object getString() {
        return "bar";
    }

    @WithBridgeMethods(Object.class)
    public static String getStaticMessage() {
      return "bar";
    }

    @WithBridgeMethods(value=String.class, castRequired=true)
    public static <T> T methodToWiden(Class<T> clazz) {
      return clazz.cast("bar");
    }

    @WithBridgeMethods(void.class)
    public static boolean hello() {
        return true;
    }

    @WithBridgeMethods(void.class)
    public static String hello2() {
        return "hello2";
    }
}