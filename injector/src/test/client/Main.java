public class Main {
    public static void main(String[] args) {
        Object o = new Foo().getMessage();
        System.out.println("We got "+o+", expecting "+args[0]);
        Object s = Foo.getStaticMessage();
        System.out.println("We got "+s+", expecting "+args[0]);
        Object w = Foo.<String>methodToWiden(String.class);
        System.out.println("We got "+w+", expecting "+args[0]);
        System.exit((o.equals(args[0]) && s.equals(args[0]))?0:1);
    }
}