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

import com.google.common.collect.ImmutableList;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.airlift.resolver.ArtifactResolver.MAVEN_CENTRAL_URI;
import static io.airlift.resolver.ArtifactResolver.USER_LOCAL_REPO;
import static org.testng.Assert.assertTrue;

public class ArtifactResolverTest
{
    @Test
    public void testResolveArtifacts()
            throws Exception
    {
        ArtifactResolver artifactResolver = new ArtifactResolver(USER_LOCAL_REPO, MAVEN_CENTRAL_URI);
        List<Artifact> artifacts = artifactResolver.resolveArtifacts(ImmutableList.of(new DefaultArtifact("org.apache.maven:maven-core:3.0.4")));

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

        ArtifactResolver artifactResolver = new ArtifactResolver(USER_LOCAL_REPO, MAVEN_CENTRAL_URI);
        List<Artifact> artifacts = artifactResolver.resolvePom(pomFile);

        Assert.assertNotNull(artifacts, "artifacts is null");
        for (Artifact artifact : artifacts) {
            Assert.assertNotNull(artifact.getFile(), "Artifact " + artifact + " is not resolved");
        }
    }

    @Test
    public void testResolveSiblingModule()
    {
        ArtifactResolver artifactResolver = new ArtifactResolver(USER_LOCAL_REPO, MAVEN_CENTRAL_URI);
        List<Artifact> artifacts = artifactResolver.resolvePom(new File("src/test/poms/multi-module-project/module2/pom.xml"));
        List<File> files = artifacts.stream()
                .map(Artifact::getFile)
                .filter(Objects::nonNull)
                .map(File::getAbsoluteFile)
                .collect(toImmutableList());

        assertTrue(files.contains(new File("src/test/poms/multi-module-project/module2/target/classes").getAbsoluteFile()));
        assertTrue(files.contains(new File("src/test/poms/multi-module-project/module1/target/classes").getAbsoluteFile()));
    }
}
