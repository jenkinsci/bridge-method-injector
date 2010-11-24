public class Foo {
    public Object getMessage() {
        return "foo";
    }
    
    public static Object getStaticMessage() {
      return "foo";
    }
    
    public static <T extends String> T methodToWiden(Class<T> clazz) {
      return clazz.cast("foo");
    }
}