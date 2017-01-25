package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.cva.storage.core.helpers.CvaDateFormatter;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by priesgo on 17/01/17.
 */
public class DocumentToCurationHistoryEntryConverterTest {

    private DocumentToCurationHistoryEntryConverter curationHistoryEntryConverter;
    private String date = CvaDateFormatter.getCurrentFormattedDate();
    private CurationScore previousScore = CurationScore.CURATION_CONFIDENCE_5;
    private CurationScore newScore = CurationScore.CURATION_CONFIDENCE_1;
    private CurationClassification previousClassification = CurationClassification.disease_causing_variant;
    private CurationClassification newClassification = CurationClassification.benign_variant;
    private String curator = "the_curator";
    List<Comment> comments = new LinkedList<Comment>();

    @Before
    public void setup() {
        this.curationHistoryEntryConverter = new DocumentToCurationHistoryEntryConverter();
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
    }

    @Test
    public void convertToDataModelTypeTest() {
        Document document = new Document();
        document.append("date", this.date);
        CurationHistoryEntry curationHistoryEntry = curationHistoryEntryConverter.convertToDataModelType(document);
        assertEquals(curationHistoryEntry.getDate(), this.date);
    }

    @Test
    public void convertToStorageTypeTest() {
        CurationHistoryEntry curationHistoryEntry= new CurationHistoryEntry();
        curationHistoryEntry.setDate(this.date);
        Document document = curationHistoryEntryConverter.convertToStorageType(curationHistoryEntry);
        assertEquals(document.get("date"), this.date);
    }
}
