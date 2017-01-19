package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.dto.KnownVariant;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.variant.converters.DocumentToVariantConverter;

import java.io.IOException;
import java.util.Date;
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
    private List<Comment> comments = new LinkedList<Comment>();
    Date now = new Date();
    Long date = now.getTime();
    String submitter = "Mr.Test";
    String sourceName = "RiskDB";
    SourceClass sourceClass = SourceClass.unknown;
    String sourceVersion = "latest";
    String sourceUrl = "http://yourref.com";
    AlleleOrigin alleleOrigin = AlleleOrigin.unknown;
    List<EvidencePhenotype> phenotypes = new LinkedList<EvidencePhenotype>();
    String pubmedId = "12345";
    String study = "any_study";
    Integer numberIndividuals = 4;
    EthnicCategory ethnicity = EthnicCategory.A;
    String description = "this reference is an important evidence";

    @Before
    public void setup() {
        DocumentToCommentConverter commentConverter = new DocumentToCommentConverter();
        this.variantConverter = new DocumentToVariantConverter();
        this.knownVariantConverter = new DocumentToKnownVariantConverter();
        // fill evidences
        EvidenceEntry evidenceEntry = new EvidenceEntry();
        evidenceEntry.setDate(this.date);
        evidenceEntry.setSubmitter(this.submitter);
        evidenceEntry.setSourceName(this.sourceName);
        evidenceEntry.setSourceClass(this.sourceClass);
        evidenceEntry.setSourceVersion(this.sourceVersion);
        evidenceEntry.setSourceUrl(this.sourceUrl);
        evidenceEntry.setAlleleOrigin(this.alleleOrigin);
        EvidencePhenotype evidencePhenotype = new EvidencePhenotype("SO:000001", ReportedModeOfInheritance.biallelic);
        EvidencePhenotype evidencePhenotype2 = new EvidencePhenotype("SO:000002", ReportedModeOfInheritance.mitochondrial);
        this.phenotypes.add(evidencePhenotype);
        this.phenotypes.add(evidencePhenotype2);
        evidenceEntry.setPhenotypes(this.phenotypes);
        evidenceEntry.setPubmedId(this.pubmedId);
        evidenceEntry.setStudy(this.study);
        evidenceEntry.setNumberIndividuals(this.numberIndividuals);
        evidenceEntry.setEthnicity(this.ethnicity);
        evidenceEntry.setDescription(this.description);
        evidenceEntry.setComments(this.comments);
        // fill comments
        Comment comment = new Comment();
        comment.setDate(new Long(1234));
        comment.setAuthor("author_comment1");
        comment.setText("a very interesting comment");
        Comment comment2 = new Comment();
        comment2.setDate(new Long(5678));
        comment2.setAuthor("author_comment2");
        comment2.setText("a more interesting comment");
        this.comments.add(comment);
        this.comments.add(comment2);
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
        //TODO: check other fields
    }

    @Test
    public void convertToStorageTypeTest()
            throws VariantAnnotatorException, IOException, IllegalCvaConfigurationException{
        Variant variant = new Variant("1", 12345, "A", "C");
        KnownVariant knownVariant = new KnownVariant(variant);
        knownVariant.setCurationClassification(this.curationClassification.toString());
        knownVariant.setCurationScore(this.curationScore);
        Document document = knownVariantConverter.convertToStorageType(knownVariant);
        assertEquals(document.get("classification"), this.curationClassification.toString());
        assertEquals(document.get("curationScore"), this.curationScore);
        //TODO: check other fields
    }
}
