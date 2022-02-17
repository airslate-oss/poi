package org.apache.poi.xwpf.usermodel;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.*;

/**
 * Test class for manipulation of block level Content Controls.
 * Related classes are:
 *      {@link XWPFSDTBlock}, {@link XWPFSDTPr}, {@link XWPFSDTContentBlock}
 */
public final class TestXWPFSDTBlock {

    /**
     * Verify that Sdt Block Pr is added to Sdt Block
     * and the related object references were updated
     */
    @Test
    public void testCreateSdtBlockPr() {
        XWPFDocument doc = new XWPFDocument();
        XWPFSDTBlock sdtBlock = doc.createSdt();

        XmlCursor cur = doc.getDocument().newCursor();
        cur.toFirstChild(); // move cursor to Body
        cur.toFirstChild(); // move cursor to SDT
        assertTrue(cur.getObject() instanceof CTSdtBlock);

        XWPFSDTPr sdtBlockPr = sdtBlock.createSdtPr();

        cur.toFirstChild();
        assertTrue(cur.getObject() instanceof CTSdtPr);
    }

    /**
     * Verify that Sdt Block Content is added to Sdt Block
     * and the related object references were updated
     */
    @Test
    public void testCreateSdtContentBlock() {
        XWPFDocument doc = new XWPFDocument();
        XWPFSDTBlock sdtBlock = doc.createSdt();

        XmlCursor cur = doc.getDocument().newCursor();
        cur.toFirstChild(); // move cursor to Body
        cur.toFirstChild(); // move cursor to SDT
        assertTrue(cur.getObject() instanceof CTSdtBlock);

        XWPFSDTContentBlock sdtBlockContent = sdtBlock.createSdtContent();

        cur.toFirstChild();
        assertTrue(cur.getObject() instanceof CTSdtContentBlock);
    }

    @Test
    public void testGetParagraphFromSdtBlockContent() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("blockAndInlineSdtTags.docx");
        XWPFSDTBlock sdtBlock = (XWPFSDTBlock) doc.getBodyElements().get(2);

