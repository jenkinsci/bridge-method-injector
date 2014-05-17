import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

public class B extends A {

    @WithBridgeMethods(value=String.class,castRequired=true)
    public CharSequence getProperty() {
        return null;
    }
}