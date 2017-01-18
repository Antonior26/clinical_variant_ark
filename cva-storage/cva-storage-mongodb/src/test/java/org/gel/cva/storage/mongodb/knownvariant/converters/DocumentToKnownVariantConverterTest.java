package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.cva.storage.core.knownvariant.dto.KnownVariant;
import org.gel.models.cva.avro.Comment;
import org.gel.models.cva.avro.CurationClassification;
import org.gel.models.cva.avro.EvidenceEntry;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.variant.converters.DocumentToVariantConverter;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by priesgo on 17/01/17.
 */
public class DocumentToKnownVariantConverterTest {

    private DocumentToKnownVariantConverter knownVariantConverter;
    private DocumentToVariantConverter variantConverter;
    private CurationClassification curationClassification = CurationClassification.disease_associated_variant;
    private Integer curationScore = 5;
    private List<EvidenceEntry> evidences = new LinkedList<EvidenceEntry>();
    private List<Comment> comment = new LinkedList<Comment>();

    @Before
    public void setup() {
        DocumentToCommentConverter commentConverter = new DocumentToCommentConverter();
        this.variantConverter = new DocumentToVariantConverter();
        this.knownVariantConverter = new DocumentToKnownVariantConverter(
                this.variantConverter,
                new DocumentToEvidenceEntryConverter(commentConverter),
                commentConverter
        );
        //TODO: fill evidences
        //TODO: fill comments
        //TODO: fill curation history
    }

    @Test
    public void convertToDataModelTypeTest() {
        Document document = new Document();
        document.append("classification", this.curationClassification.toString());
        document.append("curationScore", this.curationScore.toString());
        Variant variant = new Variant("1", 12345, "A", "C");
        Document variantDocument = this.variantConverter.convertToStorageType(variant);
        document.append("variant", variantDocument);
        KnownVariant knownVariant = knownVariantConverter.convertToDataModelType(document);
        assertEquals(knownVariant.getCurationClassification(), this.curationClassification.toString());
        assertEquals(knownVariant.getCurationScore(), this.curationScore);
    }

    @Test
    public void convertToStorageTypeTest() throws VariantAnnotatorException{
        Variant variant = new Variant("1", 12345, "A", "C");
        KnownVariant knownVariant = new KnownVariant(variant);
        knownVariant.setCurationClassification(this.curationClassification.toString());
        knownVariant.setCurationScore(this.curationScore);
        Document document = knownVariantConverter.convertToStorageType(knownVariant);
        assertEquals(document.get("classification"), this.curationClassification.toString());
        assertEquals(document.get("curationScore"), this.curationScore);
    }
}
