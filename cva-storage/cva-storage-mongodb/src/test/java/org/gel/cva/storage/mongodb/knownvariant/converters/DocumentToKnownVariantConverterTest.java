package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.helpers.CvaDateFormatter;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.variant.converters.DocumentToVariantConverter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by priesgo on 17/01/17.
 */
public class DocumentToKnownVariantConverterTest {

    private DocumentToKnownVariantConverter knownVariantConverter;
    private DocumentToVariantConverter variantConverter;
    private DocumentToEvidenceEntryConverter evidenceEntryConverter;
    private DocumentToCurationEntryConverter curationEntryConverter;
    private DocumentToCommentConverter commentConverter;
    private List<EvidenceEntry> evidences = new LinkedList<>();
    private List<CurationEntry> curations = new LinkedList<>();
    private List<Comment> comments = new LinkedList<>();
    String date = CvaDateFormatter.getCurrentFormattedDate();
    String submitter = "Mr.Test";
    String sourceName = "RiskDB";
    SourceType sourceType = SourceType.other;
    String sourceVersion = "latest";
    String sourceUrl = "http://yourref.com";
    AlleleOrigin alleleOrigin = AlleleOrigin.unknown;
    List<HeritablePhenotype> phenotypes = new LinkedList<>();
    String pubmedId = "12345";
    String study = "any_study";
    Integer numberIndividuals = 4;
    EthnicCategory ethnicity = EthnicCategory.A;
    String description = "this reference is an important evidence";
    String chromosome = "chr19";
    String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
    Integer position = 44908684;
    String reference = "T";
    String alternate = "C";

    @Before
    public void setup() {
        this.variantConverter = new DocumentToVariantConverter();
        this.knownVariantConverter = new DocumentToKnownVariantConverter();
        this.evidenceEntryConverter = new DocumentToEvidenceEntryConverter();
        this.curationEntryConverter = new DocumentToCurationEntryConverter();
        this.commentConverter = new DocumentToCommentConverter();
        // fill comments
        Comment comment = new Comment();
        comment.setDate(CvaDateFormatter.getCurrentFormattedDate());
        comment.setAuthor("author_comment1");
        comment.setText("a very interesting comment");
        Comment comment2 = new Comment();
        comment2.setDate(CvaDateFormatter.getCurrentFormattedDate());
        comment2.setAuthor("author_comment2");
        comment2.setText("a more interesting comment");
        this.comments.add(comment);
        this.comments.add(comment2);
        // fill evidences
        EvidenceEntry evidenceEntry = new EvidenceEntry();
        evidenceEntry.setDate(this.date);
        evidenceEntry.setSubmitter(this.submitter);
        EvidenceSource evidenceSource = new EvidenceSource();
        evidenceSource.setName(this.sourceName);
        evidenceSource.setType(this.sourceType);
        evidenceSource.setVersion(this.sourceVersion);
        evidenceSource.setUrl(this.sourceUrl);
        evidenceEntry.setSource(evidenceSource);
        evidenceEntry.setAlleleOrigin(this.alleleOrigin);
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype(
                "HPO:000001", ReportedModeOfInheritance.biallelic);
        HeritablePhenotype heritablePhenotype2 = new HeritablePhenotype(
                "HPO:000002", ReportedModeOfInheritance.mitochondrial);
        this.phenotypes.add(heritablePhenotype);
        this.phenotypes.add(heritablePhenotype2);
        evidenceEntry.setHeritablePhenotypes(this.phenotypes);
        evidenceEntry.setPubmedId(this.pubmedId);
        evidenceEntry.setStudy(this.study);
        evidenceEntry.setNumberIndividuals(this.numberIndividuals);
        evidenceEntry.setEthnicity(this.ethnicity);
        evidenceEntry.setDescription(this.description);
        evidenceEntry.setComments(this.comments);
        this.evidences.add(evidenceEntry);
        // fill curations
        CurationEntry curationEntry = new CurationEntry();
        Curation curation = new Curation();
        curation.setClassification(CurationClassification.uncertain_significance);
        curation.setManualCurationConfidence(ManualCurationConfidence.high_confidence);
        curation.setPenetrance(0.9f);
        curation.setVariableExpressivity(true);
        curation.setHeritablePhenotype(heritablePhenotype);
        curationEntry.setCuration(curation);
        CurationHistoryEntry curationHistoryEntry = new CurationHistoryEntry();
        curationHistoryEntry.setCurator(this.submitter);
        curationHistoryEntry.setNewCuration(curation);
        curationHistoryEntry.setDate(CvaDateFormatter.getCurrentFormattedDate());
        List<CurationHistoryEntry> history = new LinkedList<>();
        curationEntry.setHistory(history);
        this.curations.add(curationEntry);
    }

