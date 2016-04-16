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
package org.everit.osgi.ecm.bnd;

import java.util.ArrayList;
import java.util.Collection;

import aQute.bnd.header.Attrs;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Clazz;
import aQute.bnd.osgi.Constants;
import aQute.bnd.service.AnalyzerPlugin;

/**
 * Processes the Service and ManualServices annotations of ECM component classes and generates
 * Provide-Capability MANIFEST headers.
 */
public class ECMBndAnalyzerPlugin implements AnalyzerPlugin {

  public ECMBndAnalyzerPlugin() {
  }

  private void addComponentCapability(final ECMClassDataCollector ecmClassDataCollector,
      final Collection<Parameters> provides) {

    Attrs attrs = new Attrs();
    attrs.put("componentId", ecmClassDataCollector.getComponentId());
    attrs.put("class", ecmClassDataCollector.getClazz().getFQN());
    attrs.put("label", ecmClassDataCollector.getLabel());
    attrs.putTyped("version", ecmClassDataCollector.getVersion());

    String description = ecmClassDataCollector.getDescription();
    if (description != null) {
      attrs.put("description", description);
    }

    Parameters parameters = new Parameters();
    parameters.put("org.everit.osgi.ecm.component", attrs);

    provides.add(parameters);
  }

  private void addOSGiServiceCapabilities(final ECMClassDataCollector ecmClassDataCollector,
      final Collection<Parameters> provides) {

    String componentId = ecmClassDataCollector.getComponentId();
    Collection<Collection<String>> servicesWithInterfaces =
        ecmClassDataCollector.getServicesWithInterfaces();
    for (Collection<String> serviceInterfaces : servicesWithInterfaces) {
      Attrs attrs = new Attrs();

      attrs.putTyped("objectClass", serviceInterfaces);
      attrs.put("org.everit.osgi.ecm.component.id", componentId);
      attrs.putTyped("org.everit.osgi.ecm.component.version", ecmClassDataCollector.getVersion());

      Parameters parameters = new Parameters();
      parameters.put("osgi.service", attrs);

      provides.add(parameters);
    }
  }

  @Override
  public boolean analyzeJar(final Analyzer analyzer) throws Exception {
    Collection<Clazz> classes =
        analyzer.getClasses("getComponentClasses", Clazz.QUERY.ANNOTATED.name(),
            "org.everit.osgi.ecm.annotation.Component");

    Collection<Parameters> provides = new ArrayList<>();
    for (Clazz clazz : classes) {
      ECMClassDataCollector ecmClassDataCollector = new ECMClassDataCollector(clazz, analyzer);
      clazz.parseClassFileWithCollector(ecmClassDataCollector);
      processCollectedClassData(ecmClassDataCollector, provides);
    }

    StringBuilder sb = new StringBuilder();
    for (Parameters parameters : provides) {
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append(parameters.toString());
    }
    analyzer.setProperty(Constants.PROVIDE_CAPABILITY, sb.toString());

    return false;
  }

  private void processCollectedClassData(final ECMClassDataCollector ecmClassDataCollector,
      final Collection<Parameters> provides) {

    addComponentCapability(ecmClassDataCollector, provides);
    addOSGiServiceCapabilities(ecmClassDataCollector, provides);
  }

}
