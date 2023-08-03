import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Bar implements IBar {
    @WithBridgeMethods(value = String.class, castRequired = true)
    public Object widen()  { return "bar"; }

    @WithBridgeMethods(Object.class)
    public String narrow() { return "bar"; }

    @WithBridgeMethods(value = String.class, adapterMethod = "convert")
    public URL adapter() {
        try {
            return new URL("http://example.com/");
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Object convert(URL url, Class<?> type) {
        return url.toString();
    }
}