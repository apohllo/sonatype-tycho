package org.codehaus.tycho.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class Feature {
	
	public static final String FEATURE_XML = "feature.xml";

	private final Xpp3Dom dom;
	
	private final Map<String, Object> userProperties = new HashMap<String, Object>();

	public Feature(Xpp3Dom dom) {
		this.dom = dom;
	}

	public List<PluginRef> getPlugins() {
		ArrayList<PluginRef> plugins = new ArrayList<PluginRef>();
		for (Xpp3Dom pluginDom : dom.getChildren("plugin")) {
			plugins.add(new PluginRef(pluginDom));
		}
		return Collections.unmodifiableList(plugins);
	}

	public void setVersion(String version) {
		dom.setAttribute("version", version);
	}

	public List<FeatureRef> getIncludedFeatures() {
		ArrayList<FeatureRef> features = new ArrayList<FeatureRef>();
		for (Xpp3Dom featureDom : dom.getChildren("includes")) {
			features.add(new FeatureRef(featureDom));
		}
		return Collections.unmodifiableList(features);
	}

	public List<RequiresRef> getRequires() {
		ArrayList<RequiresRef> requires = new ArrayList<RequiresRef>();
		for (Xpp3Dom requiresDom : dom.getChildren("requires")) {
			requires.add(new RequiresRef(requiresDom));
		}
		return Collections.unmodifiableList(requires);
	}

	public static class PluginRef {
		
		private final Xpp3Dom dom;
		
		public PluginRef(Xpp3Dom dom) {
			this.dom = dom;
		}

		public String getId() {
			return dom.getAttribute("id");
		}

		public String getVersion() {
			return dom.getAttribute("version");
		}

		public void setVersion(String version) {
			dom.setAttribute("version", version);
		}

		public void setDownloadSide(long size) {
			dom.setAttribute("download-size", Long.toString(size));
		}

		public void setInstallSize(long size) {
			dom.setAttribute("install-size", Long.toString(size));
		}

	}

	public static class FeatureRef {
		private final Xpp3Dom dom;

		public FeatureRef(Xpp3Dom dom) {
			this.dom = dom;
		}

		public String getId() {
			return dom.getAttribute("id");
		}

		public String getVersion() {
			return dom.getAttribute("version");
		}
	}

	public static class RequiresRef {

		private final Xpp3Dom dom;

		public RequiresRef(Xpp3Dom dom) {
			this.dom = dom;
		}

		public List<ImportRef> getImports() {
			ArrayList<ImportRef> imports = new ArrayList<ImportRef>();
			for (Xpp3Dom importsDom : dom.getChildren("import")) {
				imports.add(new ImportRef(importsDom));
			}
			return Collections.unmodifiableList(imports);
		}
		
	}
	
	public static class ImportRef {

		private final Xpp3Dom dom;

		public ImportRef(Xpp3Dom dom) {
			this.dom = dom;
		}

		public String getPlugin() {
			return dom.getAttribute("plugin");
		}

		public String getFeature() {
			return dom.getAttribute("feature");
		}

	}

	@SuppressWarnings("deprecation")
	public static Feature read(File file) throws IOException, XmlPullParserException {
		XmlStreamReader reader = ReaderFactory.newXmlReader(file);
		try {
			return new Feature(Xpp3DomBuilder.build(reader));
		} finally {
			reader.close();
		}
	}

	public static void write(Feature feature, File file) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		try {
			Xpp3DomWriter.write(writer, feature.dom);
		} finally {
			writer.close();
		}
	}

	public String getVersion() {
		return dom.getAttribute("version");
	}

	public String getId() {
		return dom.getAttribute("id");
	}

	public void setUserProperty(String key, Object value) {
		userProperties.put(key, value);
	}

	public Object getUserProperty(String key) {
		return userProperties.get(key);
	}

}