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

    static boolean toggle;

    @WithBridgeMethods(void.class)
    public static boolean hello() {
        toggle = true;
        return true;
    }

    @WithBridgeMethods(void.class)
    public static String hello2() {
        toggle = true;
        return "hello2";
    }

    @WithBridgeMethods(void.class)
    public boolean hello3() {
        toggle = true;
        return true;
    }

    @WithBridgeMethods(void.class)
    public String hello4() {
        toggle = true;
        return "hello4";
    }
    
    @WithBridgeMethods(value=int.class, castRequired=true)
    public static Integer unbox() {
        return Integer.MIN_VALUE;
    }
    
    @WithBridgeMethods(value=Integer.class)
    public static int box() {
        return Integer.MAX_VALUE;
    }
}