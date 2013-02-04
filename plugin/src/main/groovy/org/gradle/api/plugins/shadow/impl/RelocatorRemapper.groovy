/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.gradle.api.plugins.shadow.impl

import org.gradle.api.plugins.shadow.relocation.Relocator
import org.objectweb.asm.commons.Remapper

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Modified from org.apache.maven.plugins.shade.DefaultShader.java#RelocatorRemapper
 *
 * Modifications
 * @author John Engelman
 */
class RelocatorRemapper extends Remapper {

    private final Pattern classPattern = Pattern.compile("(\\[*)?L(.+)")

    List<Relocator> relocators

    RelocatorRemapper(List<Relocator> relocators) {
        this.relocators = relocators
    }

    boolean hasRelocators() {
        return !relocators.isEmpty()
    }

    Object mapValue(Object object) {
        if (object instanceof String) {
            String name = (String) object
            String value = name

            String prefix = ""
            String suffix = ""

            Matcher m = classPattern.matcher(name)
            if (m.matches()) {
                prefix = m.group(1) + "L"
                suffix = ""
                name = m.group(2)
            }

            for (Relocator r : relocators) {
                if (r.canRelocateClass(name)) {
                    value = prefix + r.relocateClass(name) + suffix
                    break
                } else if (r.canRelocatePath(name)) {
                    value = prefix + r.relocatePath(name) + suffix
                    break
                }
            }

            return value
        }

        return super.mapValue(object)
    }

    String map(String name) {
        String value = name

        String prefix = ""
        String suffix = ""

        Matcher m = classPattern.matcher(name)
        if (m.matches()) {
            prefix = m.group(1) + "L"
            suffix = ""
            name = m.group(2)
        }

        for (Relocator r : relocators) {
            if (r.canRelocatePath(name)) {
                value = prefix + r.relocatePath(name) + suffix
                break
            }
        }

        return value
    }

}
