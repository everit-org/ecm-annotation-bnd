package org.everit.osgi.ecm.annotation.bnd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.everit.osgi.ecm.bnd.ECMBndAnalyzerPlugin;
import org.junit.Test;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;

public class ECMBndAnalyzerPluginTest {

  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  private File createJar(final Class<?>[] classses, final String[] resources) {
    File file;
    try {
      file = File.createTempFile("bndTest", null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(file))) {
      for (Class<?> clazz : classses) {
        String classResourceName = clazz.getName().replace('.', '/') + ".class";
        jarOutputStream.putNextEntry(new ZipEntry(classResourceName));

        writeResourceToOutputStream(classResourceName, jarOutputStream);

        jarOutputStream.closeEntry();
      }
      for (String resource : resources) {
        jarOutputStream.putNextEntry(new ZipEntry(resource));
        writeResourceToOutputStream(resource, jarOutputStream);
        jarOutputStream.closeEntry();
      }
    } catch (IOException e) {
      file.delete();
      throw new RuntimeException(e);
    }
    return file;
  }

  private Manifest generateManifestForClasses(final Class<?>[] classes, final String[] resources) {
    File jar = createJar(classes, resources);
    try (Analyzer analyzer = new Analyzer()) {
      analyzer.setJar(createJar(classes, resources));
      analyzer.addBasicPlugin(new ECMBndAnalyzerPlugin());
      return analyzer.calcManifest();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      jar.delete();
    }
  }

  @Test
  public void testNoServiceComponent() {
    Class<?>[] classes = new Class<?>[] { NoServiceComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);
    System.out.println(provideCapabilityHeader);
  }

  private void writeResourceToOutputStream(final String resourceName, final OutputStream out)
      throws IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    byte[] buffer = new byte[1024];
    try (InputStream is = classLoader.getResourceAsStream(resourceName)) {
      int r = is.read(buffer);
      while (r >= 0) {
        out.write(buffer, 0, r);
        r = is.read(buffer);
      }
    }
  }
}
