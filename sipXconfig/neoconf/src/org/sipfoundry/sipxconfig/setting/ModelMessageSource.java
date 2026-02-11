package org.sipfoundry.sipxconfig.setting;

import java.io.File;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Uses Spring's ResourceLoader strategy (file: or classpath:) 
 * which is much more robust in Jetty on modern Linux distros.
 */
public class ModelMessageSource extends ReloadableResourceBundleMessageSource {

    public ModelMessageSource(File modelFile) {
        setBasename(getBundleFileUri(modelFile));
        setDefaultEncoding("UTF-8");
        setFallbackToSystemLocale(true);
    }

    private String getBundleFileUri(File modelFile) {
        // This assumes the .properties file is next to the .xml file
        String pathWithoutExtension = modelFile.getAbsolutePath().replace(".xml", "");
        return "file:" + pathWithoutExtension;
    }
}