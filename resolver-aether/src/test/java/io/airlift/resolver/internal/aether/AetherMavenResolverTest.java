package io.airlift.resolver.internal.aether;

import com.google.common.collect.ImmutableList;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

public class AetherMavenResolverTest
{
    public static final String USER_LOCAL_REPO = System.getProperty("user.home") + "/.m2/repository";
    public static final String MAVEN_CENTRAL_URI = "http://repo1.maven.org/maven2/";

    @Test
    public void testResolveArtifacts()
            throws Exception
    {
        AetherMavenResolver aetherMavenResolver = new AetherMavenResolver(USER_LOCAL_REPO, MAVEN_CENTRAL_URI);
        List<Artifact> artifacts = aetherMavenResolver.resolveArtifacts(ImmutableList.of(new DefaultArtifact("org.apache.maven:maven-core:3.0.4")));

        Assert.assertNotNull(artifacts, "artifacts is null");
        for (Artifact artifact : artifacts) {
            Assert.assertNotNull(artifact.getFile(), "Artifact " + artifact + " is not resolved");
        }
    }

    @Test
    public void testResolvePom()
            throws DependencyResolutionException
    {
        File pomFile = new File("src/test/poms/maven-core-3.0.4.pom");
        Assert.assertTrue(pomFile.canRead());

        AetherMavenResolver aetherMavenResolver = new AetherMavenResolver(USER_LOCAL_REPO, MAVEN_CENTRAL_URI);
        List<Artifact> artifacts = aetherMavenResolver.resolvePom(pomFile);

        Assert.assertNotNull(artifacts, "artifacts is null");
        for (Artifact artifact : artifacts) {
            Assert.assertNotNull(artifact.getFile(), "Artifact " + artifact + " is not resolved");
        }
    }
}
