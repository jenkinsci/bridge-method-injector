public class Main {
    public static void main(String[] args) throws Exception {
        // invocation of void method. verify that it runs without error

        Foo.toggle = false;
        Foo.hello();
        assertEquals(true, Foo.toggle);

        Foo.toggle = false;
        Foo.hello2();
        assertEquals(true, Foo.toggle);

        Foo.toggle = false;
        new Foo().hello3();
        assertEquals(true, Foo.toggle);

        Foo.toggle = false;
        new Foo().hello4();
        assertEquals(true, Foo.toggle);

        Foo.unbox();
        Foo.box();

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
        check((Bar)Bar.class.newInstance(),args[0]);

        Adapter a = new Adapter();
        assertEquals(1,a.i());
        assertEquals("http://kohsuke.org/",a.o());
        assertEquals("http://kohsuke.org/" + args[0], a.oParam(args[0]));
        assertEquals("http://kohsuke.org/" + args[0] + "/" + args[0] + "/" + args[0], a.oParams(args[0], args[0], args[0]));

        String[] array = a.array();
        assertEquals(1, array.length);
        assertEquals("http://kohsuke.org/", array[0]);

        new Adapter.SomeClass().someMethod();

        assertEquals(1,a.l());
    }

    private static void assertEquals(Object expected, Object actual) {
        System.out.println("We got "+actual+", expecting "+expected);
        if (!actual.equals(expected)) {
            System.exit(1);
        }
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

        String u = f.adapter();
        assertEquals("http://example.com/", u);

        f.stripAbstract();
    }
}
