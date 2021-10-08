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
        if (sdtPr == null || sdtPr.getAliasList() == null) {
            return null;
        }
        return sdtPr.getAliasList().size() > 0
                ? sdtPr.getAliasList().get(0).getVal()
                : null;
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        if (sdtPr.getAliasList() != null) {
            if (sdtPr.getAliasList().size() == 0) {
                CTString cttag = sdtPr.addNewAlias();;
                cttag.setVal(title);
                sdtPr.setAliasArray(0, cttag);
            } else {
                sdtPr.getAliasList().get(0).setVal(title);
            }
        }
    }

    /**
     * @return first SDT Tag
     */
    public String getTag() {
        if (sdtPr == null || sdtPr.getTagList() == null) {
            return null;
        }
        return sdtPr.getTagList().size() > 0
                ? sdtPr.getTagList().get(0).getVal()
                : null;
    }

    /**
     * @param tag
     */
    public void setTag(String tag) {
        if (sdtPr.getTagList().size() == 0) {
            CTString cttag = CTString.Factory.newInstance();
            cttag.setVal(tag);
            sdtPr.getTagList().add(cttag);
        } else {
            sdtPr.getTagList().get(0).setVal(tag);
        }
    }

    public STLock.Enum getLock() {
        if (sdtPr == null || sdtPr.getLockList() == null) {
            return null;
        }
        return sdtPr.getLockList().size() > 0 ? sdtPr.getLockList().get(0).getVal() : null;
    }

    public void setLock(STLock.Enum lock) {
        CTLock ctLock = CTLock.Factory.newInstance();
        ctLock.setVal(lock);
        if (sdtPr.getLockList().size() == 0) {
            sdtPr.getLockList().add(ctLock);
        } else {
            sdtPr.getLockList().set(0, ctLock);
        }
    }
}
