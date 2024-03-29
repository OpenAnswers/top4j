/*
 * Copyright (c) 2021 Open Answers Ltd.
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

package io.top4j.cli;

import java.util.TimerTask;

public class ConsoleControllerTask extends TimerTask {

    private final ConsoleController controller;

    public ConsoleControllerTask(ConsoleController controller) {
        this.controller = controller;
    }

    public void run() {
        this.controller.run();
    }
}
