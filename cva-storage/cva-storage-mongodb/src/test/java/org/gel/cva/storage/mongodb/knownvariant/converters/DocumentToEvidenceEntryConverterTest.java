package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.models.cva.avro.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by priesgo on 17/01/17.
 */
public class DocumentToEvidenceEntryConverterTest {

    private DocumentToEvidenceEntryConverter evidenceEntryConverter;
    private Date now = new Date();
    private Long date = now.getTime();
    private String submitter = "Mr.Test";
    private EvidenceSource evidenceSource = EvidenceSource.unknown;
    private AlleleOrigin alleleOrigin = AlleleOrigin.unknown;
    List<EvidencePhenotype> phenotypes = new LinkedList<EvidencePhenotype>();
    String version = "latest";
    String pubmedId = "12345";
    String url = "http://yourref.com";
    String study = "any_study";
    String databaseName = "RiskDB";
    Integer numberIndividuals = 4;
    String ethnicity = "headhunter";
    String geographicalOrigin = "inagalaxyfarfaraway";
    String description = "this reference is an important evidence";
    List<Comment> comments = new LinkedList<Comment>();

    @Before
    public void setup() {
        this.evidenceEntryConverter = new DocumentToEvidenceEntryConverter(new DocumentToCommentConverter());
        EvidencePhenotype evidencePhenotype = new EvidencePhenotype("SO:000001", InheritanceMode.autosomal_dominant);
        EvidencePhenotype evidencePhenotype2 = new EvidencePhenotype("SO:000001", InheritanceMode.autosomal_dominant);
        this.phenotypes.add(evidencePhenotype);
        this.phenotypes.add(evidencePhenotype2);
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
    }

    @Test
    public void convertToDataModelTypeTest() {
        Document document = new Document();
        document.append("date", this.date);
        document.append("submitter", this.submitter);
        document.append("evidenceSource", this.evidenceSource.toString());
        document.append("alleleOrigin", this.alleleOrigin.toString());
        List<Document> phenotypesDocuments = new LinkedList<Document>();
        for (EvidencePhenotype phenotype : this.phenotypes) {
            Document phenotypeDocument = new Document();
            phenotypeDocument.append("phenotype", phenotype.getPhenotype());
            phenotypeDocument.append("inheritanceMode", phenotype.getInheritanceMode().toString());
            phenotypesDocuments.add(phenotypeDocument);
        }
        document.append("phenotypes", phenotypesDocuments);
        document.append("version", this.version);
        document.append("pubmedId", this.pubmedId);
        document.append("url", this.url);
        document.append("study", this.study);
        document.append("databaseName", this.databaseName);
        document.append("numberIndividuals", this.numberIndividuals);
        document.append("ethnicity", this.ethnicity);
        document.append("geographicalOrigin", this.geographicalOrigin);
        document.append("description", this.description);
        List<Document> commentsDocuments = new LinkedList<Document>();
        for (Comment comment: this.comments) {
            Document commentDocument = new Document();
            commentDocument.append("text", comment.getText());
            commentDocument.append("author", comment.getAuthor());
            commentDocument.append("date", comment.getDate());
            commentsDocuments.add(commentDocument);
        }
        document.append("comments", commentsDocuments);
        EvidenceEntry evidenceEntry = evidenceEntryConverter.convertToDataModelType(document);
        assertEquals(evidenceEntry.getDate(), this.date);
        assertEquals(evidenceEntry.getSubmitter(), this.submitter);
        assertEquals(evidenceEntry.getSource(), this.evidenceSource);
        assertEquals(evidenceEntry.getAlleleOrigin(), this.alleleOrigin);
        assertEquals(evidenceEntry.getPhenotypes().size(), this.phenotypes.size());
        assertEquals(evidenceEntry.getVersion(), this.version);
        assertEquals(evidenceEntry.getPubmedId(), this.pubmedId);
        assertEquals(evidenceEntry.getUrl(), this.url);
        assertEquals(evidenceEntry.getStudy(), this.study);
        assertEquals(evidenceEntry.getDatabaseName(), this.databaseName);
        assertEquals(evidenceEntry.getNumberIndividuals(), this.numberIndividuals);
        assertEquals(evidenceEntry.getEthnicity(), this.ethnicity);
        assertEquals(evidenceEntry.getGeographicalOrigin(), this.geographicalOrigin);
        assertEquals(evidenceEntry.getDescription(), this.description);
        assertEquals(evidenceEntry.getComments().size(), this.comments.size());
    }

    @Test
    public void convertToStorageTypeTest() {
        EvidenceEntry evidenceEntry = new EvidenceEntry();
        evidenceEntry.setDate(this.date);
        evidenceEntry.setSubmitter(this.submitter);
        evidenceEntry.setSource(this.evidenceSource);
        evidenceEntry.setAlleleOrigin(this.alleleOrigin);
        evidenceEntry.setPhenotypes(this.phenotypes);
        evidenceEntry.setVersion(this.version);
        evidenceEntry.setPubmedId(this.pubmedId);
        evidenceEntry.setUrl(this.url);
        evidenceEntry.setStudy(this.study);
        evidenceEntry.setDatabaseName(this.databaseName);
        evidenceEntry.setNumberIndividuals(this.numberIndividuals);
        evidenceEntry.setEthnicity(this.ethnicity);
        evidenceEntry.setGeographicalOrigin(this.geographicalOrigin);
        evidenceEntry.setDescription(this.description);
        evidenceEntry.setComments(this.comments);
        Document document = evidenceEntryConverter.convertToStorageType(evidenceEntry);
        assertEquals(document.get("date"), this.date);
        assertEquals(document.get("submitter"), this.submitter);
        assertEquals(document.get("evidenceSource"), this.evidenceSource.toString());
        assertEquals(document.get("alleleOrigin"), this.alleleOrigin.toString());
        assertEquals(((List)document.get("phenotypes")).size(), this.phenotypes.size());
    }
}
