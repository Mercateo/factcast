package org.factcast.server.rest.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.mercateo.common.rest.schemagen.IgnoreInRestSchema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * return object for the resources returning facts to the client. Also used in
 * the new transactions in the payload
 *
 * @author joerg_adler
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactJson {

    @JsonProperty
    @NotNull
    @NonNull
    @Valid
    private Header header;

    @JsonProperty
    @NotNull
    @NonNull
    private JsonNode payload;

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Header {

        @JsonProperty
        @NotNull
        final UUID id;

        @JsonProperty
        @NotNull
        final String ns;

        @JsonProperty
        final String type;

        @JsonProperty
        final Set<UUID> aggIds;

        @JsonProperty
        final Map<String, Object> meta = new HashMap<>();

        @JsonAnySetter
        @IgnoreInRestSchema
        final Map<String, Object> anyOther = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> anyOther() {
            return anyOther;
        }
    }
}
