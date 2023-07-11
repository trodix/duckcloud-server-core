package com.trodix.duckcloud.security.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component
@RequiredArgsConstructor
public class ConfigResourceLoader {

    private final ResourceLoader resourceLoader;

    public Resource loadResource(String path) throws IOException {
        Resource[] resources;
        resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(path);

        if (resources.length <= 0) {
            throw new IllegalArgumentException(String.format("Path [%s] not found", path));
        }

        return resources[0];
    }

    public String readConfig(String path) throws IOException {
        Resource resource = loadResource(path);

        InputStream is = resource.getInputStream();
        byte[] encoded = IOUtils.toByteArray(is);
        String content = new String(encoded, Charset.forName("UTF-8"));

        return content;
    }

}
