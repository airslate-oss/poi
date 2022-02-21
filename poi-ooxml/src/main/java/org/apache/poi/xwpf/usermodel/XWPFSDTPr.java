package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

/**
 * @author byy
 * Rudimentary class for SDT processing
 * Represents Content Control properties
 */
public class XWPFSDTPr {

    private CTSdtPr sdtPr;

    public XWPFSDTPr(CTSdtPr pr) {
        if (pr == null) {
            return;
        }
        this.sdtPr = pr;
    }

    public CTSdtPr getSdtPr() {
        return sdtPr;
    }

    public void setSdtPr(CTSdtPr sdtPr) {
        this.sdtPr = sdtPr;
    }

    /**
     * @return first SDT Title
     */
    public String getTitle() {
        return (sdtPr != null && sdtPr.isSetAlias()) ? sdtPr.getAlias().getVal() : null;
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        if (sdtPr != null && !sdtPr.isSetAlias()) {
            CTString cttag = sdtPr.addNewAlias();
            cttag.setVal(title);
            sdtPr.setAlias(cttag);
        } else {
            sdtPr.getAlias().setVal(title);
        }
    }

    /**
     * @return first SDT Tag
     */
    public String getTag() {
        return (sdtPr != null && sdtPr.isSetTag()) ? sdtPr.getTag().getVal() : null;
    }

    /**
     * @param tag
     */
    public void setTag(String tag) {
        if (sdtPr != null && !sdtPr.isSetTag()) {
            CTString cttag = CTString.Factory.newInstance();
            cttag.setVal(tag);
            sdtPr.setTag(cttag);
        } else {
            sdtPr.getTag().setVal(tag);
        }
    }

    public STLock.Enum getLock() {
        return (sdtPr != null && sdtPr.isSetLock()) ? sdtPr.getLock().getVal() : null;
    }

    public void setLock(STLock.Enum lock) {
        CTLock ctLock = CTLock.Factory.newInstance();
        ctLock.setVal(lock);

        if (sdtPr != null && !sdtPr.isSetLock()) {
            sdtPr.setLock(ctLock);
        } else {
            sdtPr.getLock().set(ctLock);
        }
    }
}
