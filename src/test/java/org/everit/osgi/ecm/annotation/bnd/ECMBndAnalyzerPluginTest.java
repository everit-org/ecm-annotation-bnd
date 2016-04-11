/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.ecm.annotation.bnd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.everit.osgi.ecm.annotation.bnd.ClassThatContainsComponent.EmbeddedClassWithCustomIdComponent;
import org.everit.osgi.ecm.bnd.ECMBndAnalyzerPlugin;
import org.junit.Assert;
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
      analyzer.setJar(jar);
      analyzer.addBasicPlugin(new ECMBndAnalyzerPlugin());
      return analyzer.calcManifest();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      jar.delete();
    }
  }

  @Test
  public void testAllInterfacesServiceComponent() {
    Class<?>[] classes =
        new Class<?>[] { AllInterfacesServiceComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;componentId=short;"
            + "class=\"org.everit.osgi.ecm.annotation.bnd.AllInterfacesServiceComponent\";"
            + "label=short,"
            + "osgi.service;objectClass:List<String>="
            + "\"java.io.Serializable,java.io.Closeable,java.lang.AutoCloseable\";"
            + "org.everit.osgi.ecm.component.id=short",
        provideCapabilityHeader);
  }

  @Test
  public void testEmbeddedComponentWithCustomId() {
    Class<?>[] classes =
        new Class<?>[] { EmbeddedClassWithCustomIdComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;componentId=customId;"
            + "class=\"org.everit.osgi.ecm.annotation.bnd."
            + "ClassThatContainsComponent$EmbeddedClassWithCustomIdComponent\";"
            + "label=customId",
        provideCapabilityHeader);
  }

  @Test
  public void testLocalizedLabelAndDescriptionWithDefaultLocBaseComponent() {
    Class<?>[] classes =
        new Class<?>[] { LocalizedLabelAndDescriptionWithDefaultLocBaseComponent.class };
    String[] resources = new String[] { "OSGI-INF/metatype/metatype.properties" };
    Manifest manifest = generateManifestForClasses(classes, resources);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;"
            + "componentId=\"org.everit.osgi.ecm.annotation.bnd."
            + "LocalizedLabelAndDescriptionWithDefaultLocBaseComponent\";"
            + "class=\"org.everit.osgi.ecm.annotation.bnd."
            + "LocalizedLabelAndDescriptionWithDefaultLocBaseComponent\";"
            + "label=\"Test localized label\";"
            + "description=\"Test localized desc\"",
        provideCapabilityHeader);
  }

  @Test
  public void testLocalizedLabelAndDescriptionWithMissingLocPropComponent() {
    Class<?>[] classes =
        new Class<?>[] { LocalizedLabelAndDescriptionWithMissingLocPropComponent.class };
    String[] resources = new String[] { "OSGI-INF/metatype/metatype.properties" };
    Manifest manifest = generateManifestForClasses(classes, resources);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;"
            + "componentId=\"org.everit.osgi.ecm.annotation.bnd."
            + "LocalizedLabelAndDescriptionWithMissingLocPropComponent\";"
            + "class=\"org.everit.osgi.ecm.annotation.bnd."
            + "LocalizedLabelAndDescriptionWithMissingLocPropComponent\";"
            + "label=testLabel;"
            + "description=testDesc",
        provideCapabilityHeader);
  }

  @Test
  public void testServiceWithManualAndServiceComponent() {
    Class<?>[] classes =
        new Class<?>[] { ServiceWithManualAndServiceComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    System.out.println(provideCapabilityHeader);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;"
            + "componentId=short;class=\"org.everit.osgi.ecm.annotation.bnd."
            + "ServiceWithManualAndServiceComponent\";"
            + "label=short,"
            + "osgi.service;objectClass:List<String>=\"java.lang.Byte,java.lang.Integer\";"
            + "org.everit.osgi.ecm.component.id=short,"
            + "osgi.service;objectClass:List<String>=\"java.lang.Integer,java.lang.String\";"
            + "org.everit.osgi.ecm.component.id=short,"
            + "osgi.service;objectClass:List<String>=\"java.lang.String,java.lang.Short\";"
            + "org.everit.osgi.ecm.component.id=short",
        provideCapabilityHeader);
  }

  @Test
  public void testServiceWithoutInterfacesDefinitionComponent() {
    Class<?>[] classes =
        new Class<?>[] { ServiceWithoutInterfacesDefinitionComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;"
            + "componentId=\"org.everit.osgi.ecm.annotation.bnd."
            + "ServiceWithoutInterfacesDefinitionComponent\";"
            + "class=\"org.everit.osgi.ecm.annotation.bnd."
            + "ServiceWithoutInterfacesDefinitionComponent\";"
            + "label=\"org.everit.osgi.ecm.annotation.bnd."
            + "ServiceWithoutInterfacesDefinitionComponent\","
            + "osgi.service;objectClass:List<String>="
            + "\"org.everit.osgi.ecm.annotation.bnd.ServiceWithoutInterfacesDefinitionComponent\";"
            + "org.everit.osgi.ecm.component.id="
            + "\"org.everit.osgi.ecm.annotation.bnd.ServiceWithoutInterfacesDefinitionComponent\"",
        provideCapabilityHeader);
  }

  @Test
  public void testSimpleComponent() {
    Class<?>[] classes = new Class<?>[] { SimpleComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;"
            + "componentId=\"org.everit.osgi.ecm.annotation.bnd.SimpleComponent\";"
            + "class=\"org.everit.osgi.ecm.annotation.bnd.SimpleComponent\";"
            + "label=\"org.everit.osgi.ecm.annotation.bnd.SimpleComponent\"",
        provideCapabilityHeader);
  }

  @Test
  public void testStaticLabelAndDescriptionComponent() {
    Class<?>[] classes = new Class<?>[] { StaticLabelAndDescriptionComponent.class };
    Manifest manifest = generateManifestForClasses(classes, EMPTY_STRING_ARRAY);
    String provideCapabilityHeader =
        manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

    Assert.assertEquals(
        "org.everit.osgi.ecm.component;"
            + "componentId="
            + "\"org.everit.osgi.ecm.annotation.bnd.StaticLabelAndDescriptionComponent\";"
            + "class=\"org.everit.osgi.ecm.annotation.bnd.StaticLabelAndDescriptionComponent\";"
            + "label=\"Test Label\";"
            + "description=\"Test Description\"",
        provideCapabilityHeader);
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
