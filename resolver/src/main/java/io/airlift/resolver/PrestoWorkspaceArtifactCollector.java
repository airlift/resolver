package io.airlift.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

//
// This assumes a version simple reactor model where there is a single parent POM with a set of children. The groupId and
// version are derived from the parent in a naive way.
//
public class PrestoWorkspaceArtifactCollector 
{
    private final File[] poms;
    private final MavenXpp3Reader pomReader;
    private final Map<String, File> artifacts;
    private String currentGroupId;
    private String currentVersion;

    public PrestoWorkspaceArtifactCollector(File... poms) 
    {
        this.poms = poms;
        this.pomReader = new MavenXpp3Reader();
        this.artifacts = collectArtifacts();
    }

    public Map<String, File> artifacts() 
    {
        return artifacts;
    }
  
    public File findArtifact(Artifact artifact) 
    {
        return artifacts.get(key(artifact));
    }
  
    public List<String> findVersions(Artifact artifact) 
    {
        return ImmutableList.of(artifact.getVersion());
    }
  
    private Map<String, File> collectArtifacts() 
    {
        Map<String, File> artifacts = Maps.newHashMap();
        for (File pom : poms) 
        {
            collectArtifacts(pom, artifacts);
        }
        return artifacts;
    }
  
    private void collectArtifacts(File pom, Map<String, File> artifacts) 
    {
        try (InputStream is = new FileInputStream(pom)) 
        {
            Model model = pomReader.read(is);
            if (model.getPackaging().equals("pom")) 
            {
                if(currentGroupId == null && currentVersion == null)
                {
                    currentGroupId = model.getGroupId();
                    currentVersion = model.getVersion();
                }
                for (String module : model.getModules()) {
                    File childPom = new File(new File(pom.getParentFile(), module), "pom.xml");
                    if (childPom.exists()) 
                    {
                        collectArtifacts(childPom, artifacts);
                    }
                }
            } 
            else {
                File targetClasses = new File(pom.getParentFile(), "target/classes");
                if (targetClasses.exists()) 
                {
                    artifacts.put(key(model), targetClasses);
                }
            }
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
    }
  
    private String key(Model model) 
    {
        return currentGroupId + ":" + model.getArtifactId() + ":" + currentVersion;
    }
  
    private String key(Artifact artifact) 
    {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }
}
