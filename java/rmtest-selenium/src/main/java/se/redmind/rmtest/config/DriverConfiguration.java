package se.redmind.rmtest.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.redmind.rmtest.WebDriverWrapper;

/**
 * @author Jeremy Comte
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class DriverConfiguration<WebDriverType extends WebDriver> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DesiredCapabilities baseCapabilities;
    private List<WebDriverWrapper<WebDriverType>> wrappers;

    @JsonProperty
    public String description;

    @JsonProperty("capabilities")
    public Map<String, Object> configurationCapabilities = new LinkedHashMap<>();

    protected DriverConfiguration(DesiredCapabilities baseCapabilities) {
        this.baseCapabilities = baseCapabilities;
    }

    @SuppressWarnings("unchecked")
    public <SubType extends DriverConfiguration<?>> SubType as(Class<SubType> clazz) {
        return (SubType) this;
    }

    @SuppressWarnings("unchecked")
    public DesiredCapabilities generateCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities(baseCapabilities);
        configurationCapabilities.forEach((key, value) -> {
            Object currentValue = capabilities.getCapability(key);
            if (currentValue == null) {
                capabilities.setCapability(key, value);
            } else if (currentValue instanceof Collection && value instanceof Collection) {
                ((Collection) currentValue).addAll((Collection) value);
            } else if (currentValue instanceof Map && value instanceof Map) {
                ((Map) currentValue).putAll((Map) value);
            } else if (currentValue.getClass().equals(value.getClass())) {
                capabilities.setCapability(key, value);
            } else {
                throw new RuntimeException("can't override or merge " + currentValue + " with " + value);
            }
        });
        return capabilities;
    }

    public String generateDescription() {
        if (description != null) {
            return description;
        }
        return this.getClass().getSimpleName().replaceAll("Configuration", "");
    }

    public List<WebDriverWrapper<WebDriverType>> wrappers() {
        if (wrappers == null) {
            wrappers = createDrivers();
        }
        return wrappers;
    }

    protected abstract List<WebDriverWrapper<WebDriverType>> createDrivers();

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[");
        if (wrappers != null) {
            wrappers.forEach(wrapper -> stringBuilder.append(wrapper.getDescription()).append(", "));
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }
        return stringBuilder.append("]").toString();
    }
}
