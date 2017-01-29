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
import org.gel.models.cva.avro.*;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;


/**
 * @author Pablo Riesgo Ferreiro <pablo.ferreiro@genomicsengland.co.uk>
 */
public class DocumentToEvidenceEntryConverter extends GenericDocumentComplexConverter<EvidenceEntry> {

    public static final String DATE = "date";
    public static final String SUBMITTER = "submitter";
    public static final String SOURCE = "source";
    public static final String SOURCE_NAME = "name";
    public static final String SOURCE_CLASS = "class$";
    public static final String SOURCE_VERSION = "version";
    public static final String SOURCE_URL = "url";
    public static final String ALLELE_ORIGIN = "alleleOrigin";
    public static final String PHENOTYPES = "phenotypes";
    public static final String PHENOTYPE = "phenotype";
    public static final String INHERITANCE_MODE = "inheritanceMode";
    public static final String PUBMED_ID = "pubmedId";
    public static final String STUDY = "study";
    public static final String NUMBER_INDIVIDUALS = "numberIndividuals";
    public static final String ETHNICITY = "ethnicity";
    public static final String DESCRIPTION = "description";
    public static final String COMMENTS = "comments";

    /**
     * Create a converter between {@link KnownVariantWrapper} and {@link Document} entities
     */
    public DocumentToEvidenceEntryConverter() {
        super(EvidenceEntry.class);
    }
}
