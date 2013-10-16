/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.caelum.vraptor.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.proxy.Proxifier;

/**
 * Default implementation of {@link ExceptionMapper}.
 *
 * <p>This class is a part of Exception Handling Feature.</p>
 *
 * @author Otávio Scherer Garcia
 * @since 3.2
 */
@RequestScoped
public class DefaultExceptionMapper implements ExceptionMapper {

	private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);

	private final Map<Class<? extends Exception>, ExceptionRecorder<Result>> exceptions;
	private final Proxifier proxifier;

	/** 
	 * @deprecated CDI eyes only
	 */
	protected DefaultExceptionMapper() {
		this(null);
	}
	@Inject
	public DefaultExceptionMapper(Proxifier proxifier) {
		this.proxifier = proxifier;
		this.exceptions = newLinkedHashMap();
	}

	@Override
	public Result record(Class<? extends Exception> exception) {
		checkNotNull(exception, "Exception cannot be null.");

		ExceptionRecorder<Result> instance = new ExceptionRecorder<>(proxifier);
		exceptions.put(exception, instance);

		return proxifier.proxify(Result.class, instance);
	}

	@Override
	public ExceptionRecorder<Result> findByException(Exception e) {
		logger.debug("find for exception {}", e.getClass());

		for (Entry<Class<? extends Exception>, ExceptionRecorder<Result>> entry : exceptions.entrySet()) {
			if (entry.getKey().isInstance(e)) {
				logger.debug("found exception mapping: {} -> {}", entry.getKey(), entry.getValue());

				return entry.getValue();
			}
		}

		return hasExceptionCause(e) ? findByException((Exception) e.getCause()) : null;
	}

	private boolean hasExceptionCause(Exception e) {
		return e.getCause() != null && e.getCause() instanceof Exception;
	}
}
