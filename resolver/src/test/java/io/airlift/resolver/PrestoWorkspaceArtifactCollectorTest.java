package io.airlift.resolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PrestoWorkspaceArtifactCollectorTest {

    @Test
    public void validateArtifactCollector() throws Exception 
    {
        File sourceReactor = new File(new File("").getAbsolutePath(), "src/test/reactor");
        File targetReactor = new File(new File("").getAbsolutePath(), "target/reactor");
        File pom = new File(targetReactor, "pom.xml");    
        FileUtils.deleteDirectory(targetReactor);
        targetReactor.mkdirs();
        FileUtils.copyDirectoryStructure(sourceReactor, targetReactor);
        makeTargetClasses(targetReactor);
        PrestoWorkspaceArtifactCollector artifactCollector = new PrestoWorkspaceArtifactCollector(pom);
        Map<String, File> artifacts = artifactCollector.artifacts();
        for(String key : artifacts.keySet()) {
          System.out.println(key);
        }        
        String groupId = "com.facebook.presto";
        String version = "0.135-SNAPSHOT";        
        String[] expectedArtifactNames = new String[]
        {
            "presto-base-jdbc",
            "presto-benchmark",
            "presto-benchmark-driver",
            "presto-blackhole",
            "presto-bytecode",
            "presto-cassandra",
            "presto-cli",
            "presto-client",
            // "presto-docs", is a packaging == pom
            "presto-example-http",
            "presto-hive",
            "presto-hive-cdh4",
            "presto-hive-cdh5",
            "presto-hive-hadoop1",
            "presto-hive-hadoop2",
            "presto-jdbc",
            "presto-jmx",
            "presto-kafka",
            "presto-main",
            "presto-ml",
            "presto-mysql",
            "presto-orc",
            "presto-parser",
            "presto-postgresql",
            "presto-product-tests",
            "presto-raptor",
            "presto-record-decoder",
            "presto-redis",
            "presto-server",
            "presto-server-rpm",
            "presto-spi",
            "presto-teradata-functions",
            "presto-testing-server-launcher",
            "presto-tests",
            "presto-tpch"
        };
        
        for(String expectedArtifactName : expectedArtifactNames)
        {
            String key = groupId + ":" + expectedArtifactName + ":" + version;
            Assert.assertTrue(artifacts.containsKey(key), "Expected to find " + key);
            Assert.assertTrue(new File(new File(targetReactor, expectedArtifactName), "target/classes").exists());
        }
    }    
    
    private void makeTargetClasses(File reactor) throws Exception 
    {
        Files.walkFileTree(reactor.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException 
            {
                if (!directory.toFile().getName().endsWith("classes") && directory != reactor.toPath()) 
                {
                    new File(directory.toFile(), "target/classes").mkdirs();
                }            
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
