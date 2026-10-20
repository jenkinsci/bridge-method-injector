import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Abs extends IAbs {
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

    public Object stripAbstract() {  return new Object(); }

    public URL adapterInAbstract() {
        return adapter();
    }

}
