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
        return trackChange;
    }

    public String getAuthor() {
        return trackChange.getAuthor();
    }

    public void setAuthor(String author) {
        trackChange.setAuthor(author);
    }

    public java.util.Calendar getDate() {
        return trackChange.getDate();
    }

    public void setDate(java.util.Calendar date) {
        trackChange.setDate(date);
    }

    public boolean isDel() {
        return trackChange.getDomNode().getLocalName().equals("del");
    }

    public boolean isIns() {
        return trackChange.getDomNode().getLocalName().equals("ins");
    }
}
