public class Adapter {
    public int i() { return 1; }
    public String o() { return "http://kohsuke.org/"; }
    public String[] array() {
        return new String[]{ "http://kohsuke.org/" };
    }

    // Just making sure we do not barf on Java 8 constructs:
    interface SomeInterface {
        default void someMethod() {}
    }
    static class SomeClass implements SomeInterface {
        @Override
        public void someMethod() {
            SomeInterface.super.someMethod();
        }
    }

    // we will evolve this from int -> long
    public int l() { return 1; }
}
