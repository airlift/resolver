package io.airlift.resolver.internal;

import org.sonatype.aether.artifact.Artifact;

import java.io.File;
import java.util.List;

public interface MavenResolver
{
    List<Artifact> resolveArtifacts(Iterable<? extends Artifact> sourceArtifacts);

    List<Artifact> resolvePom(File pomFile);
}
