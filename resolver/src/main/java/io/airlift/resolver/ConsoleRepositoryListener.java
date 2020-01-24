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

import io.airlift.log.Logger;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;

/**
 * A simplistic repository listener that logs events to the console.
 */
class ConsoleRepositoryListener
        extends AbstractRepositoryListener
{
    private static final Logger log = Logger.get(ConsoleRepositoryListener.class);

    @Override
    public void artifactDeployed(RepositoryEvent event)
    {
        log.debug("Deployed %s to %s", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactDeploying(RepositoryEvent event)
    {
        log.debug("Deploying %s to %s", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactDescriptorInvalid(RepositoryEvent event)
    {
        log.debug("Invalid artifact descriptor for %s: %s", event.getArtifact(), event.getException().getMessage());
    }

    @Override
    public void artifactDescriptorMissing(RepositoryEvent event)
    {
        log.debug("Missing artifact descriptor for %s", event.getArtifact());
    }

    @Override
    public void artifactInstalled(RepositoryEvent event)
    {
        log.debug("Installed %s to %s", event.getArtifact(), event.getFile());
    }

    @Override
    public void artifactInstalling(RepositoryEvent event)
    {
        log.debug("Installing %s to %s", event.getArtifact(), event.getFile());
    }

    @Override
    public void artifactResolved(RepositoryEvent event)
    {
        log.debug("Resolved artifact %s from %s", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactDownloading(RepositoryEvent event)
    {
        log.debug("Downloading artifact %s from %s", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactDownloaded(RepositoryEvent event)
    {
        log.debug("Downloaded artifact %s from %s", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactResolving(RepositoryEvent event)
    {
        log.debug("Resolving artifact %s", event.getArtifact());
    }

    @Override
    public void metadataDeployed(RepositoryEvent event)
    {
        log.debug("Deployed %s to %s", event.getMetadata(), event.getRepository());
    }

    @Override
    public void metadataDeploying(RepositoryEvent event)
    {
        log.debug("Deploying %s to %s", event.getMetadata(), event.getRepository());
    }

    @Override
    public void metadataInstalled(RepositoryEvent event)
    {
        log.debug("Installed %s to %s", event.getMetadata(), event.getFile());
    }

    @Override
    public void metadataInstalling(RepositoryEvent event)
    {
        log.debug("Installing %s to %s", event.getMetadata(), event.getFile());
    }

    @Override
    public void metadataInvalid(RepositoryEvent event)
    {
        log.debug("Invalid metadata %s", event.getMetadata());
    }

    @Override
    public void metadataResolved(RepositoryEvent event)
    {
        log.debug("Resolved metadata %s from %s", event.getMetadata(), event.getRepository());
    }

    @Override
    public void metadataResolving(RepositoryEvent event)
    {
        log.debug("Resolving metadata %s from %s", event.getMetadata(), event.getRepository());
    }
}
