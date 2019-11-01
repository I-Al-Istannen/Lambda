package com.jvmrally.lambda.command.utility.javadoc.docs;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jvmrally.lambda.command.utility.javadoc.JavadocSelector;
import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.resolving.CachingDocumentResolver;
import de.ialistannen.htmljavadocparser.resolving.CachingDocumentResolver.SimpleCache;
import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.List;

/**
 * An API helper to interact with Javadoc websites.
 */
public class DocsApi {

    private final JavadocApi javadocApi;

    /**
     * Creates a new docs API.
     */
    public DocsApi() {
        this.javadocApi = new JavadocApi();

        SimpleCache<String, Document> cache = new SimpleCache<>() {
            private Cache<String, Document> cache = Caffeine.newBuilder()
                    .<String, Document>weigher((key, value) -> key.length() + value.outerHtml().length())
                    // maximum char count. One char is 2 byte, let's use a maximum of 50 MB
                    // 50 * 1024 * 1024 / 2
                    // MB    KB      B
                    .maximumWeight(50 * 1024 * 1024 / 2)
                    .build();

            @Override
            public void put(String key, Document value) {
                cache.put(key, value);
            }

            @Override
            public Document get(String key) {
                return cache.getIfPresent(key);
            }
        };

        List<JavadocEntry> javadocSources = Arrays.asList(
                new JavadocEntry("https://docs.oracle.com/en/java/javase/12/docs/api", "/allclasses-index.html"),
                new JavadocEntry("https://openjfx.io/javadoc/12", "/allclasses.html")
        );

        for (JavadocEntry javadocEntry : javadocSources) {
            String baseUrl = javadocEntry.getBaseUrl();

            javadocApi.addApi(
                    baseUrl,
                    javadocEntry.getAllClassesAppendix(),
                    new CachingDocumentResolver(
                            new JfxDocumentResolver(baseUrl), cache
                    )
            );
        }
    }

    /**
     * Finds all classes matching the given javadoc selector.
     *
     * @param selector the selector
     * @return all classes for it
     */
    public List<? extends JavadocElement> find(JavadocSelector selector) {
        return selector.select(javadocApi);
    }

    private static class JavadocEntry {
        private String baseUrl;
        private String allClassesAppendix;

        JavadocEntry(String baseUrl, String allClassesAppendix) {
            this.baseUrl = baseUrl;
            this.allClassesAppendix = allClassesAppendix;
        }

        String getBaseUrl() {
            return baseUrl;
        }

        String getAllClassesAppendix() {
            return allClassesAppendix;
        }
    }
}
