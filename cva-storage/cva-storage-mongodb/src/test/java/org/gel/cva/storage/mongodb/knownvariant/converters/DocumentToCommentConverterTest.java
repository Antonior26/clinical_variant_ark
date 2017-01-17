package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.models.cva.avro.Comment;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by priesgo on 17/01/17.
 */
public class DocumentToCommentConverterTest {

    private DocumentToCommentConverter commentConverter;
    private String text = "This is a test comment";
    private String author = "Mr.Test";
    private Date now = new Date();
    private Long date = now.getTime();

    @Before
    public void setup() {
        this.commentConverter = new DocumentToCommentConverter();
    }

    @Test
    public void convertToDataModelTypeTest() {
        Document document = new Document();
        document.append("text", this.text);
        document.append("date", this.date);
        document.append("author", this.author);
        Comment comment = commentConverter.convertToDataModelType(document);
        assertEquals(comment.getAuthor(), this.author);
        assertEquals(comment.getText(), this.text);
        assertEquals(comment.getDate(), this.date);
    }

    @Test
    public void convertToStorageTypeTest() {
        Comment comment = new Comment();
        comment.setText(this.text);
        comment.setAuthor(this.author);
        comment.setDate(this.date);
        Document document = commentConverter.convertToStorageType(comment);
        assertEquals(document.get("author"), this.author);
        assertEquals(document.get("text"), this.text);
        assertEquals(document.get("date"), this.date);
    }
}
