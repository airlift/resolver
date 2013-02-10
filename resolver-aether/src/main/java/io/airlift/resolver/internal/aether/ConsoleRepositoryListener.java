package io.airlift.resolver.internal.aether;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener
        extends AbstractRepositoryListener
{
    private final Logger logger;

    public ConsoleRepositoryListener()
    {
        this(LoggerFactory.getLogger(ConsoleRepositoryListener.class));
    }

    public ConsoleRepositoryListener(Logger logger)
    {
        this.logger = logger;
    }

    public void artifactDeployed(RepositoryEvent event)
    {
        logger.debug("Deployed " + event.getArtifact() + " to " + event.getRepository());
    }

    public void artifactDeploying(RepositoryEvent event)
    {
        logger.debug("Deploying " + event.getArtifact() + " to " + event.getRepository());
    }

    public void artifactDescriptorInvalid(RepositoryEvent event)
    {
        logger.debug("Invalid artifact descriptor for " + event.getArtifact() + ": " + event.getException().getMessage());
    }

    public void artifactDescriptorMissing(RepositoryEvent event)
    {
        logger.debug("Missing artifact descriptor for " + event.getArtifact());
    }

    public void artifactInstalled(RepositoryEvent event)
    {
        logger.debug("Installed " + event.getArtifact() + " to " + event.getFile());
    }

    public void artifactInstalling(RepositoryEvent event)
    {
        logger.debug("Installing " + event.getArtifact() + " to " + event.getFile());
    }

    public void artifactResolved(RepositoryEvent event)
    {
        logger.debug("Resolved artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    public void artifactDownloading(RepositoryEvent event)
    {
        logger.debug("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    public void artifactDownloaded(RepositoryEvent event)
    {
        logger.debug("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository());
    }

    public void artifactResolving(RepositoryEvent event)
    {
        logger.debug("Resolving artifact " + event.getArtifact());
    }

    public void metadataDeployed(RepositoryEvent event)
    {
        logger.debug("Deployed " + event.getMetadata() + " to " + event.getRepository());
    }

    public void metadataDeploying(RepositoryEvent event)
    {
        logger.debug("Deploying " + event.getMetadata() + " to " + event.getRepository());
    }

    public void metadataInstalled(RepositoryEvent event)
    {
        logger.debug("Installed " + event.getMetadata() + " to " + event.getFile());
    }

    public void metadataInstalling(RepositoryEvent event)
    {
        logger.debug("Installing " + event.getMetadata() + " to " + event.getFile());
    }

    public void metadataInvalid(RepositoryEvent event)
    {
        logger.debug("Invalid metadata " + event.getMetadata());
    }

    public void metadataResolved(RepositoryEvent event)
    {
        logger.debug("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
    }

    public void metadataResolving(RepositoryEvent event)
    {
        logger.debug("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
    }
}
