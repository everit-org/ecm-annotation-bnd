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

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Annotation;
import aQute.bnd.osgi.ClassDataCollector;
import aQute.bnd.osgi.Clazz;
import aQute.bnd.osgi.Descriptors.TypeRef;
import aQute.bnd.osgi.Resource;
import aQute.bnd.version.Version;

/**
 * Collects ECM Service and ManualService annotations.
 */
public class ECMClassDataCollector extends ClassDataCollector {

  private static final String LOCALIZED_VALUE_PREFIX = "%";

  private boolean allInterfacesAppended = false;

  private final Analyzer analyzer;

  private final Clazz clazz;

  private String componentId;

  private String description;

  private String label;

  private Properties localizationProperties = null;

  private final Collection<Collection<String>> servicesWithInterfaces = new LinkedHashSet<>();

  private Version version;

  public ECMClassDataCollector(final Clazz clazz, final Analyzer analyzer) {
    this.clazz = clazz;
    this.analyzer = analyzer;
  }

  private void addAllInterfaceRecurse(final Clazz currentClazz, final Set<String> interfaceNames) {
    if (currentClazz == null) {
      return;
    }
    if (currentClazz.isInterface()) {
      interfaceNames.add(currentClazz.getFQN());
    }
    Clazz superClazzTypeRef = resolveClazzByTypeRef(currentClazz.getSuper());
    addAllInterfaceRecurse(superClazzTypeRef, interfaceNames);

    TypeRef[] interfaces = currentClazz.getInterfaces();
    if (interfaces != null) {
      for (TypeRef typeRef : interfaces) {
        Clazz interfaceClazz = resolveClazzByTypeRef(typeRef);
        addAllInterfaceRecurse(interfaceClazz, interfaceNames);
      }
    }
  }

  @Override
  public void annotation(final Annotation annotation) throws Exception {
    String annotationFQN = annotation.getName().getFQN();

    switch (annotationFQN) {
      case "org.everit.osgi.ecm.annotation.Component":
        handleComponentAnnotation(annotation);
        break;
      case "org.everit.osgi.ecm.annotation.ManualServices":
        handleManualServicesAnnotation(annotation);
        break;
      case "org.everit.osgi.ecm.annotation.Service":
        handleServiceAnnotation(annotation);
        break;
      default:
        break;
    }
  }

  public Clazz getClazz() {
    return clazz;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getDescription() {
    return description;
  }

  public String getLabel() {
    return label;
  }

  private Properties getLocalizedProperties(final Annotation componentAnnotation) throws Exception {
    if (localizationProperties != null) {
      return localizationProperties;
    }
    localizationProperties = new Properties();
    String localizationBase = componentAnnotation.get("localizationBase");
    if (localizationBase == null) {
      localizationBase = "OSGI-INF/metatype/metatype";
    } else if (localizationBase.trim().equals("")) {
      return localizationProperties;
    }
    Resource localizationFile = analyzer.getJar().getResource(localizationBase + ".properties");
    if (localizationFile == null) {
      return localizationProperties;
    }
    try (InputStream in = localizationFile.openInputStream()) {
      localizationProperties.load(in);
    }
    return localizationProperties;
  }

  public Collection<Collection<String>> getServicesWithInterfaces() {
    return servicesWithInterfaces;
  }

  public Version getVersion() {
    return version;
  }

  private void handleComponentAnnotation(final Annotation annotation) {
    componentId = resolveComponentId(annotation);
    label = resolveLabel(annotation);
    description = resolveDescription(annotation);
    version = resolveVersion(annotation);

  }

  private void handleManualServicesAnnotation(final Annotation annotation) {
    Object[] value = annotation.get("value");
    for (Object annotationObj : value) {
      handleServiceAnnotation((Annotation) annotationObj);
    }
  }

  private void handleServiceAnnotation(final Annotation annotation) {
    Object[] typeArray = annotation.get("value");
    Set<String> interfaceNames = new LinkedHashSet<>();
    if (typeArray != null && typeArray.length > 0) {
      for (Object typeRefObj : typeArray) {
        interfaceNames.add(((TypeRef) typeRefObj).getFQN());
      }
    }
    if (interfaceNames.size() > 0) {
      servicesWithInterfaces.add(interfaceNames);
    } else if (!allInterfacesAppended) {
      allInterfacesAppended = true;
      addAllInterfaceRecurse(clazz, interfaceNames);
      if (interfaceNames.size() == 0) {
        interfaceNames.add(clazz.getFQN());
      }
      servicesWithInterfaces.add(interfaceNames);
    }
  }

  private Clazz resolveClazzByTypeRef(final TypeRef typeRef) {
    if (typeRef == null) {
      return null;
    }
    try {
      return analyzer.findClass(typeRef);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String resolveComponentId(final Annotation componentAnnotation) {
    String componentId = componentAnnotation.get("componentId");
    if (componentId != null) {
      return componentId;
    } else {
      return clazz.getFQN();
    }
  }

  private String resolveDescription(final Annotation annotation) {
    String result = annotation.get("description");
    if (result == null || "".equals(result.trim())) {
      return null;
    }
    if (result.startsWith(LOCALIZED_VALUE_PREFIX)) {
      result = resolveLocalizedValue(result, annotation);
    }
    return result;
  }

  private String resolveLabel(final Annotation annotation) {
    String result = annotation.get("label");
    if (result == null || "".equals(result.trim())) {
      return this.componentId;
    }
    if (result.startsWith(LOCALIZED_VALUE_PREFIX)) {
      result = resolveLocalizedValue(result, annotation);
    }
    return result;
  }

  private String resolveLocalizedValue(final String localizedValue,
      final Annotation componentAnnotation) {
    String result = localizedValue.substring(1);
    try {
      Properties props = getLocalizedProperties(componentAnnotation);
      return props.getProperty(result, result);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private Version resolveVersion(final Annotation annotation) {
    String versionValue = annotation.get("version");
    if (versionValue == null) {
      return new Version(analyzer.getVersion());
    }
    return new Version(versionValue);
  }

}
