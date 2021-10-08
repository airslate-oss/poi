package org.apache.poi.xwpf.usermodel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;

public class XWPFSDTBlock extends XWPFAbstractSDT
        implements IBodyElement, IRunBody, ISDTContentsBlock {

    private CTSdtBlock ctSdtBlock;
    private XWPFSDTContentBlock contentBlock;
    private IBody part;

    public XWPFSDTBlock(CTSdtBlock sdtBlock, IBody part) {
        super(sdtBlock.getSdtPr());
        this.ctSdtBlock = sdtBlock;
        this.part = part;
        this.contentBlock = new XWPFSDTContentBlock(sdtBlock.getSdtContent(), part);
    }

    @Override
    public XWPFSDTContentBlock getContent() {
        return contentBlock;
    }

    public XWPFSDTContentBlock createSdtContent() {
        XWPFSDTContentBlock xwpfsdtContentBlock = new XWPFSDTContentBlock(this.ctSdtBlock.addNewSdtContent(), part);
        this.contentBlock = xwpfsdtContentBlock;
        return xwpfsdtContentBlock;
    }

    public XWPFSDTPr createSdtPr() {
        XWPFSDTPr xwpfsdtPr = new XWPFSDTPr(this.ctSdtBlock.addNewSdtPr());
        this.sdtPr = xwpfsdtPr;
        return xwpfsdtPr;
    }
    /**
     * @return null
     */
    public IBody getBody() {
        return part;
    }

    /**
     * @return document part
     */
    public POIXMLDocumentPart getPart() {
        return part.getPart();
    }

    /**
     * @return partType
     */
    public BodyType getPartType() {
        return BodyType.CONTENTCONTROL;
    }

    /**
     * @return element type
     */
    public BodyElementType getElementType() {
        return BodyElementType.CONTENTCONTROL;
    }

    public XWPFDocument getDocument() {
        return part.getXWPFDocument();
    }

    public CTSdtBlock getCtSdtBlock() {
        return ctSdtBlock;
    }
}
