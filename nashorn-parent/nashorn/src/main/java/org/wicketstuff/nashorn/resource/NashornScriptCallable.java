/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.nashorn.resource;

import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.wicket.request.resource.IResource.Attributes;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * The script callable setup the script environment and evaluates the given script. If the script is
 * running the thread id is going to be determined. This id is used to check the memory usage of the
 * process.
 * 
 * @author Tobias Soloschenko
 *
 */
@SuppressWarnings({ "restriction" })
public class NashornScriptCallable implements Callable<Value>
{

	private String script;

	private Attributes attributes;

	private Predicate<String> classFilter;

	private OutputStream outputStream;

	private OutputStream errorStream;

	private volatile long threadId = -1;

	/**
	 * Creates a script result
	 * 
	 * @param script
	 *            the script to be executed
	 * @param attributes
	 *            the attributes to
	 * @param classFilter
	 *            the class filter to be applied
	 * @param outputStream
	 *            the stream to output script prints
	 * @param errorStream
	 *            the stream to output errors
	 */
	public NashornScriptCallable(String script, Attributes attributes, Predicate<String> classFilter,
		OutputStream outputStream, OutputStream errorStream)
	{
		this.script = script;
		this.attributes = attributes;
		this.classFilter = classFilter;
		this.outputStream = outputStream;
		this.errorStream = errorStream;
	}

	@Override
	public Value call() throws Exception
	{
		enableSecurity();
		Context context = Context.newBuilder("js")
				.allowHostAccess(true)
				.hostClassFilter(classFilter)
				.out(getOutputStream())
				.err(getErrorStream())
				.build();
		Value bindings = context.getBindings("js");
		setup(getAttributes(), bindings);
		Thread thread = Thread.currentThread();
		threadId = thread.getId();
		bindings.putMember("nashornResourceReferenceScriptExecutionThread", thread);
		return context.eval("js", getScript());
	}

	/**
	 * Enables the security for the current thread
	 */
	private void enableSecurity()
	{
		SecurityManager securityManager = System.getSecurityManager();
		if (securityManager != null && securityManager instanceof NashornSecurityManager)
		{
			((NashornSecurityManager)securityManager).enable();
		}
	}

	/**
	 * Setup the bindings and make information available to the scripting context
	 * 
	 * @param attributes
	 *            the attributes of the request
	 * @param bindings
	 *            the bindings to add java objects to
	 */
	protected void setup(Attributes attributes, Value bindings)
	{
		// NOOP
	}

	/**
	 * Gets the script
	 * 
	 * @return the script
	 */
	public String getScript()
	{
		return script;
	}

	/**
	 * Gets the attributes
	 * 
	 * @return the attributes
	 */
	public Attributes getAttributes()
	{
		return attributes;
	}

	/**
	 * Gets the class filter
	 * 
	 * @return the class filter
	 */
	public Predicate<String> getClassFilter()
	{
		return classFilter;
	}

	/**
	 * Gets the writer
	 * 
	 * @return the writer
	 */
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	/**
	 * Gets the error writer
	 * 
	 * @return the error writer
	 */
	public OutputStream getErrorStream()
	{
		return errorStream;
	}

	/**
	 * Gets the script callable thread id
	 * 
	 * @return the script callable script id
	 */
	public long getThreadId()
	{
		return threadId;
	}
}
