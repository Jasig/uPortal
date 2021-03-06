/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.json.rendering;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.character.stream.CharacterEventReader;
import org.apereo.portal.character.stream.events.CharacterEvent;
import org.apereo.portal.rendering.CharacterPipelineComponentWrapper;
import org.apereo.portal.rendering.PipelineEventReader;
import org.apereo.portal.rendering.PipelineEventReaderImpl;
import org.apereo.portal.utils.cache.CacheKey;

/** Adds JsonWrapperFilteringCharacterEventReader into the character event reader stream */
public class JsonWrapperFilteringCharacterPipelineComponent
        extends CharacterPipelineComponentWrapper {

    /* (non-Javadoc)
     * @see org.apereo.portal.rendering.PipelineComponent#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response)
            throws IllegalStateException {
        if (this.wrappedComponent != null) {
            return this.wrappedComponent.getCacheKey(request, response);
        } else {
            logger.debug("PipelineComponentWrapper.wrapperComponent is null");
            throw new IllegalStateException("PipelineComponentWrapper.wrapperComponent is null");
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<CharacterEventReader, CharacterEvent> getEventReader(
            HttpServletRequest request, HttpServletResponse response) throws IllegalStateException {
        if (this.wrappedComponent != null) {
            final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader =
                    this.wrappedComponent.getEventReader(request, response);
            final CharacterEventReader eventReader = pipelineEventReader.getEventReader();
            final JsonWrapperFilteringCharacterEventReader
                    jsonWrapperFilteringCharacterEventReader =
                            new JsonWrapperFilteringCharacterEventReader(eventReader);
            final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
            return new PipelineEventReaderImpl<CharacterEventReader, CharacterEvent>(
                    jsonWrapperFilteringCharacterEventReader, outputProperties);
        } else {
            logger.warn("PipelineComponentWrapper.wrapperComponent is null");
            throw new IllegalStateException("PipelineComponentWrapper.wrapperComponent is null");
        }
    }
}
