package org.apache.poi.xwpf.usermodel;

import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;

import java.util.List;

/**
 * Interface for Block level SDT that describes
 * common operations with its Content
 */
public interface ISDTContentBlock {

    /**
     * @return Collection of paragraphs
     */
    List<XWPFParagraph> getParagraphs();

    /**
     * get the paragraph with the CTP class p
     *
     * @param p
     * @return a paragraph {@link XWPFParagraph} or null
     */
    XWPFParagraph getParagraph(CTP p);

    /**
     * Add a new paragraph at position of the cursor. The cursor must be on the
     * {@link org.apache.xmlbeans.XmlCursor.TokenType#START} tag of an subelement
     * of the documents body. When this method is done, the cursor passed as
     * parameter points to the {@link org.apache.xmlbeans.XmlCursor.TokenType#END}
     * of the newly inserted paragraph.
     *
     * @param cursor The cursor-position where the new paragraph should be added.
     * @return the {@link XWPFParagraph} object representing the newly inserted
     * CTP object
     */
    XWPFParagraph insertNewParagraph(XmlCursor cursor);

    /**
     * @return Collection of paragraphs
     */
    List<XWPFTable> getTables();

    /**
     * get a table by its CTTbl-Object
     *
     * @param ctTbl
     * @return a table by its CTTbl-Object or null
     * @see org.apache.poi.xwpf.usermodel.IBody#getTable(org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl)
     */
    XWPFTable getTable(CTTbl ctTbl);

    XWPFTable insertNewTbl(XmlCursor cursor);

    /**
     * Clone existing {@link IBodyElement} to content & return ref to it
     *
     * @param elem
     * @return
     */
    IBodyElement cloneExistingIBodyElement(IBodyElement elem);

    /**
     * Removes {@link IBodyElement} from content by its position in {@link XWPFSDTContentBlock#bodyElements}
     *
     * @param pos
     * @return
     */
    boolean removeIBodyElement(int pos);
}
