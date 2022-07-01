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
    private List<XWPFSDTRun> sdtRuns = new ArrayList<>();

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

    public List<XWPFSDTRun> getSDTRuns() {
        return Collections.unmodifiableList(sdtRuns);
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
                sdtRuns.add(cc);
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

    @Override
    public XWPFRun createRun() {
        XWPFRun xwpfRun = new XWPFRun(ctContentRun.addNewR(), parent);
        runs.add(xwpfRun);
        iruns.add(xwpfRun);
        return xwpfRun;
    }

    public void setSDTRun(int pos, XWPFSDTRun sdt) {
        sdtRuns.set(pos, sdt);
        ctContentRun.setSdtArray(pos, sdt.getCtSdtRun());
    }

    @Override
    public XWPFSDTRun createSdtRun() {
        XWPFSDTRun sdtRun = new XWPFSDTRun(ctContentRun.addNewSdt(), parent);
        sdtRuns.add(sdtRun);
        iruns.add(sdtRun);
        return sdtRun;
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
        } else if (elem instanceof XWPFSDTRun) {
            CTSdtRun ctSdtRun = ctContentRun.addNewSdt();
            ctSdtRun.set(((XWPFSDTRun) elem).getCtSdtRun());
            XWPFSDTRun sdtRun = new XWPFSDTRun(ctSdtRun, parent);
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
     * @param pos
     * @return true if the run was removed
     */
    public boolean removeRun(int pos) {
        if (pos >= 0 && pos < runs.size()) {
            XWPFRun run = runs.get(pos);
            // CTP -> CTHyperlink -> R array
            if (run instanceof XWPFHyperlinkRun
                    && isTheOnlyCTHyperlinkInRuns((XWPFHyperlinkRun) run)) {
                XmlCursor c = ((XWPFHyperlinkRun) run).getCTHyperlink()
                        .newCursor();
                c.removeXml();
                c.dispose();
                runs.remove(pos);
                iruns.remove(run);
                return true;
            }
            // CTP -> CTField -> R array
            if (run instanceof XWPFFieldRun
                    && isTheOnlyCTFieldInRuns((XWPFFieldRun) run)) {
                XmlCursor c = ((XWPFFieldRun) run).getCTField().newCursor();
                c.removeXml();
                c.dispose();
                runs.remove(pos);
                iruns.remove(run);
                return true;
            }
            XmlCursor c = run.getCTR().newCursor();
            c.removeXml();
            c.dispose();
            runs.remove(pos);
            iruns.remove(run);
            return true;
        }
        return false;
    }

    /**
     * Remove Sdt Run by its position in iruns collection
     *
     * @param irunPos
     * @return true if element was removed
     */
    public boolean removeSdtRun(int irunPos) {
        if (irunPos >= 0 && irunPos < iruns.size()) {
            IRunElement sdtRun = iruns.get(irunPos);

            if (sdtRun instanceof XWPFSDTRun) {
                XmlCursor c = ((XWPFSDTRun) sdtRun).getCtSdtRun().newCursor();
                c.removeXml();
                c.dispose();
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
     * @param irunPos
     * @return true if element was removed
     */
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
}
