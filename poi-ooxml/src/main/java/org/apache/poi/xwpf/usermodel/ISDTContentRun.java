package org.apache.poi.xwpf.usermodel;

import java.util.List;

public interface ISDTContentRun {
    /**
     * @return Collection of {@link IRunElement}
     */
    List<IRunElement> getIRuns();

    /**
     * @return Collection of {@link XWPFRun}
     */
    List<XWPFRun> getRuns();

    /**
     * @return Collection of {@link XWPFSDTRun}
     */
    List<XWPFSDTRun> getSDTRuns();

    /**
     * Appends a new {@link XWPFRun} to SDT content
     *
     * @return a new text {@link XWPFRun}
     */
    XWPFRun createRun();

    /**
     * Set a {@link XWPFRun} to SDT content
     */
    void setSDTRun(int pos, XWPFSDTRun sdt);

    /**
     * Appends a new {@link XWPFSDTRun} to SDT content
     *
     * @return a new {@link XWPFSDTRun}
     */
    XWPFSDTRun createSdtRun();

    /**
     * Clone existing {@link IRunElement} to content and return ref to it
     *
     * @param elem
     * @return
     */
    IRunElement cloneExistingIRunElement(IRunElement elem);

    /**
     * Removes {@link IRunElement} from content by its position in {@link XWPFSDTContentRun#iruns}
     *
     * @param pos
     * @return
     */
    boolean removeIRunElement(int pos);
}
