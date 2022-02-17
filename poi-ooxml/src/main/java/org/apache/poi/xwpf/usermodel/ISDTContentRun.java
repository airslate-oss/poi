package org.apache.poi.xwpf.usermodel;

public interface ISDTContentRun {
    /**
     * Appends a new run to SDT content
     *
     * @return a new text run
     */
    XWPFRun createRun();
    /**
     * Clone existing {@link IRunElement} to content & return ref to it
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
