package io.airlift.resolver.internal.aether;

import io.airlift.resolver.internal.MavenResolver;
import io.airlift.resolver.internal.MavenResolverFactory;

import java.util.List;

public class AetherMavenResolverFactory
        implements MavenResolverFactory
{
    @Override
    public MavenResolver createMavenResolver(String localRepositoryDir, List<String> remoteRepositoryUris)
    {
        return new AetherMavenResolver(localRepositoryDir, remoteRepositoryUris);
    }
}
