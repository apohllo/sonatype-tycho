package org.sonatype.tycho.p2.impl.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.tycho.p2.impl.resolver.DuplicateReactorIUsException;
import org.sonatype.tycho.p2.impl.resolver.P2ResolverImpl;
import org.sonatype.tycho.p2.resolver.P2ResolutionResult;
import org.sonatype.tycho.p2.resolver.P2Resolver;
import org.sonatype.tycho.test.util.HttpServer;

public class P2ResolverImplTest
{
    private HttpServer server;

    @After
    public void stopHttpServer()
        throws Exception
    {
        HttpServer _server = server;
        server = null;
        if ( _server != null )
        {
            _server.stop();
        }
    }

    @Test
    public void basic()
        throws Exception
    {
        P2ResolverImpl impl = new P2ResolverImpl();
        impl.setRepositoryCache( new P2RepositoryCacheImpl() );
        impl.setLocalRepositoryLocation( getLocalRepositoryLocation() );
        impl.addP2Repository( new File( "resources/repositories/e342" ).getCanonicalFile().toURI() );
        impl.setLogger( new NullP2Logger() );

        File bundle = new File( "resources/resolver/bundle01" ).getCanonicalFile();
        String groupId = "org.sonatype.tycho.p2.impl.resolver.test.bundle01";
        String artifactId = "org.sonatype.tycho.p2.impl.resolver.test.bundle01";
        String version = "1.0.0-SNAPSHOT";

        impl.setEnvironments( getEnvironments() );
        impl.addMavenArtifact( new ArtifactMock( bundle, groupId, artifactId, version, P2Resolver.TYPE_ECLIPSE_PLUGIN ) );

        List<P2ResolutionResult> results = impl.resolveProject( bundle );

        impl.stop();

        Assert.assertEquals( 1, results.size() );
        P2ResolutionResult result = results.get( 0 );

        Assert.assertEquals( 2, result.getArtifacts().size() );
        Assert.assertEquals( 1, result.getInstallableUnits().size() );
    }

    @Test
    public void offline()
        throws Exception
    {
        server = HttpServer.startServer();
        String url = server.addServer( "e342", new File( "resources/repositories/e342" ) );

        // prime local repository
        P2ResolverImpl impl = new P2ResolverImpl();
        impl.setLogger( new NullP2Logger() );
        resolveFromHttp( impl, url );

        // now go offline and resolve again
        impl = new P2ResolverImpl();
        impl.setLogger( new NullP2Logger() );
        impl.setOffline( true );
        List<P2ResolutionResult> results = resolveFromHttp( impl, url );

        Assert.assertEquals( 1, results.size() );
        P2ResolutionResult result = results.get( 0 );

        Assert.assertEquals( 2, result.getArtifacts().size() );
        Assert.assertEquals( 1, result.getInstallableUnits().size() );
    }

    @Test
    public void offlineNoLocalCache()
        throws Exception
    {
        server = HttpServer.startServer();
        String url = server.addServer( "e342", new File( "resources/repositories/e342" ) );

        delete( getLocalRepositoryLocation() );

        P2ResolverImpl impl = new P2ResolverImpl();
        impl.setOffline( true );

        try
        {
            resolveFromHttp( impl, url );
            Assert.fail();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            // TODO better assertion
        }
    }

    @Test
    public void siteConflictingDependenciesResolver()
        throws IOException
    {
        P2ResolverImpl impl = new P2ResolverImpl();
        impl.setRepositoryCache( new P2RepositoryCacheImpl() );
        impl.setLocalRepositoryLocation( getLocalRepositoryLocation() );
        impl.addP2Repository( new File( "resources/repositories/e342" ).getCanonicalFile().toURI() );
        impl.setLogger( new NullP2Logger() );

        impl.setEnvironments( getEnvironments() );

        addMavenProject( impl, new File( "resources/siteresolver/bundle342" ), P2Resolver.TYPE_ECLIPSE_PLUGIN,
                         "bundle342" );
        addMavenProject( impl, new File( "resources/siteresolver/bundle352" ), P2Resolver.TYPE_ECLIPSE_PLUGIN,
                         "bundle352" );
        addMavenProject( impl, new File( "resources/siteresolver/feature342" ), P2Resolver.TYPE_ECLIPSE_FEATURE,
                         "feature342" );
        addMavenProject( impl, new File( "resources/siteresolver/feature352" ), P2Resolver.TYPE_ECLIPSE_FEATURE,
                         "feature352" );

        File basedir = new File( "resources/siteresolver/site" ).getCanonicalFile();
        addMavenProject( impl, basedir, P2Resolver.TYPE_ECLIPSE_UPDATE_SITE, "site" );

        P2ResolutionResult result = impl.collectProjectDependencies( basedir );

        impl.stop();

        Assert.assertEquals( 6, result.getArtifacts().size() );
    }

