package org.codehaus.tycho.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.IOUtil;

import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import de.pdark.decentxml.XMLIOSource;
import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLWriter;

/**
 * http://help.eclipse.org/ganymede/topic/org.eclipse.platform.doc.isv/reference/misc/update_sitemap.html
 */
public class UpdateSite
{
    public static final String SITE_XML = "site.xml";

    private static XMLParser parser = new XMLParser();

    private final Element dom;

    private final Document document;

    public UpdateSite( Document document )
    {
        this.document = document;
        this.dom = document.getRootElement();
    }

    public List<SiteFeatureRef> getFeatures()
    {
        ArrayList<SiteFeatureRef> features = new ArrayList<SiteFeatureRef>();
        for ( Element featureDom : dom.getChildren( "feature" ) )
        {
            features.add( new SiteFeatureRef( featureDom ) );
        }
        return Collections.unmodifiableList( features );
    }

    public Map<String, String> getArchives()
    {
        Map<String, String> archives = new HashMap<String, String>();
        for ( Element archiveDom : dom.getChildren( "archive" ) )
        {
            String path = archiveDom.getAttributeValue( "path" );
            String url = archiveDom.getAttributeValue( "url" );
            archives.put( path, url );
        }
        return Collections.unmodifiableMap( archives );
    }

    public void removeArchives()
    {
        for ( Element archive : dom.getChildren( "archive" ) )
        {
            dom.removeNode( archive );
        }
    }

    public static class SiteFeatureRef
        extends FeatureRef
    {

        public SiteFeatureRef( Element dom )
        {
            super( dom );
        }

        public void setUrl( String url )
        {
            dom.setAttribute( "url", url );
        }

        public String getUrl()
        {
            return dom.getAttributeValue( "url" );
        }

    }

    public static UpdateSite read( File file )
        throws IOException
    {
        return read( new BufferedInputStream( new FileInputStream( file ) ) );
    }

    public static UpdateSite read( InputStream is )
        throws IOException
    {
        try
        {
            return new UpdateSite( parser.parse( new XMLIOSource( is ) ) );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    public static void write( UpdateSite site, File file )
        throws IOException
    {
        OutputStream os = new BufferedOutputStream( new FileOutputStream( file ) );

        Document document = site.document;
        try
        {
            Writer w =
                document.getEncoding() != null ? new OutputStreamWriter( os, document.getEncoding() )
                                : new OutputStreamWriter( os );
            XMLWriter xw = new XMLWriter( w );
            try
            {
                document.toXML( xw );
            }
            finally
            {
                xw.flush();
            }
        }
        finally
        {
            IOUtil.close( os );
        }
    }

    public boolean isPack200()
    {
        String pack200 = dom.getAttributeValue( "pack200" );
        return "true".equals( pack200 );
    }
}
