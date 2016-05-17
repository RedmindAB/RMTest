package se.redmind.rmtest.config;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jeremy Comte
 */
public class AndroidConfiguration {

    @JsonProperty
    @NotNull
    public String home;

    @JsonProperty
    public String toolsVersion;

}
