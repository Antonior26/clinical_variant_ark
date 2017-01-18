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
import org.gel.cva.storage.core.knownvariant.dto.KnownVariant;
import org.gel.models.cva.avro.*;
import org.opencb.commons.datastore.core.ComplexTypeConverter;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Pablo Riesgo Ferreiro <pablo.ferreiro@genomicsengland.co.uk>
 */
public class DocumentToCommentConverter implements ComplexTypeConverter<Comment, Document> {

    public static final String TEXT = "text";
    public static final String DATE = "date";
    public static final String AUTHOR = "author";

    /**
     * Create a converter between {@link KnownVariant} and {@link Document} entities
     */
    public DocumentToCommentConverter() {

    }

    @Override
    public Comment convertToDataModelType(Document object) {
        Comment comment = new Comment();
        comment.setText((String) object.get(TEXT));
        comment.setDate((Long) object.get(DATE));
        comment.setAuthor((String) object.get(AUTHOR));
        return comment;
    }

    @Override
    public Document convertToStorageType(Comment comment) {
        // Creates an EvidenceEntry with the compulsory fields
        Document mongoComment = new Document()
                .append(TEXT, comment.getText())
                .append(DATE, comment.getDate())
                .append(AUTHOR, comment.getAuthor());
        return mongoComment;
    }

}
