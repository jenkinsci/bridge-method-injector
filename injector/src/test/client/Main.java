public class Main {
    public static void main(String[] args) throws Exception {
        // invocation of void method. verify that it runs without error
        Foo.hello();

        Object o = new Foo().getMessage();
        assertEquals(args[0],o);

        String n = new Foo().getString();
        assertEquals(args[0],n);

        Object s = Foo.getStaticMessage();
        assertEquals(args[0],s);

        Object w = Foo.<String>methodToWiden(String.class);
        assertEquals(args[0],w);

        // using reflection to ensure that JIT isn't doing inlining
        check((Foo)Foo.class.newInstance(),args[0]);

        // still a work in progress
//        check((Bar)Bar.class.newInstance(),args[0]);
    }

    private static void assertEquals(Object expected, Object actual) {
        System.out.println("We got "+actual+", expecting "+expected);
        if (!actual.equals(expected))
            System.exit(1);
    }

    private static void check(IFoo f, String expected) {
        Object o = f.getMessage();
        assertEquals(expected,o);

        String n = f.getString();
        assertEquals(expected,n);
    }

    private static void check(IBar f, String expected) {
        Object o = f.narrow();
        assertEquals(expected,o);

        String n = f.widen();
        assertEquals(expected,n);
    }
}
