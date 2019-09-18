/*
 * Copyright (c) 2019 Open Answers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.top4j.javaagent.exception;

import javax.management.MBeanException;

public class MBeanRuntimeException extends MBeanException {

    private static final long serialVersionUID = 1L;

    public MBeanRuntimeException(Exception e) {
        super(e);
    }

    public MBeanRuntimeException(Exception e, String message) {
        super(e, message);
    }
}