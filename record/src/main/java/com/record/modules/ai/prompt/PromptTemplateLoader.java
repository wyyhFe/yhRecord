package com.record.modules.ai.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptTemplateLoader {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String path) {
        return cache.computeIfAbsent(path, this::readResource);
    }

    public String resolve(String overrideValue, String fallbackPath) {
        return StringUtils.hasText(overrideValue) ? overrideValue.trim() : load(fallbackPath);
    }

    private String readResource(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Prompt template not found: " + path);
        }
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load prompt template: " + path, ex);
        }
    }
}