    @Test
    public void duplicateInstallableUnit()
        throws Exception
    {
        P2ResolverImpl impl = new P2ResolverImpl();
        impl.setLogger( new NullP2Logger() );
        impl.setRepositoryCache( new P2RepositoryCacheImpl() );
        impl.setLocalRepositoryLocation( getLocalRepositoryLocation() );
        impl.setEnvironments( getEnvironments() );

        File projectLocation = new File( "resources/duplicate-iu/featureA" ).getCanonicalFile();
        impl.addMavenProject( new ArtifactMock( projectLocation, "groupId", "featureA", "1.0.0-SNAPSHOT",
                                                P2Resolver.TYPE_ECLIPSE_FEATURE ) );

        impl.addMavenProject( new ArtifactMock( new File( "resources/duplicate-iu/featureA2" ).getCanonicalFile(),
                                                "groupId", "featureA2", "1.0.0-SNAPSHOT",
                                                P2Resolver.TYPE_ECLIPSE_FEATURE ) );

        try
        {
            impl.resolveProject( projectLocation );
            fail();
        }
        catch ( DuplicateReactorIUsException e )
        {
            // TODO proper assertion
        }
    }

    private void addMavenProject( P2ResolverImpl impl, File basedir, String packaging, String id )
        throws IOException
    {
        String version = "1.0.0-SNAPSHOT";

        impl.addMavenArtifact( new ArtifactMock( basedir.getCanonicalFile(), id, id, version, packaging ) );
    }

    protected List<P2ResolutionResult> resolveFromHttp( P2ResolverImpl impl, String url )
        throws IOException, URISyntaxException
    {
        impl.setRepositoryCache( new P2RepositoryCacheImpl() );
        impl.setLocalRepositoryLocation( getLocalRepositoryLocation() );
        impl.addP2Repository( new URI( url ) );

        impl.setEnvironments( getEnvironments() );

        String groupId = "org.sonatype.tycho.p2.impl.resolver.test.bundle01";
        File bundle = new File( "resources/resolver/bundle01" ).getCanonicalFile();

        addMavenProject( impl, bundle, P2Resolver.TYPE_ECLIPSE_PLUGIN, groupId );

        List<P2ResolutionResult> results = impl.resolveProject( bundle );
        return results;
    }

    protected File getLocalRepositoryLocation()
        throws IOException
    {
        return new File( "target/localrepo" ).getCanonicalFile();
    }

    private List<Map<String, String>> getEnvironments()
    {
        ArrayList<Map<String, String>> environments = new ArrayList<Map<String, String>>();

        Map<String, String> properties = new LinkedHashMap<String, String>();
        properties.put( "osgi.os", "linux" );
        properties.put( "osgi.ws", "gtk" );
        properties.put( "osgi.arch", "x86_64" );

        // TODO does not belong here
        properties.put( "org.eclipse.update.install.features", "true" );

        environments.add( properties );

        return environments;
    }

    static void delete( File dir )
    {
        if ( dir == null || !dir.exists() )
        {
            return;
        }

        if ( dir.isDirectory() )
        {
            File[] members = dir.listFiles();
            if ( members != null )
            {
                for ( File member : members )
                {
                    delete( member );
                }
            }
        }

        Assert.assertTrue( "Delete " + dir.getAbsolutePath(), dir.delete() );
    }
}