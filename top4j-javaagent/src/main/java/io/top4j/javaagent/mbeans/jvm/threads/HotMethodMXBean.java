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

package io.top4j.javaagent.mbeans.jvm.threads;

/**
 * Created by ryan on 27/07/15.
 */
public interface HotMethodMXBean {

	void setMethodName( String methodName );

	String getMethodName();

    void setThreadName( String threadName );

	String getThreadName();

	void setThreadId( long threadId );

	long getThreadId();

	void setLoadProfile( double loadProfile );

	double getLoadProfile();

	String getStackTrace( int maxDepth );

}
