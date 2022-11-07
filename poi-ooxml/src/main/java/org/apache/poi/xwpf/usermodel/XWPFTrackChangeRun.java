package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRunTrackChange;

public class XWPFTrackChangeRun extends XWPFRun {
    private CTRunTrackChange trackChange;

    public XWPFTrackChangeRun(CTRunTrackChange trackChange, CTR r, IRunBody p) {
        super(r, p);
        this.trackChange = trackChange;
    }

    public CTRunTrackChange getTrackChange() {
        return this.trackChange;
    }

    public String getAuthor() {
        return this.trackChange.getAuthor();
    }

    public void setAuthor(String author) {
        this.trackChange.setAuthor(author);
    }

    public java.util.Calendar getDate() {
        return this.trackChange.getDate();
    }

    public void setDate(java.util.Calendar date) {
        this.trackChange.setDate(date);
    }

    public boolean isDel() {
        return this.trackChange.getDomNode().getLocalName().equals("del");
    }

    public boolean isIns() {
        return this.trackChange.getDomNode().getLocalName().equals("ins");
    }
}
