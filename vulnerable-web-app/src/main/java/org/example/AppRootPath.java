package org.example;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AppRootPath
{

    private final ResourceLoader resourceLoader;

    public AppRootPath(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public Path getRootPath()
    {
        try
        {
            return Paths.get(resourceLoader.getResource("classpath:/static/").getURI()).toAbsolutePath().normalize();
        } catch (Exception e)
        {
            throw new RuntimeException("Unable to determine root path", e);
        }
    }
}