    @Test
    public void convertToDataModelTypeTest() {
        // Prepares a Document
        Document document = new Document();
        Variant variant = new Variant(this.chromosome, this.position, this.reference, this.alternate);
        Document variantDocument = this.variantConverter.convertToStorageType(variant);
        document.append(DocumentToKnownVariantConverter.VARIANT, variantDocument);
        document.append(DocumentToKnownVariantConverter.SUBMITTER, this.submitter);
        // Adds evidences
        List<Document> evidencesDoc = new LinkedList<>();
        for (EvidenceEntry evidenceEntry: evidences) {
            Document evidenceEntryDoc = this.evidenceEntryConverter.convertToStorageType(evidenceEntry);
            evidencesDoc.add(evidenceEntryDoc);
        }
        document.append(DocumentToKnownVariantConverter.EVIDENCES, evidencesDoc);
        // Adds comments
        List<Document> commentsDoc = new LinkedList<>();
        for (Comment comment: this.comments) {
            Document commentDoc = this.commentConverter.convertToStorageType(comment);
            commentsDoc.add(commentDoc);
        }
        document.append(DocumentToKnownVariantConverter.COMMENTS, commentsDoc);
        // Adds curations
        List<Document> curationsDoc = new LinkedList<>();
        for (CurationEntry curationEntry : this.curations) {
            Document curationEntryDoc = this.curationEntryConverter.convertToStorageType(curationEntry);
            curationsDoc.add(curationEntryDoc);
        }
        document.append(DocumentToKnownVariantConverter.CURATIONS, curationsDoc);
        // Converts the Document to a KnownVariant
        KnownVariantWrapper knownVariantWrapper = knownVariantConverter.convertToDataModelType(document);
        // Checks the KnownVariant
        assertNotNull(knownVariantWrapper);
        assertEquals(this.chromosomeNormalized, knownVariantWrapper.getVariant().getChromosome());
        assertEquals(this.position, knownVariantWrapper.getVariant().getStart());
        assertEquals(this.reference, knownVariantWrapper.getVariant().getReference());
        assertEquals(this.alternate, knownVariantWrapper.getVariant().getAlternate());
        assertEquals(this.submitter, knownVariantWrapper.getImpl().getSubmitter());
        assertEquals(this.evidences.size(), knownVariantWrapper.getImpl().getEvidences().size());
        assertEquals(this.submitter, knownVariantWrapper.getImpl().getEvidences().get(0).getSubmitter());
        assertEquals(this.sourceName, knownVariantWrapper.getImpl().getEvidences().get(0).getSource().getName());
        assertEquals(this.sourceType, knownVariantWrapper.getImpl().getEvidences().get(0).getSource().getType());
        assertEquals(this.sourceUrl, knownVariantWrapper.getImpl().getEvidences().get(0).getSource().getUrl());
        assertEquals(this.sourceVersion, knownVariantWrapper.getImpl().getEvidences().get(0).getSource().getVersion());
        assertEquals(this.alleleOrigin, knownVariantWrapper.getImpl().getEvidences().get(0).getAlleleOrigin());
        assertEquals(this.phenotypes.size(), knownVariantWrapper.getImpl().getEvidences().get(0)
                .getHeritablePhenotypes().size());
        assertEquals(this.comments.size(), knownVariantWrapper.getImpl().getComments().size());
        assertEquals(this.curations.size(), knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.uncertain_significance, knownVariantWrapper.getImpl().getCurations()
                .get(0).getCuration().getClassification());
    }

    @Test
    public void convertToStorageTypeTest()
            throws VariantAnnotatorException, CvaException{
        // Prepares a known variant
        Variant variant = new Variant(this.chromosome, this.position, this.reference, this.alternate);
        KnownVariantWrapper knownVariantWrapper = new KnownVariantWrapper(this.submitter, variant);
        knownVariantWrapper.getImpl().setEvidences(this.evidences);
        knownVariantWrapper.getImpl().setCurations(this.curations);
        knownVariantWrapper.getImpl().setComments(this.comments);
        // Transforms to a document
        Document document = knownVariantConverter.convertToStorageType(knownVariantWrapper);
        assertNotNull(document);
        Document variantDoc = (Document) document.get(DocumentToKnownVariantConverter.VARIANT);
        assertEquals(this.chromosomeNormalized, variantDoc.get("chromosome"));
        assertEquals(this.position, variantDoc.get("start"));
        assertEquals(this.reference, variantDoc.get("reference"));
        assertEquals(this.alternate, variantDoc.get("alternate"));
        List<Document> evidencesDoc = (List<Document>) document.get(DocumentToKnownVariantConverter.EVIDENCES);
        assertEquals(this.evidences.size(), evidencesDoc.size());
        List<Document> curationsDoc = (List<Document>) document.get(DocumentToKnownVariantConverter.CURATIONS);
        assertEquals(this.curations.size(), curationsDoc.size());
        List<Document> commentsDoc = (List<Document>) document.get(DocumentToKnownVariantConverter.COMMENTS);
        assertEquals(this.comments.size(), commentsDoc.size());
        //TODO: check other fields
    }
}
