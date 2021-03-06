/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.lang.js;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ShortConverterMap extends HashMap<String, Object> {

  public ShortConverterMap(Map<String, Object> other) {
    for (Entry<String, Object> entry: other.entrySet()) {
      Object val = entry.getValue();
      if (val == null) {
        put(entry.getKey(), null);
      } else if (!(val instanceof Short) && val instanceof Number) {
        Number number = (Number)val;
        Short s = number.shortValue();
        put(entry.getKey(), s);
      }
    }
  }
}
