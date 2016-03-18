package io.airlift.resolver;

import java.io.File;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

public class PrestoWorkspaceReader implements WorkspaceReader {
  
  private final WorkspaceRepository workspaceRepository;
  private final PrestoWorkspaceArtifactCollector artifactCollector;

  public PrestoWorkspaceReader(File... poms) {
    this.workspaceRepository = new WorkspaceRepository();
    this.artifactCollector = new PrestoWorkspaceArtifactCollector(poms);    
  }

  @Override
  public WorkspaceRepository getRepository() {
    return workspaceRepository;
  }

  @Override
  public File findArtifact(Artifact artifact) {
    return artifactCollector.findArtifact(artifact);
  }

  @Override
  public List<String> findVersions(Artifact artifact) {
    return artifactCollector.findVersions(artifact);
  }
}
