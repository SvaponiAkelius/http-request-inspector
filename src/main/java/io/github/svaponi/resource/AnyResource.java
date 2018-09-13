package io.github.svaponi.resource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Generic resource to be converted in any JSON object.
 */
public class AnyResource {

    @JsonAnySetter
    private final Map<String, Object> fields = new TreeMap<>();

    public <T> AnyResource withField(final String name, final T value) {
        fields.put(name, value);
        return this;
    }

    public <T> AnyResource withFieldIfValue(final String name, final T value, final Predicate<? super T> predicate) {
        if (predicate.test(value)) {
            fields.put(name, value);
        }
        return this;
    }

    public <T> AnyResource withFieldIfValueNot(final String name, final T value, final Predicate<? super T> predicate) {
        return withFieldIfValue(name, value, predicate.negate());
    }


    @JsonAnyGetter
    public Map<String, Object> fields() {
        return this.fields;
    }
}
