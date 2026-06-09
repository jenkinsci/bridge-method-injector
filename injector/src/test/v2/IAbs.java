import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.net.URL;

public abstract class IAbs {
    @WithBridgeMethods(value=String.class,castRequired=true)
    public abstract Object widen();
    @WithBridgeMethods(Object.class)
    public abstract String narrow();
    @WithBridgeMethods(String.class)
    public abstract URL adapter();
    @WithBridgeMethods(value=void.class,stripAbstract=true)
    public abstract Object stripAbstract();
    @WithBridgeMethods(value=String.class,stripAbstract=true,adapterMethod="convertInAbstract")
    public abstract URL adapterInAbstract();

    private Object convertInAbstract(URL url, Class<?> type) {
        return url.toString();
    }
}
