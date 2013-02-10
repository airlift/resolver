package io.airlift.resolver.internal;

import java.util.List;

public interface MavenResolverFactory
{
    public MavenResolver createMavenResolver(String localRepositoryDir, List<String> remoteRepositoryUris);
}
