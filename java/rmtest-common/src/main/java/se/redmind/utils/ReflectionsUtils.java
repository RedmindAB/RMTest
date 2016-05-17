package se.redmind.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * @author Jeremy Comte
 */
public class ReflectionsUtils {

    private static Reflections reflections;

    public static synchronized Reflections current() {
        if (reflections == null) {
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            FilterBuilder filterBuilder = new FilterBuilder();

            Set<URL> classPath = ClasspathHelper.forJavaClassPath().stream()
                .filter(url -> new File(URI.create(url.toString())).exists())
                .collect(Collectors.toSet());

            configurationBuilder.addUrls(classPath);

            filterBuilder.include(FilterBuilder.prefix("se.redmind"));

            configurationBuilder.filterInputsBy(filterBuilder).setScanners(
                new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new MethodParameterScanner(),
                new MethodAnnotationsScanner(),
                new FieldAnnotationsScanner());
            reflections = new Reflections(configurationBuilder);
        }

        return reflections;
    }

}
