public class Main {
    public static void main(String[] args) {
        Object o = new Foo().getMessage();
        System.out.println("We got "+o+", expecting "+args[0]);
        System.exit(o.equals(args[0])?0:1);
    }
}