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

import java.io.PrintStream;
import java.util.Arrays;

import org.everit.osgi.ecm.annotation.Service;

import aQute.bnd.osgi.Annotation;
import aQute.bnd.osgi.ClassDataCollector;

/**
 * Collects ECM Service and ManualService annotations.
 */
public class ECMClassDataCollector extends ClassDataCollector {

  @Override
  public void annotation(final Annotation annotation) throws Exception {
    PrintStream out = System.out;
    out.println(
        "Annotation name: " + annotation.getName().getFQN() + "; elements: "
            + annotation.getElementType());
    out.println("  keyset: " + annotation.keySet());

    if (Service.class.getName().equals(annotation.getName().getFQN())) {
      Object value = annotation.get("value");
      Object[] valueArray = (Object[]) value;
      out.println(" service values: " + Arrays.toString(valueArray));
      if (valueArray.length > 0) {
        out.println("  type of service value element: " + valueArray[0].getClass());
      }

    }
  }
}
