public class Abs extends IAbs {
    public String widen()  { return "foo"; }
    public Object narrow() { return "foo"; }
    public String adapter() { return "http://example.com/"; }
    public void stripAbstract() { }
    public String adapterInAbstract() { return adapter(); }
}
