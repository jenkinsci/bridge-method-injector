public class Foo implements IFoo {
    /**
     * Testing narrowing. In v2, we'll narrow this to return the String type. This is type safe.
     */
    public Object getMessage() {
        return "foo";
    }

    /**
     * Testing widening. In v2, we'll widen this to Object. Potentially type unsafe.
     */
    public String getString() {
        return "foo";
    }
    
    public static Object getStaticMessage() {
      return "foo";
    }

    public static <T extends String> T methodToWiden(Class<T> clazz) {
      return clazz.cast("foo");
    }

    public static void hello() {}
}