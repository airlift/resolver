package io.airlift.resolver.internal.aether;

import com.google.common.collect.ImmutableList;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static io.airlift.resolver.ArtifactResolver.MAVEN_CENTRAL_URI;

public class AetherMavenResolverTest
{
    @Test
    public void testResolveArtifacts()
            throws Exception
    {
        AetherMavenResolver aetherMavenResolver = new AetherMavenResolver("target/local-repo", MAVEN_CENTRAL_URI);
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

        AetherMavenResolver aetherMavenResolver = new AetherMavenResolver("target/local-repo", MAVEN_CENTRAL_URI);
        List<Artifact> artifacts = aetherMavenResolver.resolvePom(pomFile);

        Assert.assertNotNull(artifacts, "artifacts is null");
        for (Artifact artifact : artifacts) {
            Assert.assertNotNull(artifact.getFile(), "Artifact " + artifact + " is not resolved");
        }
    }
}
