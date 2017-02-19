package cucumber.runtime.java.picocontainer;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Utils;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Originally from cucumber-picocontainer but we needed to be able to add instances
 *
 * @author Jeremy Comte
 */
public class PicoFactory implements ObjectFactory {

    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new LinkedHashSet<>();
    private final Map<Class<?>, Object> instances = new LinkedHashMap<>();

    @Override
    public void start() {
        pico = new PicoBuilder().withCaching().build();
        instances.keySet().forEach(classes::remove);
        classes.forEach(clazz -> pico.addComponent(clazz));
        instances.forEach((clazz, instance) -> pico.addComponent(clazz, instance));
        pico.start();
    }

    @Override
    public void stop() {
        pico.stop();
        pico.dispose();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        if (Utils.isInstantiable(clazz) && classes.add(clazz)) {
            addConstructorDependencies(clazz);
        }
        return true;
    }

    public boolean addInstance(Object instance) {
        instances.put(instance.getClass(), instance);
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return pico.getComponent(type);
    }

    private void addConstructorDependencies(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            for (Class<?> paramClazz : constructor.getParameterTypes()) {
                addClass(paramClazz);
            }
        }
    }
}
