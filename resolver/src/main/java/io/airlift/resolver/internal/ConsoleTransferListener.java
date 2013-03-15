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
package io.airlift.resolver.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferResource;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A simplistic transfer listener that logs uploads/downloads.
 */
public class ConsoleTransferListener
        extends AbstractTransferListener
{
    private final Logger logger;

    public ConsoleTransferListener()
    {
        this(LoggerFactory.getLogger(ConsoleTransferListener.class));
    }

    public ConsoleTransferListener(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void transferInitiated(TransferEvent event)
    {
        String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

        logger.debug(message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
    }

    @Override
    public void transferProgressed(TransferEvent event)
    {
    }

    @Override
    public void transferSucceeded(TransferEvent event)
    {
        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if (contentLength >= 0) {
            String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
            String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

            String throughput = "";
            long duration = System.currentTimeMillis() - resource.getTransferStartTime();
            if (duration > 0) {
                DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
                double kbPerSec = (contentLength / 1024.0) / (duration / 1000.0);
                throughput = " at " + format.format(kbPerSec) + " KB/sec";
            }

            logger.debug(type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len + throughput + ")");
        }
    }

    @Override
    public void transferFailed(TransferEvent event)
    {
        logger.debug("transfer failed", event.getException());
    }

    public void transferCorrupted(TransferEvent event)
    {
        logger.debug("transfer corrupted", event.getException());
    }

    protected long toKB(long bytes)
    {
        return (bytes + 1023) / 1024;
    }
}
