/*
 * Copyright (c) 2019 Open Answers Ltd. https://www.openanswers.co.uk
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

package io.top4j.javaagent.test;

public class Test {

    /**
     * Run JavaAgent test
     * @param args list of command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Firing up JavaAgent test....");
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            System.err.println("Thread sleep interrupted.");
        }

    }

}
