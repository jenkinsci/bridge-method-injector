import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

public class Foo {
    @WithBridgeMethods(Object.class)
    public String getMessage() {
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
}