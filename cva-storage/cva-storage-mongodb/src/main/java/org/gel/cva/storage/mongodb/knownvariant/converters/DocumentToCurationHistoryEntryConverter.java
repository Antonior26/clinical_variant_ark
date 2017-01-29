/*
 * Copyright 2017 Genomics England Ltd.
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

package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.CurationHistoryEntry;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;


/**
 * @author Pablo Riesgo Ferreiro <pablo.ferreiro@genomicsengland.co.uk>
 */
public class DocumentToCurationHistoryEntryConverter extends GenericDocumentComplexConverter<CurationHistoryEntry> {

    public static final String DATE = "date";
    public static final String PREVIOUS_SCORE = "previousScore";
    public static final String NEW_SCORE = "newScore";
    public static final String PREVIOUS_CLASSIFICATION = "previousClassification";
    public static final String NEW_CLASSIFICATION = "newClassification";
    public static final String CURATOR = "curator";
    public static final String COMMENTS = "comments";

    /**
     * Create a converter between {@link KnownVariantWrapper} and {@link Document} entities
     */
    public DocumentToCurationHistoryEntryConverter() {
        super(CurationHistoryEntry.class);
    }
}
