package org.apache.poi.xwpf.usermodel;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.xmlbeans.XmlCursor;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLock;

import java.io.IOException;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.*;

/**
 * Test class for manipulation of inline Content Controls.
 * Related classes are:
 *      {@link XWPFSDTRun}, {@link XWPFSDTPr}, {@link XWPFSDTContentRun}
 */
public final class TestXWPFSDTRun {

    /**
     * Verify that Sdt Run Pr is added to Sdt Run
     * and the related object references were updated
     */
    @Test
    public void testSdtRunCreateSdtPr() {
        XWPFDocument doc = new XWPFDocument();
        XWPFSDTRun sdtRun = doc.createParagraph().createSdtRun();
        XWPFSDTPr sdtPr = sdtRun.createSdtPr();

        XmlCursor cur = sdtRun.getCtSdtRun().newCursor();
        cur.toFirstChild();

        assertEquals(sdtPr.getSdtPr(), cur.getObject());
    }

    /**
     * Verify that Sdt Run Content is added to Sdt Run
     * and the related object references were updated
     */
    @Test
    public void testSdtRunCreateSdtContentRun() {
        XWPFDocument doc = new XWPFDocument();
        XWPFSDTRun sdtRun = doc.createParagraph().createSdtRun();
        XWPFSDTContentRun sdtContent = sdtRun.createSdtContent();

        XmlCursor cur = sdtRun.getCtSdtRun().newCursor();
        cur.toFirstChild();

        assertEquals(sdtContent.getCtContentRun(), cur.getObject());
    }

    /**
     * Verify that Run is created inside Sdt Run Content
     * and the collections are updated relatively
     */
    @Test
    public void testCreateRunInsideSdtContent() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();
        XWPFSDTRun sdt = p.createSdtRun();

        XWPFSDTContentRun sdtContent = sdt.createSdtContent();
        XWPFRun run = sdtContent.createRun();
        run.setText("text in SDT");

        assertEquals(1, sdt.getContent().getIRuns().size());
        assertEquals(1, sdt.getContent().getRuns().size());
        assertEquals("text in SDT", ((XWPFRun) sdt.getContent().getIRuns().get(0)).getText(0));
    }

    /**
     * Insert Sdt Run between chosen Run in paragraph
     * Then copy the content of this Run to Sdt Run Content
     * Then delete the Run
     * Verify that Run was "wrapped" in Sdt Run
     *
     * @throws IOException
     */
    @Test
    public void testInsertSDTRunBetweenRuns() throws IOException {
        XmlCursor cur = null;
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();
        p.createRun().setText("first ");
        XWPFRun run = p.createRun();
        run.setText("second ");
        run.setFontFamily("Times New Roman");
        run.setFontSize(40);
        p.createRun().setText("third ");

        XmlCursor curBefore = run.getCTR().newCursor();
        XWPFSDTRun sdtRunBefore = p.insertNewSDTRunByCursor(curBefore);

        XmlCursor curAfter = run.getCTR().newCursor();
        curAfter.toEndToken();
        curAfter.toNextToken();
        XWPFSDTRun sdtRunAfter = p.insertNewSDTRunByCursor(curAfter);

        cur = p.getCTP().newCursor();
        cur.toChild(1);
        cur.push();

        // verify that second element in paragraph is SDT
        assertTrue(cur.getObject() instanceof CTSdtRun);

        cur.toNextSibling(); // to next R
        cur.toNextSibling(); // to Sdt after R

        assertTrue(cur.getObject() instanceof CTSdtRun);

        // create Pr & Content for SDT
        XWPFSDTPr sdtPr = sdtRunBefore.createSdtPr();
        XWPFSDTContentRun sdtContent = sdtRunBefore.createSdtContent();

        sdtPr.setTag("new-inline-tag");
        sdtPr.setTitle("new-inline-title");
        sdtPr.setLock(STLock.SDT_CONTENT_LOCKED);

        // copy existing run to sdt content & remove run from Paragraph
        sdtContent.cloneExistingIRunElement(run);

        cur.pop();
        cur.toChild(1); // move to SdtContent
        cur.toFirstChild(); // select copied run

        assertTrue(cur.getObject() instanceof CTR);
        assertEquals("second ",  new XWPFRun((CTR) cur.getObject(), sdtRunBefore).getText(0));
        assertEquals("Times New Roman",  new XWPFRun((CTR) cur.getObject(), sdtRunBefore).getFontFamily());

        assertEquals(5, p.getIRuns().size());
        assertEquals(3, p.getRuns().size());
        assertEquals(XWPFSDTRun.class, p.getIRuns().get(1).getClass());
        assertEquals(XWPFSDTRun.class, p.getIRuns().get(3).getClass());
    }

    /**
     * Verify that existing Content Control in document is correctly
     * unmarshalled & we can proceed with modifying its content
     * @throws Exception
     */
    @Test
    public void testUnmarshallingSdtRun() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("blockAndInlineSdtTags.docx");
        XWPFParagraph paragraph = doc.getParagraphArray(0);
        XWPFSDTRun sdtRun = (XWPFSDTRun) paragraph.getIRuns().get(1);

        // Tag
        assertEquals("inline-sdt-tag", sdtRun.getSdtPr().getTag());

        sdtRun.getSdtPr().setTag("new-inline-tag");
        assertEquals("new-inline-tag", sdtRun.getSdtPr().getTag());

        // Title
        assertEquals("inline-sdt-title", sdtRun.getSdtPr().getTitle());

        sdtRun.getSdtPr().setTitle("new-inline-title");
        assertEquals("new-inline-title", sdtRun.getSdtPr().getTitle());

        // Lock
        assertEquals(STLock.SDT_CONTENT_LOCKED, sdtRun.getSdtPr().getLock());

        sdtRun.getSdtPr().setLock(STLock.SDT_LOCKED);
        assertEquals(STLock.SDT_LOCKED, sdtRun.getSdtPr().getLock());

        // SdtContent
        assertEquals("inline-sdt", sdtRun.getContent().getRuns().get(0).getText(0));

        sdtRun.getContent().getRuns().get(0).setText("new-inline-sdt", 0);
        assertEquals("new-inline-sdt", sdtRun.getContent().getRuns().get(0).getText(0));
    }
}