        CTP p = sdtBlock.getContent().getParagraphs().get(0).getCTP();
        assertSame(
                sdtBlock.getContent().getParagraphs().get(0),
                sdtBlock.getContent().getParagraph(p)
        );
    }

    @Test
    public void testInsertNewParagraphToSdtBlockContent() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("blockAndInlineSdtTags.docx");
        XWPFSDTBlock sdtBlock = (XWPFSDTBlock) doc.getBodyElements().get(2);

        XmlCursor cur = sdtBlock.getContent().getCtSdtContentBlock().newCursor();
        cur.toFirstChild(); // move cursor to Tbl
        cur.toEndToken(); // move cursor to the end of Tbl
        cur.toNextToken(); // move cursor right after the Tbl

        assertEquals(1, sdtBlock.getContent().getParagraphs().size());

        XWPFParagraph newP = sdtBlock.getContent().insertNewParagraph(cur);

        assertEquals(2, sdtBlock.getContent().getParagraphs().size());
        assertEquals(3, sdtBlock.getContent().getBodyElements().size());
        assertSame(newP, sdtBlock.getContent().getParagraphs().get(0));
    }

    @Test
    public void testInsertSdtBlockInDocument() {
        XWPFDocument doc = new XWPFDocument();

        // create few elements in body
        XWPFParagraph p = doc.createParagraph();
        p.createRun().setText("Text in first paragraph");
        doc.createTable().createRow().createCell().addParagraph().createRun().setText("Text in Tbl cell");

        XmlCursor cur = p.getCTP().newCursor();
        cur.toEndToken();
        cur.toNextToken(); // move cursor right after the Paragraph

        XWPFSDTBlock sdtBlock = doc.insertNewSdtBlock(cur);

        assertEquals(3, doc.getBodyElements().size());
        assertEquals(1, doc.getSdtBlocks().size());

        cur = p.getCTP().newCursor();
        cur.toEndToken();
        cur.toNextToken();

        // verify that Sdt Block is inserted
        assertTrue(cur.getObject() instanceof CTSdtBlock);
    }

    @Test
    public void testInsertExistingParagraphToSdtContentBlock() {
        XWPFDocument doc = new XWPFDocument();
        doc.createParagraph().createRun().setText("Some text1");
        XWPFSDTBlock sdtBlock = doc.createSdt();
        XWPFSDTContentBlock sdtBlockContent = sdtBlock.createSdtContent();
        sdtBlockContent.cloneExistingIBodyElement(
                doc.getParagraphs().get(0)
        );

        assertEquals("Some text1", sdtBlockContent.getParagraphs().get(0).getText());
        assertEquals(1, sdtBlockContent.getParagraphs().size());
        assertEquals(1, sdtBlockContent.getBodyElements().size());
    }

    @Test
    public void testInsertExistingTblToSdtContentBlock() {
        XWPFDocument doc = new XWPFDocument();
        doc.createTable().createRow().createCell().addParagraph().createRun().setText("Deep in Tbl");
        XWPFSDTBlock sdtBlock = doc.createSdt();
        XWPFSDTContentBlock sdtBlockContent = sdtBlock.createSdtContent();
        sdtBlockContent.cloneExistingIBodyElement(
                doc.getTables().get(0)
        );

        assertEquals("Deep in Tbl", sdtBlockContent.getTables().get(0).getText().trim());
        assertEquals(1, sdtBlockContent.getTables().size());
        assertEquals(1, sdtBlockContent.getBodyElements().size());
    }

    @Test
    public void testInsertNewTblToSdtBlockContent() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("blockAndInlineSdtTags.docx");
        XWPFSDTBlock sdtBlock = (XWPFSDTBlock) doc.getBodyElements().get(2);

        XmlCursor cur = sdtBlock.getContent().getCtSdtContentBlock().newCursor();
        cur.toFirstChild(); // move cursor to Tbl
        cur.toEndToken(); // move cursor to the end of Tbl
        cur.toNextToken(); // move cursor fight after the Tbl

        assertEquals(1, sdtBlock.getContent().getTables().size());

        XWPFTable newTbl = sdtBlock.getContent().insertNewTbl(cur);

        assertEquals(2, sdtBlock.getContent().getTables().size());
        assertEquals(3, sdtBlock.getContent().getBodyElements().size());
        assertSame(newTbl, sdtBlock.getContent().getTables().get(1));
    }

    /**
     * Verify that existing Content Control in document is correctly
     * unmarshalled & we can proceed with modifying its content
     * @throws Exception
     */
    @Test
    public void testUnmarshallingSdtBlock() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("blockAndInlineSdtTags.docx");
        XWPFSDTBlock sdtBlock = (XWPFSDTBlock) doc.getBodyElements().get(2);

        // Tag
        assertEquals("block-sdt-tag", sdtBlock.getSdtPr().getTag());

        sdtBlock.getSdtPr().setTag("new-block-tag");
        assertEquals("new-block-tag", sdtBlock.getSdtPr().getTag());

        // Title
        assertEquals("block-sdt-title", sdtBlock.getSdtPr().getTitle());

        sdtBlock.getSdtPr().setTitle("new-block-title");
        assertEquals("new-block-title", sdtBlock.getSdtPr().getTitle());

        // Lock
        assertEquals(STLock.Enum.forInt(STLock.INT_SDT_CONTENT_LOCKED), sdtBlock.getSdtPr().getLock());

        sdtBlock.getSdtPr().setLock(STLock.UNLOCKED);
        assertEquals(STLock.UNLOCKED, sdtBlock.getSdtPr().getLock());

        // SdtContent
        assertEquals(
                "Some content1",
                sdtBlock.getContent()
                        .getTables()
                        .get(0)
                        .getRows()
                        .get(0)
                        .getCell(0)
                        .getText()
        );
    }
}
