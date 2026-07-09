package com.os.workshop.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Configuration
@ImportAutoConfiguration(exclude = {
        org.springdoc.core.configuration.SpringDocHateoasConfiguration.class
})
public class OpenApiDocumentationConfig {

    @Bean
    public GlobalOpenApiCustomizer hideSpringDataRestEntityEndpoints() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().entrySet()
                        .removeIf(entry -> isSpringDataRestEntityPath(entry.getKey(), entry.getValue()));
            }

            if (openApi.getTags() != null) {
                openApi.getTags()
                        .removeIf(tag -> isSpringDataRestEntityName(tag.getName()));
            }

            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                removeUnreferencedSchemas(openApi.getPaths(), openApi.getComponents().getSchemas());
            }
        };
    }

    private boolean isSpringDataRestEntityPath(String path, PathItem pathItem) {
        if (isSpringDataRestEntityName(path)) {
            return true;
        }

        return pathItem.readOperations()
                .stream()
                .anyMatch(this::isSpringDataRestEntityOperation);
    }

    private boolean isSpringDataRestEntityOperation(Operation operation) {
        return containsSpringDataRestEntityName(operation.getTags())
                || isSpringDataRestEntityName(operation.getOperationId());
    }

    private boolean containsSpringDataRestEntityName(Collection<String> names) {
        return names != null
                && names.stream()
                .filter(Objects::nonNull)
                .anyMatch(this::isSpringDataRestEntityName);
    }

    private boolean isSpringDataRestEntityName(String name) {
        if (name == null) {
            return false;
        }

        String normalizedName = name.toLowerCase(Locale.ROOT);

        List<String> generatedNameFragments = List.of(
                "entity-controller",
                "-entity-",
                "search-resource",
                "repository-search",
                "profile-controller"
        );

        return generatedNameFragments.stream()
                .anyMatch(normalizedName::contains);
    }

    private void removeUnreferencedSchemas(Object paths, Map<String, Schema> schemas) {
        Set<String> referencedSchemas = new HashSet<>();

        collectSchemaReferences(Json.mapper().valueToTree(paths), referencedSchemas);

        boolean foundNewReference;

        do {
            foundNewReference = false;

            for (String schemaName : Set.copyOf(referencedSchemas)) {
                Schema schema = schemas.get(schemaName);

                if (schema == null) {
                    continue;
                }

                int before = referencedSchemas.size();

                collectSchemaReferences(Json.mapper().valueToTree(schema), referencedSchemas);

                if (referencedSchemas.size() > before) {
                    foundNewReference = true;
                }
            }
        } while (foundNewReference);

        schemas.keySet()
                .removeIf(schemaName -> !referencedSchemas.contains(schemaName));
    }

    private void collectSchemaReferences(JsonNode node, Set<String> referencedSchemas) {
        if (node == null || node.isNull()) {
            return;
        }

        JsonNode reference = node.get("$ref");

        if (reference != null && reference.isTextual()) {
            String referenceValue = reference.asText();
            String schemaPrefix = "#/components/schemas/";

            if (referenceValue.startsWith(schemaPrefix)) {
                referencedSchemas.add(referenceValue.substring(schemaPrefix.length()));
            }
        }

        if (node.isObject() || node.isArray()) {
            node.elements()
                    .forEachRemaining(child -> collectSchemaReferences(child, referencedSchemas));
        }
    }
}
