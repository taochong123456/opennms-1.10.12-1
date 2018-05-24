/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

@Provider
public class ValidatingMessageBodyReader<T> implements MessageBodyReader<T> {

	@Context
	protected Providers providers;

	/**
	 * @return true if the class is a JAXB-marshallable class that has an
	 *         {@link javax.xml.bind.annotation.XmlRootElement} annotation.
	 */
	public boolean isReadable(final Class<?> clazz, final Type type,
			final Annotation[] annotations, final MediaType mediaType) {
		return (clazz.getAnnotation(XmlRootElement.class) != null);
	}

	public T readFrom(final Class<T> clazz, final Type type,
			final Annotation[] annotations, final MediaType mediaType,
			final MultivaluedMap<String, String> parameters,
			final InputStream stream) throws IOException,
			WebApplicationException {
		/*
		 * LogUtils.debugf(this, "readFrom: %s/%s/%s", clazz.getSimpleName(),
		 * type, mediaType);
		 * 
		 * JAXBContext jaxbContext = null; final ContextResolver<JAXBContext>
		 * resolver = providers.getContextResolver(JAXBContext.class,
		 * mediaType); try {
		 * 
		 * if (resolver != null) { jaxbContext = resolver.getContext(clazz); }
		 * 
		 * if (jaxbContext == null) { jaxbContext =
		 * JAXBContext.newInstance(clazz);
		 * 
		 * }
		 * 
		 * return JaxbUtils.unmarshal(clazz, new InputSource(stream),
		 * jaxbContext);
		 * 
		 * } catch (final JAXBException e) { throw new
		 * WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR); }
		 */

		ObjectMapper m = new ObjectMapper();
		JsonParser jp = m.getJsonFactory().createJsonParser(stream);
		/*
		 * Important: we are NOT to close the underlying stream after mapping,
		 * so we need to instruct parser:
		 */
		jp.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
		return m.readValue(jp, m.constructType(type));

	}
}
