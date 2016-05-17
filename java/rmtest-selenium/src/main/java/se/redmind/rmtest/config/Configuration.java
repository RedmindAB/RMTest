package se.redmind.rmtest.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.*;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterators;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.runners.FilterDrivers;
import se.redmind.utils.*;

/**
 * @author Jeremy Comte
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Configuration {

    private static final String CONFIG_SYSTEM_PROPERTY = "config";
    private static final String DEFAULT_REPORTS_PATH = "/target/RMTReports";
    private static final String DEFAULT_LOCAL_CONFIG = "/etc/LocalConfig.yml";
    private static final String DEFAULT_LEGACY_CONFIG = "/etc/LocalConfig.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static final Set<WebDriverWrapper<?>> WRAPPERS = new LinkedHashSet<>();
    private static ObjectMapper objectMapper;
    private static Validator validator;

    // let's save the latest read config as a singleton, to be able to replicate the behavior of the legacy FrameworkConfig
    // TODO: dependency injection?
    private static Configuration current;

    @JsonIgnore
    private String filePath;

    @JsonProperty
    @NotEmpty
    @Valid
    public List<DriverConfiguration<?>> drivers = new ArrayList<>();

    @JsonProperty
    public boolean autoCloseDrivers = true;

    @JsonProperty
    public String rmReportIP = "127.0.0.1";

    @JsonProperty
    public int rmReportLivePort = 12345;

    @JsonProperty
    public String jsonReportSavePath = System.getProperty("user.dir") + DEFAULT_REPORTS_PATH;

    @JsonProperty
    public AndroidConfiguration android;

    @JsonProperty
    public boolean reuseDriverBetweenTests = true;

    @JsonProperty
    public int defaultTimeOut = 5;

    /**
     * @return the path of the file this configuration is based on
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Overrides configuration properties with applicable system properties
     *
     * @return the current configuration
     */
    public Configuration applySystemProperties() {
        Table<String, Object, Field> fieldsByPathAndDeclaringInstance = Fields.listByPathAndDeclaringInstance(this);
        fieldsByPathAndDeclaringInstance.cellSet().forEach(cell -> {
            String value = System.getProperty(cell.getRowKey());
            if (value != null) {
                if (value.contains("\\n")) {
                    value = value.replaceAll("\\\\n", "\n");
                }
                Field field = cell.getValue();
                LOGGER.info("overriding configuration key '" + cell.getRowKey() + "' with '" + value + "'");
                try {
                    if (field.getType().equals(List.class)) {
                        field.set(cell.getColumnKey(), objectMapper().readValue(value, JavaTypes.getParametizedList(field)));
                    } else {
                        field.set(cell.getColumnKey(), objectMapper().readValue(value, field.getType()));
                    }
                } catch (IOException | IllegalArgumentException | IllegalAccessException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
        return this;
    }

    /**
     * Validates the configuration
     *
     * @return the current configuration
     */
    public Configuration validate() {
        Set<ConstraintViolation<Configuration>> violations = validator().validate(this);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder(violations.size() + " error" + (violations.size() > 1 ? "s" : "") + " in configuration file " + filePath);
            violations.forEach(violation -> {
                Path.Node node = Iterators.getLast(violation.getPropertyPath().iterator());
                try {
                    Class<?> type = violation.getLeafBean().getClass().getField(node.getName()).getType();
                    message.append("\n").append(violation.getPropertyPath()).append(" of type ").append(type.getName()).append(" ").append(violation.getMessage());
                } catch (NoSuchFieldException | SecurityException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            });
            throw new ValidationException(message.toString());
        }
        return this;
    }

    public List<Object[]> createWrappersParameters() {
        return createWrappers().stream().map(obj -> new Object[]{obj}).collect(Collectors.toList());
    }

    public List<Object[]> createWrappersParameters(FilterDrivers filterDrivers) {
        return createWrappers().stream()
            .filter(WebDriverWrapper.filter(filterDrivers))
            .map(obj -> new Object[]{obj}).collect(Collectors.toList());
    }

    @SafeVarargs
    public final List<Object[]> createWrappersParameters(Predicate<WebDriverWrapper<?>>... predicates) {
        Stream<WebDriverWrapper<?>> wrappers = createWrappers().stream();
        for (Predicate<WebDriverWrapper<?>> predicate : predicates) {
            wrappers = wrappers.filter(predicate);
        }
        return wrappers.map(obj -> new Object[]{obj}).collect(Collectors.toList());
    }

    public List<WebDriverWrapper<?>> createWrappers() {
        return drivers.stream()
            .map(driverConfiguration -> driverConfiguration.wrappers())
            .peek(wrappers -> WRAPPERS.addAll(wrappers))
            .flatMap(wrappers -> wrappers.stream())
            .filter(WebDriverWrapper.filterFromSystemProperties())
            .peek(driverWrapper -> driverWrapper.setReuseDriverBetweenTests(reuseDriverBetweenTests))
            .collect(Collectors.toList());
    }

    public void stopAllDrivers() {
        WRAPPERS.forEach(driverWrapper -> driverWrapper.stopAllDrivers());
    }

    @Override
    public String toString() {
        try {
            return (filePath != null ? "# " + filePath + "\n" : "") + objectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return super.toString();
    }

    /**
     * @return the last read configuration, if none was read then try to read the default one, then the legacy one
     */
    @SuppressWarnings("null")
    public static Configuration current() {
        if (current == null) {
            Configuration configuration = null;
            String configFile = null;
            try {
                configFile = System.getProperty(CONFIG_SYSTEM_PROPERTY);
                if (configFile == null) {
                    LOGGER.warn("no config provided as a system property");
                    if (TestHome.get() != null) {
                        configFile = TestHome.get() + DEFAULT_LOCAL_CONFIG;
                    }
                }
                configuration = read(configFile);
            } catch (IOException e) {
                LOGGER.error("couldn't read " + e.getMessage());
                try {
                    if (TestHome.get() != null) {
                        configFile = TestHome.get() + DEFAULT_LEGACY_CONFIG;
                        configuration = read(configFile);
                    }
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
            if (configuration == null) {
                throw new RuntimeException("no driver config provided and all the fallbacks failed ...");
            } else {
                LOGGER.info("using " + configFile);
            }
            current = configuration.applySystemProperties().validate();
            // this will close all the drivers as the jvm goes down
            if (current.autoCloseDrivers) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        current.stopAllDrivers();
                    }
                });
            }
        }
        return current;
    }

    public static void setCurrent(Configuration current) {
        Configuration.current = current;
    }

    /**
     * read the file located at filepath
     *
     * @param filepath
     * @return the Configuration
     * @throws IOException
     */
    public static Configuration read(String filepath) throws IOException {
        if (filepath == null) {
            return null;
        }
        return read(new File(filepath));
    }

    /**
     * read the file
     *
     * @param file
     * @return the Configuration
     * @throws IOException
     */
    public static Configuration read(File file) throws IOException {
        String content = Files.toString(file, Charset.defaultCharset()).trim();
        Configuration configuration;
        configuration = from(content);
        configuration.filePath = file.getAbsolutePath();
        return configuration;
    }

    /**
     * Builds a configuration object from a String
     *
     * @param content
     * @return the Configuration
     */
    public static Configuration from(String content) {
        Configuration configuration;
        if (content.startsWith("{")) {
            configuration = fromLegacyJson(content);
        } else {
            configuration = fromYaml(content);
        }
        return configuration;
    }

    /**
     * Builds a configuration object from a YAML String
     *
     * @param yaml
     * @return the Configuration
     */
    public static Configuration fromYaml(String yaml) {
        try {
            return objectMapper().readValue(yaml, Configuration.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Builds a configuration object from a legacy JSON String
     *
     * @param json
     * @return the Configuration
     */
    public static Configuration fromLegacyJson(String json) {
        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        if (jsonObject.has("configuration")) {
            JsonObject jsonConfiguration = jsonObject.getAsJsonObject("configuration");
            Configuration configuration = new Configuration();
            if (jsonConfiguration.has("runOnGrid") && jsonConfiguration.get("runOnGrid").getAsBoolean()) {
                GridConfiguration gridConfiguration = new GridConfiguration();
                if (jsonConfiguration.has("hubIp")) {
                    gridConfiguration.hubIp = jsonConfiguration.get("hubIp").getAsString();
                }
                if (jsonConfiguration.has("enableLiveStream")) {
                    gridConfiguration.enableLiveStream = jsonConfiguration.get("enableLiveStream").getAsBoolean();
                }
                configuration.drivers.add(gridConfiguration);
            } else {
                if (jsonConfiguration.has("usePhantomJS") && jsonConfiguration.get("usePhantomJS").getAsBoolean()) {
                    configuration.drivers.add(new PhantomJSConfiguration());
                }
                if (jsonConfiguration.has("useFirefox") && jsonConfiguration.get("useFirefox").getAsBoolean()) {
                    configuration.drivers.add(new FirefoxConfiguration());
                }
                if (jsonConfiguration.has("useChrome") && jsonConfiguration.get("useChrome").getAsBoolean()) {
                    configuration.drivers.add(new ChromeConfiguration());
                }
                if (jsonConfiguration.has("androidHome")) {
                    configuration.android = new AndroidConfiguration();
                    configuration.android.home = jsonConfiguration.get("androidHome").getAsString();
                    if (jsonConfiguration.has("AndroidBuildtoolsVersion")) {
                        configuration.android.toolsVersion = jsonConfiguration.get("AndroidBuildtoolsVersion").getAsString();
                    }
                }
            }
            if (jsonConfiguration.has("autoCloseDrivers")) {
                configuration.autoCloseDrivers = jsonConfiguration.get("autoCloseDrivers").getAsBoolean();
            }
            if (jsonConfiguration.has("RmReportIP")) {
                configuration.rmReportIP = jsonConfiguration.get("RmReportIP").getAsString();
            }
            if (jsonConfiguration.has("RmReportLivePort")) {
                configuration.rmReportLivePort = jsonConfiguration.get("RmReportLivePort").getAsInt();
            }
            if (jsonConfiguration.has("jsonReportSavePath")) {
                configuration.jsonReportSavePath = jsonConfiguration.get("jsonReportSavePath").getAsString();
            }
            return configuration;
        }
        throw new RuntimeException("config doesn't contain any 'configuration' object\ncontent:\n" + json);
    }

    /**
     * singleton of the YAML ObjectMapper
     *
     * @return the objectMapper
     */
    public static synchronized ObjectMapper objectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper(new YAMLFactory());
            ReflectionsUtils.current().getSubTypesOf(DriverConfiguration.class).stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> objectMapper.registerSubtypes(clazz));
        }
        return objectMapper;
    }

    /**
     * singleton of the Validator
     *
     * @return the validator
     */
    public static synchronized Validator validator() {
        if (validator == null) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        }
        return validator;
    }
}
