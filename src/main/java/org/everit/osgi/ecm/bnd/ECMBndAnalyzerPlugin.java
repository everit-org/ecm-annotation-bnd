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

import org.everit.osgi.ecm.annotation.Component;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Clazz;
import aQute.bnd.service.AnalyzerPlugin;

/**
 * Processes the Service and ManualServices annotations of ECM component classes and generates
 * Provide-Capability MANIFEST headers.
 */
public class ECMBndAnalyzerPlugin implements AnalyzerPlugin {

  @Override
  public boolean analyzeJar(final Analyzer analyzer) throws Exception {
    Collection<Clazz> classes =
        analyzer.getClasses("getComponentClasses", Clazz.QUERY.ANNOTATED.name(),
            Component.class.getName());

    for (Clazz clazz : classes) {
      clazz.parseClassFileWithCollector(new ECMClassDataCollector());
    }

    analyzer.setProperty("myheader", "myheadervalue");
    // TODO Auto-generated method stub
    return false;
  }

}
