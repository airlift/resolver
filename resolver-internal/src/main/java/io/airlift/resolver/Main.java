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
