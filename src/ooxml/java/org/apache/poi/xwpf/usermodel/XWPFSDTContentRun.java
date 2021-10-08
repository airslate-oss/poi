package org.apache.poi.xwpf.usermodel;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XWPFSDTContentRun implements ISDTContent, ISDTContentRun {

    private IRunBody parent;
    private CTSdtContentRun ctContentRun;
    private List<XWPFRun> runs = new ArrayList<>();
    private List<IRunElement> iruns = new ArrayList<>();

    public XWPFSDTContentRun(CTSdtContentRun ctContentRun, IRunBody parent) {
        if (ctContentRun == null) {
            return;
        }
        this.ctContentRun = ctContentRun;
        this.parent = parent;

        buildRunsInOrderFromXml(this.ctContentRun);
    }

    public List<IRunElement> getIRuns() {
        return Collections.unmodifiableList(iruns);
    }

    public List<XWPFRun> getRuns() {
        return Collections.unmodifiableList(runs);
    }

    private void buildRunsInOrderFromXml(XmlObject object) {
        XmlCursor c = object.newCursor();
        c.selectPath("child::*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTR) {
                XWPFRun r = new XWPFRun((CTR) o, parent);
                runs.add(r);
                iruns.add(r);
            }
            if (o instanceof CTHyperlink) {
                CTHyperlink link = (CTHyperlink)o;
                for (CTR r : link.getRArray()) {
                    XWPFHyperlinkRun hr = new XWPFHyperlinkRun(link, r, parent);
                    runs.add(hr);
                    iruns.add(hr);
                }
            }
            if (o instanceof CTSimpleField) {
                CTSimpleField field = (CTSimpleField)o;
                for (CTR r : field.getRArray()) {
                    XWPFFieldRun fr = new XWPFFieldRun(field, r, parent);
                    runs.add(fr);
                    iruns.add(fr);
                }
            }
            if (o instanceof CTSdtRun) {
                XWPFSDTRun cc = new XWPFSDTRun((CTSdtRun) o, parent);
                iruns.add(cc);
            }
            if (o instanceof CTRunTrackChange) {
                for (CTR r : ((CTRunTrackChange) o).getRArray()) {
                    XWPFRun cr = new XWPFRun(r, parent);
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
        c.dispose();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public XWPFRun createRun() {
        XWPFRun xwpfRun = new XWPFRun(ctContentRun.addNewR(), parent);
        runs.add(xwpfRun);
        iruns.add(xwpfRun);
        return xwpfRun;
    }

    @Override
    public IRunElement cloneExistingIRunElement(IRunElement elem) {
        if (elem instanceof XWPFRun) {
            CTR ctr = ctContentRun.addNewR();
            ctr.set(((XWPFRun) elem).getCTR());
            XWPFRun r = new XWPFRun(ctr, parent);
            runs.add(r);
            iruns.add(r);
            return r;
        }
        return null;
    }

    /**
     * Implementation may be based on {@link XWPFParagraph#removeRun(int)}
     * @param pos
     * @return
     */
    @Override
    public boolean removeIRunElement(int pos) {
        throw new UnsupportedOperationException();
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
}
