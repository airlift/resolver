/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.resolver;

import io.airlift.resolver.internal.MavenResolver;
import io.airlift.resolver.internal.MavenResolverFactory;
import org.sonatype.aether.artifact.Artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ArtifactResolver
{
    public static final String USER_LOCAL_REPO = System.getProperty("user.home") + "/.m2/repository";
    public static final String MAVEN_CENTRAL_URI = "http://repo1.maven.org/maven2/";

    private static final URLClassLoader INTERAL_CLASS_LOADER = loadInternalClassLoader(ArtifactResolver.class);
    private static final MavenResolverFactory MAVEN_RESOLVER_FACTORY = loadMavenResolverFactory();

    private final MavenResolver mavenResolver;

    public ArtifactResolver(String localRepositoryDir, String... remoteRepositoryUris)
    {
        this(localRepositoryDir, Arrays.asList(remoteRepositoryUris));
    }

    public ArtifactResolver(String localRepositoryDir, List<String> remoteRepositoryUris)
    {
        try (ThreadContextClassLoader threadContextClassLoader = new ThreadContextClassLoader(INTERAL_CLASS_LOADER)) {
            mavenResolver = MAVEN_RESOLVER_FACTORY.createMavenResolver(localRepositoryDir, remoteRepositoryUris);
        }
    }

    public List<Artifact> resolveArtifacts(Artifact... sourceArtifacts)
    {
        try (ThreadContextClassLoader threadContextClassLoader = new ThreadContextClassLoader(INTERAL_CLASS_LOADER)) {
            return mavenResolver.resolveArtifacts(Arrays.asList(sourceArtifacts));
        }
    }

    public List<Artifact> resolveArtifacts(Iterable<? extends Artifact> sourceArtifacts)
    {
        try (ThreadContextClassLoader threadContextClassLoader = new ThreadContextClassLoader(INTERAL_CLASS_LOADER)) {
            return mavenResolver.resolveArtifacts(sourceArtifacts);
        }
    }

    public List<Artifact> resolvePom(File pomFile)
    {
        try (ThreadContextClassLoader threadContextClassLoader = new ThreadContextClassLoader(INTERAL_CLASS_LOADER)) {
            return mavenResolver.resolvePom(pomFile);
        }
    }

    private static MavenResolverFactory loadMavenResolverFactory()
    {
        try (ThreadContextClassLoader threadContextClassLoader = new ThreadContextClassLoader(INTERAL_CLASS_LOADER)) {
            ServiceLoader<MavenResolverFactory> mavenResolvers = ServiceLoader.load(MavenResolverFactory.class, INTERAL_CLASS_LOADER);
            Iterator<MavenResolverFactory> iterator = mavenResolvers.iterator();
            if (!iterator.hasNext()) {
                throw new RuntimeException("Could not load " + MavenResolverFactory.class.getSimpleName());
            }
            MavenResolverFactory mavenResolverFactory = iterator.next();
            if (iterator.hasNext()) {
                throw new RuntimeException("Expected only one " + MavenResolverFactory.class.getSimpleName() + " instance");
            }

            return mavenResolverFactory;
        }
    }

    private static URLClassLoader loadInternalClassLoader(Class<?> baseClass)
    {
        List<URL> urls;
        try {
            URL url = baseClass.getResource(baseClass.getSimpleName() + ".class");
            JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
            JarFile jarFile = urlConnection.getJarFile();
            urls = new ArrayList<>();
            for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
                if (!jarEntry.isDirectory() && jarEntry.getName().startsWith("io/airlift/resolver/internal/deps/")) {
                    String name = jarEntry.getName();
                    name = name.substring(name.lastIndexOf('/') + 1);
                    if (name.endsWith(".jar")) {
                        name = name.substring(0, name.length() - ".jar".length());
                    }
                    try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                        File file = copyToTempFile(inputStream, name);
                        urls.add(file.toURL());
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error loading isolated jars", e);
        }

        return new BasicChildFirstClassLoader(urls, baseClass);
    }

    private static File copyToTempFile(InputStream inputStream, String name)
            throws IOException
    {
        File tempFile = File.createTempFile(name, ".jar");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            while (true) {
                int size = inputStream.read(buffer);
                if (size == -1) {
                    break;
                }
                outputStream.write(buffer, 0, size);
            }
        }
        return tempFile;
    }

    private static class BasicChildFirstClassLoader
            extends URLClassLoader
    {
        public BasicChildFirstClassLoader(List<URL> urls, Class<?> baseClass)
        {
            super(urls.toArray(new URL[urls.size()]), baseClass.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException
        {
            // grab the magic lock
            synchronized (getClassLoadingLock(name)) {

                // first check if this class loader has the class
                // we should not do this for classes starting with "java.", but
                // this is only used for our own custom built jar which isn't
                // doing anything trick like that
                Class clazz = findLoadedClass(name);
                if (clazz == null) {
                    try {
                        clazz = findClass(name);
                    }
                    catch (ClassNotFoundException ignored) {
                        // class loaders were not designed for child first, so this class will throw an exception
                    }
                }

                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }

                // we didn't find the class, so delegate to the parent
                // this will result in a "double" search, but it is the
                // only safe way to load the classes
                return super.loadClass(name, resolve);
            }
        }
    }

    private static class ThreadContextClassLoader
            implements AutoCloseable
    {
        private final ClassLoader originalThreadContextClassLoader;

        private ThreadContextClassLoader(ClassLoader newThreadContextClassLoader)
        {
            this.originalThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(newThreadContextClassLoader);
        }

        @Override
        public void close()
        {
            Thread.currentThread().setContextClassLoader(originalThreadContextClassLoader);
        }
    }
}
