package org.apache.poi.xwpf.usermodel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;

public class XWPFSDTRun extends XWPFAbstractSDT
        implements IRunBody, IRunElement, ISDTContentsRun {

    private CTSdtRun ctSdtRun;
    private XWPFSDTContentRun contentRun;
    private IRunBody parent;

    public XWPFSDTRun(CTSdtRun ctSdtRun, IRunBody parent) {
        super(ctSdtRun.getSdtPr());
        this.parent = parent;
        this.ctSdtRun = ctSdtRun;
        this.contentRun = new XWPFSDTContentRun(ctSdtRun.getSdtContent(), this);
    }

    @Override
    public XWPFSDTContentRun getContent() {
        return this.contentRun;
    }

    public XWPFSDTContentRun createSdtContent() {
        XWPFSDTContentRun xwpfsdtContentRun = new XWPFSDTContentRun(this.ctSdtRun.addNewSdtContent(), this);
        this.contentRun = xwpfsdtContentRun;
        return xwpfsdtContentRun;
    }

    @Override
    public XWPFSDTPr createSdtPr() {
        XWPFSDTPr xwpfsdtPr = new XWPFSDTPr(this.ctSdtRun.addNewSdtPr());
        this.sdtPr = xwpfsdtPr;
        return xwpfsdtPr;
    }

    /**
     * Get the currently referenced paragraph/SDT object
     *
     * @return current parent
     */
    public IRunBody getParent() {
        return parent;
    }

    @Override
    public XWPFDocument getDocument() {
        return parent.getDocument();
    }

    @Override
    public POIXMLDocumentPart getPart() {
        return parent.getPart();
    }

    public CTSdtRun getCtSdtRun() {
        return ctSdtRun;
    }
}
