import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.net.URL;

public interface IBar {
    @WithBridgeMethods(value=String.class,castRequired=true)
    Object widen();
    @WithBridgeMethods(Object.class)
    String narrow();
    @WithBridgeMethods(String.class)
    URL adapter();
    @WithBridgeMethods(value=void.class,stripAbstract=true)
    Object stripAbstract();
    @WithBridgeMethods(value=String.class,stripAbstract=true,adapterMethod="convertInAbstract")
    URL adapterInAbstract();

    private Object convertInAbstract(URL url, Class<?> type) {
        return url.toString();
    }
}
