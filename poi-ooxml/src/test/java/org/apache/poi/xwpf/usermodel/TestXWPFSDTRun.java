package org.apache.poi.xwpf.usermodel;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.xmlbeans.XmlCursor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLock;

import java.io.IOException;

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

        Assertions.assertEquals(sdtPr.getSdtPr(), cur.getObject());
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

        Assertions.assertEquals(sdtContent.getCtContentRun(), cur.getObject());
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

        Assertions.assertEquals(1, sdt.getContent().getIRuns().size());
        Assertions.assertEquals(1, sdt.getContent().getRuns().size());
        Assertions.assertEquals(0, sdt.getContent().getSDTRuns().size());
        Assertions.assertEquals("text in SDT", ((XWPFRun) sdt.getContent().getIRuns().get(0)).getText(0));
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
    public void testInsertSDTRunBetweenRuns() {
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
        Assertions.assertTrue(cur.getObject() instanceof CTSdtRun);

        cur.toNextSibling(); // to next R
        cur.toNextSibling(); // to Sdt after R

        Assertions.assertTrue(cur.getObject() instanceof CTSdtRun);

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

        Assertions.assertTrue(cur.getObject() instanceof CTR);
        Assertions.assertEquals("second ", new XWPFRun((CTR) cur.getObject(), sdtRunBefore).getText(0));
        Assertions.assertEquals("Times New Roman", new XWPFRun((CTR) cur.getObject(), sdtRunBefore).getFontFamily());

        Assertions.assertEquals(5, p.getIRuns().size());
        Assertions.assertEquals(3, p.getRuns().size());
        Assertions.assertEquals(2, p.getSDTRuns().size());
        Assertions.assertEquals(XWPFSDTRun.class, p.getIRuns().get(1).getClass());
        Assertions.assertEquals(XWPFSDTRun.class, p.getIRuns().get(3).getClass());
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
        Assertions.assertEquals("inline-sdt-tag", sdtRun.getSdtPr().getTag());

        sdtRun.getSdtPr().setTag("new-inline-tag");
        Assertions.assertEquals("new-inline-tag", sdtRun.getSdtPr().getTag());

        // Title
        Assertions.assertEquals("inline-sdt-title", sdtRun.getSdtPr().getTitle());

        sdtRun.getSdtPr().setTitle("new-inline-title");
        Assertions.assertEquals("new-inline-title", sdtRun.getSdtPr().getTitle());

        // Lock
        Assertions.assertEquals(STLock.SDT_CONTENT_LOCKED, sdtRun.getSdtPr().getLock());

        sdtRun.getSdtPr().setLock(STLock.SDT_LOCKED);
        Assertions.assertEquals(STLock.SDT_LOCKED, sdtRun.getSdtPr().getLock());

        // SdtContent
        Assertions.assertEquals("inline-sdt", sdtRun.getContent().getRuns().get(0).getText(0));

        sdtRun.getContent().getRuns().get(0).setText("new-inline-sdt", 0);
        Assertions.assertEquals("new-inline-sdt", sdtRun.getContent().getRuns().get(0).getText(0));
    }

    @Test
    public void testNestedSdtRun() {
        XWPFDocument document = new XWPFDocument();
        XWPFSDTContentRun sdtContent1 = document.createParagraph().createSdtRun().createSdtContent();
        sdtContent1.createRun();
        sdtContent1.createRun();

        XWPFSDTContentRun sdtContent2 = sdtContent1.createSdtRun().createSdtContent();

        XWPFSDTContentRun sdtContent3 = sdtContent2.createSdtRun().createSdtContent();
        sdtContent3.createSdtRun().createSdtContent();
        sdtContent3.createSdtRun().createSdtContent();
        sdtContent3.createSdtRun().createSdtContent();

        Assertions.assertEquals(1, document.getParagraphs().size());

        XWPFSDTContentRun actual = document.getParagraphs().get(0)
                .getSDTRuns()
                .get(0)
                .getContent();
        Assertions.assertEquals(2, actual.getRuns().size());
        Assertions.assertEquals(3, actual.getIRuns().size());
        Assertions.assertEquals(1, actual.getSDTRuns().size());

        actual = document.getParagraphs().get(0)
                .getSDTRuns()
                .get(0)
                .getContent()
                .getSDTRuns()
                .get(0)
                .getContent();
        Assertions.assertEquals(0, actual.getRuns().size());
        Assertions.assertEquals(1, actual.getIRuns().size());
        Assertions.assertEquals(1, actual.getSDTRuns().size());

        actual = document.getParagraphs().get(0)
                .getSDTRuns()
                .get(0)
                .getContent()
                .getSDTRuns()
                .get(0)
                .getContent()
                .getSDTRuns()
                .get(0)
                .getContent();
        Assertions.assertEquals(0, actual.getRuns().size());
        Assertions.assertEquals(3, actual.getIRuns().size());
        Assertions.assertEquals(3, actual.getSDTRuns().size());
    }
}
