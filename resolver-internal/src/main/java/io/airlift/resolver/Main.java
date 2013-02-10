package io.airlift.resolver;

import org.sonatype.aether.artifact.Artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.airlift.resolver.ArtifactResolver.MAVEN_CENTRAL_URI;
import static io.airlift.resolver.ArtifactResolver.USER_LOCAL_REPO;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("java -cp ...  "  + Main.class.getName() + " GAV_OR_POM");
            System.exit(1);
        }

        String localRepo = System.getProperty("maven.repo.local", USER_LOCAL_REPO);

        String remoteReposString = System.getProperty("maven.repo.remote", MAVEN_CENTRAL_URI);
        List<String> remoteRepos = new ArrayList<>();
        for (String repo : remoteReposString.split(",")) {
            remoteRepos.add(repo.trim());
        }

        ArtifactResolver artifactResolver = new ArtifactResolver(localRepo, remoteRepos);

        File pomFile = new File(args[0]);
        List<Artifact> artifacts;
        if (pomFile.canRead()) {
            artifacts = artifactResolver.resolvePom(pomFile);
        }
        else {
            artifacts = artifactResolver.resolveArtifacts(new DefaultArtifact(args[0]));
        }

        for (Artifact artifact : artifacts) {
            if (artifact.getFile() != null) {
                System.out.println("Resolved " + artifact + " to " + artifact.getFile());
            }
        }

        for (Artifact artifact : artifacts) {
            if (artifact.getFile() == null) {
                System.out.println("Could not resolved " + artifact);
            }
        }
    }
}
