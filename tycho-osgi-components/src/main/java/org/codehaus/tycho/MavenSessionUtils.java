package org.codehaus.tycho;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public class MavenSessionUtils
{
    public static MavenProject getMavenProject( MavenSession session, String basedir )
    {
        return getMavenProject( session, new File( basedir ) );
    }

    public static MavenProject getMavenProject( MavenSession session, File basedir )
    {
        for ( MavenProject project : session.getProjects() )
        {
            if ( basedir.equals( project.getBasedir() ) )
            {
                return project;
            }
        }

        return null; // not a reactor project
    }

    public static Map<File, MavenProject> getBasedirMap( MavenSession session )
    {
        return getBasedirMap( session.getProjects() );
    }

    public static Map<File, MavenProject> getBasedirMap( List<MavenProject> projects )
    {
        HashMap<File, MavenProject> result = new HashMap<File, MavenProject>();

        for ( MavenProject project : projects )
        {
            result.put( project.getBasedir(), project );
        }

        return result;
    }
}
