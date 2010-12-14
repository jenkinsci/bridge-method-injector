import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

public interface IFoo {
    @WithBridgeMethods(Object.class)
    public String getMessage();

    @WithBridgeMethods(value=String.class,castRequired=true)
    public Object getString();
}