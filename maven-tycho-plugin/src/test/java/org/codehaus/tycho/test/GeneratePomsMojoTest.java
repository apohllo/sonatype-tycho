package org.codehaus.tycho.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MavenPluginCollector;
import org.apache.maven.plugin.MavenPluginDiscoverer;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.tycho.osgitools.OsgiState;

public class GeneratePomsMojoTest extends AbstractMojoTestCase {

	MavenXpp3Reader modelReader = new MavenXpp3Reader();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		OsgiState state = (OsgiState) lookup(OsgiState.class);
		state.init(new File(getBasedir(), "target/targetPlatform"), new Properties());
	}

	protected File getBaseDir(String name) throws IOException {
		File src = new File( getBasedir(), "src/test/resources/" + name );
		File dst = new File( getBasedir(), "target/" + name);

		if (dst.isDirectory()) {
			FileUtils.deleteDirectory(dst);
		} else if (dst.isFile()) {
			if (!dst.delete()) {
				throw new IOException("Can't delete file " + dst.toString());
			}
		}

		FileUtils.copyDirectoryStructure(src, dst);
		
		return dst;
	}

    private void generate(File baseDir, Map<String, Object> params) throws Exception {
		Mojo generateMojo = lookupMojo("org.codehaus.tycho", "maven-tycho-plugin", "0.2.0-SNAPSHOT", "generate-poms", null);
		setVariableValueToObject(generateMojo, "baseDir", baseDir);
		if (params != null) {
			for (Map.Entry<String, Object> param : params.entrySet()) {
				setVariableValueToObject(generateMojo, param.getKey(), param.getValue());
			}
		}
		generateMojo.execute();
	}

	private void generate(File baseDir) throws Exception {
		generate(baseDir, null);
	}

	public void testPluginPom() throws Exception {
		File baseDir = getBaseDir("projects/p001");
		generate(baseDir);
		Model model = readModel(baseDir, "pom.xml");
		
		assertEquals("p001", model.getGroupId());
		assertEquals("p001", model.getArtifactId());
		assertEquals("1.0.0", model.getVersion());
		assertEquals("eclipse-plugin", model.getPackaging());
	}

	public void testFeaturePom() throws Exception {
		File baseDir = getBaseDir("projects/p002");
		generate(baseDir);
		Model model = readModel(baseDir, "pom.xml");
		
		assertEquals("p002", model.getGroupId());
		assertEquals("p002", model.getArtifactId());
		assertEquals("1.0.0", model.getVersion());
		assertEquals("eclipse-feature", model.getPackaging());
	}

	public void testUpdateSite() throws Exception {
		File baseDir = getBaseDir("projects/p003");
		generate(baseDir);
		Model model = readModel(baseDir, "pom.xml");

//		assertEquals("p003", model.getGroupId());
		assertEquals("p003", model.getArtifactId());
//		assertEquals("1.0.0", model.getVersion());
		assertEquals("eclipse-update-site", model.getPackaging());
	}

	public void testParent() throws Exception {
		File baseDir = getBaseDir("projects");
		Map<String, Object> params  = new HashMap<String, Object>();
		params.put("groupId", "group");
		params.put("version", "1.0.0");
		generate(baseDir, params);
		Model model = readModel(baseDir, "pom.xml");

		assertEquals("group", model.getGroupId());
		assertEquals("projects", model.getArtifactId());
		assertEquals("1.0.0", model.getVersion());
		assertEquals("pom", model.getPackaging());

		List modules = model.getModules();
		assertEquals(3, modules.size());

		Model p002 = readModel(baseDir, "p002/pom.xml");
		
		assertEquals("group", p002.getParent().getGroupId());
		
	}

	private Model readModel(File baseDir, String name) throws IOException, XmlPullParserException {
    	File pom = new File(baseDir, name);
    	FileInputStream is = new FileInputStream(pom);
    	try {
    		return modelReader.read(is);
    	} finally {
    		is.close();
    	}
	}

	protected void customizeContainerConfiguration(ContainerConfiguration containerConfiguration) {
    	super.customizeContainerConfiguration(containerConfiguration);
    	containerConfiguration.addComponentDiscoverer( new MavenPluginDiscoverer() );
    	containerConfiguration.addComponentDiscoveryListener( new MavenPluginCollector() );
    }

}