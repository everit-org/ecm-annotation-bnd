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

import java.util.Collection;

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

  private void addComponentCapabilities(final ECMClassDataCollector ecmClassDataCollector,
      final StringBuilder sb) {

    if (sb.length() > 0) {
      sb.append(',');
    }
    sb.append("org.everit.osgi.ecm.component;componentId=")
        .append(escapeClauseElementValue(ecmClassDataCollector.getComponentId(), false))
        .append(";class=").append(ecmClassDataCollector.getClazz().getFQN()).append(";label=\"")
        .append(escapeClauseElementValue(ecmClassDataCollector.getLabel(), true)).append('\"');

    String description = ecmClassDataCollector.getDescription();
    if (description != null) {
      sb.append(";description=\"").append(escapeClauseElementValue(description, true))
          .append('"');
    }

  }

  private void addOSGiServiceCapabilities(final ECMClassDataCollector ecmClassDataCollector,
      final StringBuilder sb) {
    String componentId = ecmClassDataCollector.getComponentId();
    Collection<Collection<String>> servicesWithInterfaces =
        ecmClassDataCollector.getServicesWithInterfaces();
    for (Collection<String> serviceInterfaces : servicesWithInterfaces) {
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append("osgi.service");
      sb.append(";objectClass:List<String>=\"");
      boolean first = true;
      for (String serviceInterface : serviceInterfaces) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        sb.append(serviceInterface);
      }
      sb.append("\";org.everit.osgi.ecm.component.id=\"")
          .append(escapeClauseElementValue(componentId, true));
    }
  }

  @Override
  public boolean analyzeJar(final Analyzer analyzer) throws Exception {
    Collection<Clazz> classes =
        analyzer.getClasses("getComponentClasses", Clazz.QUERY.ANNOTATED.name(),
            "org.everit.osgi.ecm.annotation.Component");

    for (Clazz clazz : classes) {
      ECMClassDataCollector ecmClassDataCollector = new ECMClassDataCollector(clazz, analyzer);
      clazz.parseClassFileWithCollector(ecmClassDataCollector);
      processCollectedClassData(ecmClassDataCollector, analyzer);
    }
    return false;
  }

  private String escapeClauseElementValue(final String clauseElementValue, final boolean inQuotes) {
    // String backslash = (inQuotes) ? "\\\\" : "\\";
    String backslash = "\\";
    StringBuilder sb = new StringBuilder();

    char[] charArray = clauseElementValue.toCharArray();
    for (char c : charArray) {
      switch (c) {
        case '\\':
          sb.append(backslash).append(backslash);
          break;
        case ',':
        case '"':
          sb.append(backslash).append(c);
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  private void processCollectedClassData(final ECMClassDataCollector ecmClassDataCollector,
      final Analyzer analyzer) {
    StringBuilder sb = new StringBuilder();

    addOSGiServiceCapabilities(ecmClassDataCollector, sb);
    addComponentCapabilities(ecmClassDataCollector, sb);
    analyzer.setProperty(Constants.PROVIDE_CAPABILITY, sb.toString());
  }

}
