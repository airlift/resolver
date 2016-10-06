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


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;

import com.google.common.collect.ImmutableList;

import io.airlift.resolver.internal.ConsoleRepositoryListener;
import io.airlift.resolver.internal.ConsoleTransferListener;
import io.airlift.resolver.internal.Slf4jLoggerManager;

public class ArtifactResolver
{
    public static final String USER_LOCAL_REPO = System.getProperty("user.home") + "/.m2/repository";
    public static final String MAVEN_CENTRAL_URI = "http://repo1.maven.org/maven2/";

    private final RepositorySystem repositorySystem;
    private final MavenRepositorySystemSession repositorySystemSession;
    private final List<RemoteRepository> repositories;

    public ArtifactResolver(String localRepositoryDir, String... remoteRepositoryUris)
    {
        this(localRepositoryDir, Arrays.asList(remoteRepositoryUris));
    }

    public ArtifactResolver(String localRepositoryDir, List<String> remoteRepositoryUris)
    {
        MavenServiceLocator locator = new MavenServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        locator.addService(RepositoryConnectorFactory.class, AsyncRepositoryConnectorFactory.class);
        repositorySystem = locator.getService(RepositorySystem.class);

        repositorySystemSession = new MavenRepositorySystemSession();

        LocalRepositoryManager localRepositoryManager = new SimpleLocalRepositoryManager(localRepositoryDir);
        repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);

        repositorySystemSession.setTransferListener(new ConsoleTransferListener());
        repositorySystemSession.setRepositoryListener(new ConsoleRepositoryListener());

        List<RemoteRepository> repositories = new ArrayList<>(remoteRepositoryUris.size());
        int index = 0;
        for (String repositoryUri : remoteRepositoryUris) {
            repositories.add(new RemoteRepository("repo-" + index++, "default", repositoryUri));
        }
        this.repositories = Collections.unmodifiableList(repositories);
    }

    public List<Artifact> resolveArtifacts(Artifact... sourceArtifacts)
    {
        return resolveArtifacts(Arrays.asList(sourceArtifacts));
    }

    public List<Artifact> resolveArtifacts(Iterable<? extends Artifact> sourceArtifacts)
    {
        CollectRequest collectRequest = new CollectRequest();
        for (Artifact sourceArtifact : sourceArtifacts) {
            collectRequest.addDependency(new Dependency(sourceArtifact, JavaScopes.RUNTIME));
        }
        for (RemoteRepository repository : repositories) {
            collectRequest.addRepository(repository);
        }

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME));

        return resolveArtifacts(dependencyRequest);
    }

    public List<Artifact> resolvePom(File pomFile)
    {
        if (pomFile == null) {
            throw new RuntimeException("pomFile is null");
        }

        MavenProject pom;
        try {
            PlexusContainer container = container();
            org.apache.maven.repository.RepositorySystem lrs = container.lookup(org.apache.maven.repository.RepositorySystem.class);
            ProjectBuilder projectBuilder = container.lookup(ProjectBuilder.class);
            ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
            request.setRepositorySession(repositorySystemSession);
            request.setProcessPlugins(false);
            request.setLocalRepository(lrs.createDefaultLocalRepository());
            request.setRemoteRepositories(Arrays.asList(new ArtifactRepository[]{lrs.createDefaultRemoteRepository()}.clone()));
            ProjectBuildingResult result = projectBuilder.build(pomFile, request);
            pom = result.getProject();
        }
        catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error loading pom: " + pomFile.getAbsolutePath(), e);
        }


        Artifact rootArtifact = new DefaultArtifact(pom.getArtifact().getGroupId(),
                pom.getArtifact().getArtifactId(),
                pom.getArtifact().getClassifier(),
                pom.getArtifact().getType(),
                pom.getArtifact().getVersion(),
                null,
                new File(pom.getModel().getBuild().getOutputDirectory()));

        CollectRequest collectRequest = new CollectRequest();
        for (org.apache.maven.model.Dependency dependency : pom.getDependencies()) {
            collectRequest.addDependency(toAetherDependency(dependency));
        }
        for (RemoteRepository repository : pom.getRemoteProjectRepositories()) {
            collectRequest.addRepository(repository);
        }
        for (RemoteRepository repository : repositories) {
            collectRequest.addRepository(repository);
        }
        
        // Make sure we account for managed dependencies
        if(pom.getDependencyManagement() != null)
        {
            for(org.apache.maven.model.Dependency managedDependency : pom.getDependencyManagement().getDependencies())
            {
                collectRequest.addManagedDependency(toAetherDependency(managedDependency));
            }
        }

        // Make sure we account for managed dependencies
        if (pom.getDependencyManagement() != null) {
            for (org.apache.maven.model.Dependency managedDependency : pom.getDependencyManagement().getDependencies()) {
                collectRequest.addManagedDependency(toAetherDependency(managedDependency));
            }
        }

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME));
        List<Artifact> artifacts = resolveArtifacts(dependencyRequest);
        return ImmutableList.<Artifact>builder().add(rootArtifact).addAll(artifacts).build();
    }

    private Dependency toAetherDependency(org.apache.maven.model.Dependency dependency)
    {
        Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependency.getVersion());
        ImmutableList.Builder<Exclusion> exclusions = ImmutableList.builder();
        for (org.apache.maven.model.Exclusion exclusion : dependency.getExclusions()) {
            exclusions.add(new Exclusion(exclusion.getGroupId(), exclusion.getArtifactId(), null, "*"));
        }
        return new Dependency(artifact, dependency.getScope(), dependency.isOptional(), exclusions.build());
    }

    private List<Artifact> resolveArtifacts(DependencyRequest dependencyRequest)
    {
        DependencyResult dependencyResult;
        try {
            dependencyResult = repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        }
        catch (DependencyResolutionException e) {
            dependencyResult = e.getResult();
        }
        List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
        List<Artifact> artifacts = new ArrayList<>(artifactResults.size());
        for (ArtifactResult artifactResult : artifactResults) {
            if (artifactResult.isMissing()) {
                artifacts.add(artifactResult.getRequest().getArtifact());
            }
            else {
                artifacts.add(artifactResult.getArtifact());
            }
        }

        return Collections.unmodifiableList(artifacts);
    }

    private static PlexusContainer container()
    {
        try {
            ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());

            ContainerConfiguration cc = new DefaultContainerConfiguration()
                    .setClassWorld(classWorld)
                    .setRealm(null)
                    .setName("maven");

            DefaultPlexusContainer container = new DefaultPlexusContainer(cc);

            // NOTE: To avoid inconsistencies, we'll use the Thread context class loader exclusively for lookups
            container.setLookupRealm(null);

            container.setLoggerManager(new Slf4jLoggerManager());
            container.getLoggerManager().setThresholds(Logger.LEVEL_INFO);

            return container;
        }
        catch (PlexusContainerException e) {
            throw new RuntimeException("Error loading Maven system", e);
        }
    }
}
