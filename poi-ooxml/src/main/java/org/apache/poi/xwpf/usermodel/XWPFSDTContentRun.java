package org.apache.poi.xwpf.usermodel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class XWPFSDTContentRun implements ISDTContent, IRunBody {
    private IRunElement parent;
    private CTSdtContentRun ctContentRun;
    private final List<XWPFRun> runs = new ArrayList<>();
    private final List<IRunElement> iruns = new ArrayList<>();
    private final List<XWPFSDTRun> sdtRuns = new ArrayList<>();

    public XWPFSDTContentRun(CTSdtContentRun ctContentRun, IRunElement parent) {
        if (ctContentRun == null) {
            return;
        }
        this.ctContentRun = ctContentRun;
        this.parent = parent;

        buildRunsInOrderFromXml(this.ctContentRun);
    }

    @Override
    public List<IRunElement> getIRuns() {
        return Collections.unmodifiableList(iruns);
    }

    @Override
    public List<XWPFRun> getRuns() {
        return Collections.unmodifiableList(runs);
    }

    @Override
    public List<XWPFSDTRun> getSDTRuns() {
        return Collections.unmodifiableList(sdtRuns);
    }

    @Override
    public XWPFHyperlinkRun insertNewHyperlinkRun(int pos, String uri) {
        if (pos == runs.size()) {
            return createHyperlinkRun(uri);
        }
        XWPFHyperlinkRun newRun = insertNewProvidedRun(pos, newCursor -> {
            String namespaceURI = CTHyperlink.type.getName().getNamespaceURI();
            String localPart = "hyperlink";
            newCursor.beginElement(localPart, namespaceURI);
            // move the cursor to the START token to the hyperlink just created
            newCursor.toParent();
            CTHyperlink ctHyperLink = (CTHyperlink) newCursor.getObject();
            return new XWPFHyperlinkRun(ctHyperLink, ctHyperLink.addNewR(), this);
        });

        if (newRun != null) {
            String rId = getPart().getPackagePart().addExternalRelationship(
                    uri, XWPFRelation.HYPERLINK.getRelation()
            ).getId();
            newRun.getCTHyperlink().setId(rId);
        }

        return newRun;
    }

    @Override
    public XWPFHyperlinkRun createHyperlinkRun(String uri) {
        // Create a relationship ID for this link.
        String rId = getPart().getPackagePart().addExternalRelationship(
                uri, XWPFRelation.HYPERLINK.getRelation()
        ).getId();

        // Create the run.
        CTHyperlink ctHyperLink = getCtContentRun().addNewHyperlink();
        ctHyperLink.setId(rId);
        ctHyperLink.addNewR();

        // Append this run to the paragraph.
        XWPFHyperlinkRun link = new XWPFHyperlinkRun(ctHyperLink, ctHyperLink.getRArray(0), this);
        runs.add(link);
        iruns.add(link);
        return link;
    }

    @Override
    public XWPFFieldRun insertNewFieldRun(int pos) {
        if (pos == runs.size()) {
            return createFieldRun();
        }
        return insertNewProvidedRun(pos, newCursor -> {
            String uri = CTSimpleField.type.getName().getNamespaceURI();
            String localPart = "fldSimple";
            newCursor.beginElement(localPart, uri);
            // move the cursor to the START token to the field just created
            newCursor.toParent();
            CTSimpleField ctSimpleField = (CTSimpleField) newCursor.getObject();
            return new XWPFFieldRun(ctSimpleField, ctSimpleField.addNewR(), this);
        });
    }

    @Override
    public XWPFFieldRun createFieldRun() {
        CTSimpleField ctSimpleField = ctContentRun.addNewFldSimple();
        XWPFFieldRun newRun = new XWPFFieldRun(ctSimpleField, ctSimpleField.addNewR(), this);
        runs.add(newRun);
        iruns.add(newRun);
        return newRun;
    }

    @Override
    public XWPFRun insertNewRun(int pos) {
        if (pos == runs.size()) {
            return createRun();
        }
        return insertNewProvidedRun(pos, newCursor -> {
            String uri = CTR.type.getName().getNamespaceURI();
            String localPart = "r";
            // creates a new run, cursor is positioned inside the new
            // element
            newCursor.beginElement(localPart, uri);
            // move the cursor to the START token to the run just created
            newCursor.toParent();
            CTR r = (CTR) newCursor.getObject();
            return new XWPFRun(r, (IRunBody)this);
        });
    }

    /**
     * insert a new run provided by  in all runs
     *
     * @param <T> XWPFRun or XWPFHyperlinkRun or XWPFFieldRun
     * @param pos The position at which the new run should be added.
     * @param provider provide a new run at position of the given cursor.
     * @return the inserted run or null if the given pos is out of bounds.
     */
    private <T extends XWPFRun> T insertNewProvidedRun(int pos, Function<XmlCursor, T> provider) {
        if (pos >= 0 && pos < runs.size()) {
            XWPFRun run = runs.get(pos);
            CTR ctr = run.getCTR();
            try (XmlCursor newCursor = ctr.newCursor()) {
                if (!isCursorInSdtContent(newCursor)) {
                    // look up correct position for CTP -> XXX -> R array
                    newCursor.toParent();
                }
                if (isCursorInSdtContent(newCursor)) {
                    // provide a new run
                    T newRun = provider.apply(newCursor);

                    // To update the iruns, find where we're going
                    // in the normal runs, and go in there
                    int iPos = iruns.size();
                    int oldAt = iruns.indexOf(run);
                    if (oldAt != -1) {
                        iPos = oldAt;
                    }
                    iruns.add(iPos, newRun);
                    // Runs itself is easy to update
                    runs.add(pos, newRun);
                    return newRun;
                }
            }
        }
        return null;
    }

    /**
     * verifies that cursor is on the right position
     */
    private boolean isCursorInSdtContent(XmlCursor cursor) {
        try (XmlCursor verify = cursor.newCursor()) {
            verify.toParent();
            return verify.getObject() == this.ctContentRun;
        }
    }

    private void buildRunsInOrderFromXml(XmlObject object) {
        try (XmlCursor c = object.newCursor()) {
            c.selectPath("child::*");
            while (c.toNextSelection()) {
                XmlObject o = c.getObject();
                if (o instanceof CTR) {
                    XWPFRun r = new XWPFRun((CTR) o, this);
                    runs.add(r);
                    iruns.add(r);
                }
                if (o instanceof CTHyperlink) {
                    CTHyperlink link = (CTHyperlink) o;
                    for (CTR r : link.getRArray()) {
                        XWPFHyperlinkRun hr = new XWPFHyperlinkRun(link, r, this);
                        runs.add(hr);
                        iruns.add(hr);
                    }
                }
                if (o instanceof CTSimpleField) {
                    CTSimpleField field = (CTSimpleField) o;
                    for (CTR r : field.getRArray()) {
                        XWPFFieldRun fr = new XWPFFieldRun(field, r, this);
                        runs.add(fr);
                        iruns.add(fr);
                    }
                }
                if (o instanceof CTSdtRun) {
                    XWPFSDTRun cc = new XWPFSDTRun((CTSdtRun) o, this);
                    iruns.add(cc);
                    sdtRuns.add(cc);
                }
                if (o instanceof CTRunTrackChange) {
                    for (CTR r : ((CTRunTrackChange) o).getRArray()) {
                        XWPFRun cr = new XWPFRun(r, this);
                        runs.add(cr);
                        iruns.add(cr);
                    }
                }
                if (o instanceof CTSmartTagRun) {
                    // Smart Tags can be nested many times.
                    // This implementation does not preserve the tagging information
                    buildRunsInOrderFromXml(o);
                }
                if (o instanceof CTRunTrackChange) {
                    // add all the insertions as text
                    for (CTRunTrackChange change : ((CTRunTrackChange) o).getInsArray()) {
                        buildRunsInOrderFromXml(change);
                    }
                }
            }
        }
    }

    public XWPFRun createRun() {
        XWPFRun xwpfRun = new XWPFRun(ctContentRun.addNewR(), this);
        runs.add(xwpfRun);
        iruns.add(xwpfRun);
        return xwpfRun;
    }

    @Override
    public XWPFSDTRun insertNewSDTRunByCursor(XmlCursor cursor) {
        if (isCursorInSdtContent(cursor)) {
            String uri = CTSdtRun.type.getName().getNamespaceURI();
            String localPart = "sdt";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTSdtRun sdt = (CTSdtRun) cursor.getObject();
            XWPFSDTRun newSdtRun = new XWPFSDTRun(sdt, this);
            XmlObject o = null;
            while (!(o instanceof CTSdtRun) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if (!(o instanceof CTSdtRun)) {
                sdtRuns.add(0, newSdtRun);
            } else {
                int pos = sdtRuns.indexOf(getSDTRun((CTSdtRun) o)) + 1;
                sdtRuns.add(pos, newSdtRun);
            }
            int i = 0;
            try (XmlCursor sdtCursor = sdt.newCursor()) {
                cursor.toCursor(sdtCursor);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTR || o instanceof CTSdtRun || o instanceof CTHyperlink || o instanceof CTSimpleField) {
                        i++;
                    }
                }
                iruns.add(i, newSdtRun);
                cursor.toCursor(sdtCursor);
                cursor.toEndToken();
                return newSdtRun;
            }
        }
        return null;
    }

    private XWPFSDTRun getSDTRun(CTSdtRun ctSdtRun) {
        for (int i = 0; i < sdtRuns.size(); i++) {
            if (getSDTRuns().get(i).getCtSdtRun() == ctSdtRun) {
                return getSDTRuns().get(i);
            }
        }
        return null;
    }

    @Override
    public void setSDTRun(int pos, XWPFSDTRun sdt) {
        sdtRuns.set(pos, sdt);
        ctContentRun.setSdtArray(pos, sdt.getCtSdtRun());
    }

    @Override
    public XWPFSDTRun createSdtRun() {
        XWPFSDTRun sdtRun = new XWPFSDTRun(ctContentRun.addNewSdt(), this);
        sdtRuns.add(sdtRun);
        iruns.add(sdtRun);
        return sdtRun;
    }

    public IRunElement cloneExistingIRunElement(IRunElement elem) {
        if (elem instanceof XWPFHyperlinkRun) {
            CTHyperlink ctHyperlink = ctContentRun.addNewHyperlink();
            ctHyperlink.set(((XWPFHyperlinkRun) elem).getCTHyperlink());
            XWPFHyperlinkRun hyperlinkRun = new XWPFHyperlinkRun(ctHyperlink, ctHyperlink.getRArray(0), this);
            runs.add(hyperlinkRun);
            iruns.add(hyperlinkRun);
            return hyperlinkRun;
        } else if (elem instanceof XWPFFieldRun) {
            CTSimpleField ctSimpleField = ctContentRun.addNewFldSimple();
            ctSimpleField.set(((XWPFFieldRun) elem).getCTField());
            XWPFFieldRun fieldRun = new XWPFFieldRun(ctSimpleField, ctSimpleField.getRArray(0), this);
            runs.add(fieldRun);
            iruns.add(fieldRun);
            return fieldRun;
        } else if (elem instanceof XWPFRun) {
            CTR ctr = ctContentRun.addNewR();
            ctr.set(((XWPFRun) elem).getCTR());
            XWPFRun run = new XWPFRun(ctr, this);
            runs.add(run);
            iruns.add(run);
            return run;
        } else if (elem instanceof XWPFSDTRun) {
            CTSdtRun ctSdtRun = ctContentRun.addNewSdt();
            ctSdtRun.set(((XWPFSDTRun) elem).getCtSdtRun());
            XWPFSDTRun sdtRun = new XWPFSDTRun(ctSdtRun, this);
            iruns.add(sdtRun);
            return sdtRun;
        }
        return null;
    }

    /**
     * Is there only one ctHyperlink in all runs
     *
     * @param run hyperlink run
     */
    private boolean isTheOnlyCTHyperlinkInRuns(XWPFHyperlinkRun run) {
        CTHyperlink ctHyperlink = run.getCTHyperlink();
        long count = runs.stream().filter(r -> (r instanceof XWPFHyperlinkRun
                        && ctHyperlink == ((XWPFHyperlinkRun) r).getCTHyperlink()))
                .count();
        return count <= 1;
    }

    /**
     * Is there only one ctField in all runs
     *
     * @param run field run
     */
    private boolean isTheOnlyCTFieldInRuns(XWPFFieldRun run) {
        CTSimpleField ctField = run.getCTField();
        long count = runs.stream().filter(r -> (r instanceof XWPFFieldRun
                && ctField == ((XWPFFieldRun) r).getCTField())).count();
        return count <= 1;
    }

    /**
     * removes a Run at the position pos in the paragraph
     *
     * @return true if the run was removed
     */
    @Override
    public boolean removeRun(int pos) {
        if (pos >= 0 && pos < runs.size()) {
            XWPFRun run = runs.get(pos);
            // CTP -> CTHyperlink -> R array
            if (run instanceof XWPFHyperlinkRun
                    && isTheOnlyCTHyperlinkInRuns((XWPFHyperlinkRun) run)) {
                try (XmlCursor c = ((XWPFHyperlinkRun) run).getCTHyperlink().newCursor()) {
                    c.removeXml();
                }
                runs.remove(pos);
                iruns.remove(run);
                return true;
            }
            // CTP -> CTField -> R array
            if (run instanceof XWPFFieldRun
                    && isTheOnlyCTFieldInRuns((XWPFFieldRun) run)) {
                try (XmlCursor c = ((XWPFFieldRun) run).getCTField().newCursor()) {
                    c.removeXml();
                }
                runs.remove(pos);
                iruns.remove(run);
                return true;
            }
            try (XmlCursor c = run.getCTR().newCursor()) {
                c.removeXml();
            }
            runs.remove(pos);
            iruns.remove(run);
            return true;
        }
        return false;
    }

    /**
     * Remove Sdt Run by its position in iruns collection
     *
     * @return true if element was removed
     */
    @Override
    public boolean removeSdtRun(int irunPos) {
        if (irunPos >= 0 && irunPos < iruns.size()) {
            IRunElement sdtRun = iruns.get(irunPos);

            if (sdtRun instanceof XWPFSDTRun) {
                try (XmlCursor c = ((XWPFSDTRun) sdtRun).getCtSdtRun().newCursor()) {
                    c.removeXml();
                }
                sdtRuns.remove(sdtRun);
                iruns.remove(sdtRun);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes elements {@link XWPFSDTRun}, {@link XWPFRun} from iruns collection
     *
     * @return true if element was removed
     */
    @Override
    public boolean removeIRunElement(int irunPos) {
        if (irunPos >= 0 && irunPos < iruns.size()) {
            IRunElement iRunElement = iruns.get(irunPos);

            if (iRunElement instanceof XWPFSDTRun) {
                return removeSdtRun(irunPos);
            }
            if (iRunElement instanceof XWPFRun) {
                return removeRun(runs.indexOf(iRunElement));
            }
        }
        return false;
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        boolean addNewLine = false;
        for (int i = 0; i < iruns.size(); i++) {
            Object o = iruns.get(i);
            if (o instanceof XWPFSDTRun) {
                text.append(((XWPFSDTRun) o).getContent().getText());
                addNewLine = true;
            } else if (o instanceof XWPFRun) {
                text.append(o);
                addNewLine = false;
            }
            if (addNewLine && i < iruns.size() - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }

    @Override
    public String toString() {
        return getText();
    }

    public CTSdtContentRun getCtContentRun() {
        return ctContentRun;
    }

    @Override
    public XWPFDocument getDocument() {
        return parent.getDocument();
    }

    @Override
    public POIXMLDocumentPart getPart() {
        if (parent != null) {
            return parent.getPart();
        }
        return null;
    }

    public IRunElement getParent() {
        return parent;
    }
}
