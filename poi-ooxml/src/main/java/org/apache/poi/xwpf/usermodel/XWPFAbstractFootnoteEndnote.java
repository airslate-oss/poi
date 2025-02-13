/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;

/**
 * Base class for both bottom-of-the-page footnotes {@link XWPFFootnote} and end
 * notes {@link XWPFEndnote}).
 * <p>The only significant difference between footnotes and
 * end notes is which part they go on. Footnotes are managed by the Footnotes part
 * {@link XWPFFootnotes} and end notes are managed by the Endnotes part {@link XWPFEndnotes}.</p>
 * @since 4.0.0
 */
public abstract class XWPFAbstractFootnoteEndnote  implements Iterable<XWPFParagraph>, IBody {

    private final List<XWPFParagraph> paragraphs = new ArrayList<>();
    private final List<XWPFTable> tables = new ArrayList<>();
    private final List<XWPFPictureData> pictures = new ArrayList<>();
    private final List<IBodyElement> bodyElements = new ArrayList<>();
    protected CTFtnEdn ctFtnEdn;
    protected XWPFAbstractFootnotesEndnotes footnotes;
    protected XWPFDocument document;
    private List<XWPFSDTBlock> sdtBlocks = new ArrayList<>();

    public XWPFAbstractFootnoteEndnote() {
        super();
    }

    @Internal
    protected XWPFAbstractFootnoteEndnote(XWPFDocument document, CTFtnEdn body) {
        ctFtnEdn = body;
        this.document = document;
        init();
    }

    @Internal
    protected XWPFAbstractFootnoteEndnote(CTFtnEdn note, XWPFAbstractFootnotesEndnotes footnotes) {
        this.footnotes = footnotes;
        ctFtnEdn = note;
        document = footnotes.getXWPFDocument();
        init();
    }

    protected void init() {
        try (XmlCursor cursor = ctFtnEdn.newCursor()) {
            //copied from XWPFDocument...should centralize this code
            //to avoid duplication
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTP) {
                    XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                    bodyElements.add(p);
                    paragraphs.add(p);
                } else if (o instanceof CTTbl) {
                    XWPFTable t = new XWPFTable((CTTbl) o, this);
                    bodyElements.add(t);
                    tables.add(t);
                } else if (o instanceof CTSdtBlock) {
                    XWPFSDTBlock c = new XWPFSDTBlock((CTSdtBlock) o, this);
                    bodyElements.add(c);
                    sdtBlocks.add(c);
                }

            }
        }
    }

    /**
     * Get the list of {@link XWPFParagraph}s in the footnote.
     * @return List of paragraphs
     */
    @Override
    public List<XWPFParagraph> getParagraphs() {
        return paragraphs;
    }

    /**
     * Get an iterator over the {@link XWPFParagraph}s in the footnote.
     * @return Iterator over the paragraph list.
     */
    @Override
    public Iterator<XWPFParagraph> iterator() {
        return paragraphs.iterator();
    }

    /**
     * Get a spliterator over the {@link XWPFParagraph}s in the footnote.
     * @return Spliterator over the paragraph list.
     *
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<XWPFParagraph> spliterator() {
        return paragraphs.spliterator();
    }

    /**
     * Get the list of {@link XWPFTable}s in the footnote.
     * @return List of tables
     */
    @Override
    public List<XWPFTable> getTables() {
        return tables;
    }

    @Override
    public List<XWPFSDTBlock> getSdtBlocks() {
        return sdtBlocks;
    }

    /**
     * Gets the list of {@link XWPFPictureData}s in the footnote.
     * @return List of pictures
     */
    public List<XWPFPictureData> getPictures() {
        return pictures;
    }

    /**
     * Gets the body elements ({@link IBodyElement}) of the footnote.
     * @return List of body elements.
     */
    @Override
    public List<IBodyElement> getBodyElements() {
        return bodyElements;
    }

    /**
     * Gets the underlying CTFtnEdn object for the footnote.
     * @return CTFtnEdn object
     */
    public CTFtnEdn getCTFtnEdn() {
        return ctFtnEdn;
    }

    /**
     * Set the underlying CTFtnEdn for the footnote.
     * <p>Use {@link XWPFDocument#createFootnote()} to create new footnotes.</p>
     * @param footnote The CTFtnEdn object that will underly the footnote.
     */
    public void setCTFtnEdn(CTFtnEdn footnote) {
        ctFtnEdn = footnote;
    }

    /**
     * Gets the {@link XWPFTable} at the specified position from the footnote's table array.
     * @param pos in table array
     * @return The {@link XWPFTable} at position pos, or null if there is no table at position pos.
     */
    @Override
    public XWPFTable getTableArray(int pos) {
        if (pos >= 0 && pos < tables.size()) {
            return tables.get(pos);
        }
        return null;
    }

    /**
     * Inserts an existing XWPFTable into the arrays bodyElements and tables.
     *
     * @param pos Position, in the bodyElements array, to insert the table
     * @param table XWPFTable to be inserted
     */
    @Override
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i = 0;
        for (CTTbl tbl : ctFtnEdn.getTblList()) {
            if (tbl == table.getCTTbl()) {
                break;
            }
            i++;
        }
        tables.add(i, table);

    }

    /**
     * if there is a corresponding {@link XWPFTable} of the parameter
     * ctTable in the tableList of this header
     * the method will return this table, or null if there is no
     * corresponding {@link XWPFTable}.
     */
    @Override
    public XWPFTable getTable(CTTbl ctTable) {
        for (XWPFTable table : tables) {
            if (table == null)
                return null;
            if (table.getCTTbl().equals(ctTable))
                return table;
        }
        return null;
    }

    @Override
    public XWPFSDTBlock getSdtBlock(CTSdtBlock ctSdtBlock) {
        for (int i = 0; i < sdtBlocks.size(); i++) {
            if (getSdtBlocks().get(i).getCtSdtBlock() == ctSdtBlock) {
                return getSdtBlocks().get(i);
            }
        }
        return null;
    }

    @Override
    public XWPFParagraph getParagraph(CTP p) {
        for (XWPFParagraph paragraph : paragraphs) {
            if (paragraph.getCTP().equals(p))
                return paragraph;
        }
        return null;
    }

    /**
     * Returns the {@link XWPFParagraph} at position pos in footnote's paragraph array.
     * @param pos Array position of the paragraph to get.
     * @return the {@link XWPFParagraph} at position pos, or null if there is no paragraph at that position.
     */
    @Override
    public XWPFParagraph getParagraphArray(int pos) {
        if(pos >=0 && pos < paragraphs.size()) {
            return paragraphs.get(pos);
        }
        return null;
    }

    /**
     * get the {@link XWPFTableCell} that belongs to the CTTc cell.
     *
     * @return {@link XWPFTableCell} that corresponds to the CTTc cell, if there is one, otherwise null.
     */
    @Override
    public XWPFTableCell getTableCell(CTTc cell) {
        XmlObject o;
        CTRow row;
        try (final XmlCursor cursor = cell.newCursor()) {
            cursor.toParent();
            o = cursor.getObject();
            if (!(o instanceof CTRow)) {
                return null;
            }
            row = (CTRow) o;
            cursor.toParent();
            o = cursor.getObject();
        }
        if (!(o instanceof CTTbl)) {
            return null;
        }
        CTTbl tbl = (CTTbl) o;
        XWPFTable table = getTable(tbl);
        if (table == null) {
            return null;
        }
        XWPFTableRow tableRow = table.getRow(row);
        if(tableRow == null){
            return null;
        }
        return tableRow.getTableCell(cell);
    }

    /**
     * Verifies that cursor is on the right position.
     *
     * @return true if the cursor is within a CTFtnEdn element.
     */
    private boolean isCursorInFtn(XmlCursor cursor) {
        try (XmlCursor verify = cursor.newCursor()) {
            verify.toParent();
            return  (verify.getObject() == this.ctFtnEdn);
        }
    }

    /**
     * The owning object for this footnote
     *
     * @return The {@link XWPFFootnotes} object that contains this footnote.
     */
    public POIXMLDocumentPart getOwner() {
        return footnotes;
    }

    /**
     * Insert a table constructed from OOXML table markup.
     * @return the inserted {@link XWPFTable}
     */
    @Override
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInFtn(cursor)) {
            String uri = CTTbl.type.getName().getNamespaceURI();
            String localPart = "tbl";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTTbl t = (CTTbl) cursor.getObject();
            XWPFTable newT = new XWPFTable(t, this);
            cursor.removeXmlContents();
            XmlObject o = null;
            while (!(o instanceof CTTbl) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if (!(o instanceof CTTbl)) {
                tables.add(0, newT);
            } else {
                int pos = tables.indexOf(getTable((CTTbl) o)) + 1;
                tables.add(pos, newT);
            }
            int i = 0;
            try (XmlCursor cursor1 = t.newCursor()) {
                while (cursor1.toPrevSibling()) {
                    o = cursor1.getObject();
                    if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock)
                        i++;
                }
                bodyElements.add(i, newT);
            }
            try (XmlCursor c2 = t.newCursor()) {
                cursor.toCursor(c2);
                cursor.toEndToken();
            }
            return newT;
        }
        return null;
    }

    @Override
    public XWPFSDTBlock insertNewSdtBlock(XmlCursor cursor) {
        if (isCursorInFtn(cursor)) {
            String uri = CTSdtBlock.type.getName().getNamespaceURI();
            String localPart = "sdt";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTSdtBlock sdt = (CTSdtBlock) cursor.getObject();
            XWPFSDTBlock newSdtBlock = new XWPFSDTBlock(sdt, this);
            XmlObject o = null;
            while (!(o instanceof CTSdtBlock) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if (!(o instanceof CTSdtBlock)) {
                sdtBlocks.add(0, newSdtBlock);
            } else {
                int pos = sdtBlocks.indexOf(getSdtBlock((CTSdtBlock) o)) + 1;
                sdtBlocks.add(pos, newSdtBlock);
            }
            int i = 0;
            try (XmlCursor sdtCursor = sdt.newCursor()) {
                cursor.toCursor(sdtCursor);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock) {
                        i++;
                    }
                }
                bodyElements.add(i, newSdtBlock);
                cursor.toCursor(sdtCursor);
                cursor.toEndToken();
                return newSdtBlock;
            }
        }
        return null;
    }

    /**
     * Add a new {@link XWPFParagraph} at position of the cursor.
     *
     * @return The inserted {@link XWPFParagraph}
     */
    @Override
    public XWPFParagraph insertNewParagraph(final XmlCursor cursor) {
        if (isCursorInFtn(cursor)) {
            String uri = CTP.type.getName().getNamespaceURI();
            String localPart = "p";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTP p = (CTP) cursor.getObject();
            XWPFParagraph newP = new XWPFParagraph(p, this);
            XmlObject o = null;
            while (!(o instanceof CTP) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if ((!(o instanceof CTP)) || o == p) {
                paragraphs.add(0, newP);
            } else {
                int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
                paragraphs.add(pos, newP);
            }
            int i = 0;
            try (XmlCursor p2 = p.newCursor()) {
                cursor.toCursor(p2);
            }
            while (cursor.toPrevSibling()) {
                o = cursor.getObject();
                if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock)
                    i++;
            }
            bodyElements.add(i, newP);
            try (XmlCursor p2 = p.newCursor()) {
                cursor.toCursor(p2);
                cursor.toEndToken();
            }
            return newP;
        }
        return null;
    }

    /**
     * Add a new {@link XWPFTable} to the end of the footnote.
     *
     * @param table CTTbl object from which to construct the {@link XWPFTable}
     * @return The added {@link XWPFTable}
     */
    public XWPFTable addNewTbl(CTTbl table) {
        CTTbl newTable = ctFtnEdn.addNewTbl();
        newTable.set(table);
        XWPFTable xTable = new XWPFTable(newTable, this);
        tables.add(xTable);
        return xTable;
    }

    /**
     * Add a new {@link XWPFParagraph} to the end of the footnote.
     *
     * @param paragraph CTP paragraph from which to construct the {@link XWPFParagraph}
     * @return The added {@link XWPFParagraph}
     */
    public XWPFParagraph addNewParagraph(CTP paragraph) {
        CTP newPara = ctFtnEdn.addNewP();
        newPara.set(paragraph);
        XWPFParagraph xPara = new XWPFParagraph(newPara, this);
        paragraphs.add(xPara);
        return xPara;
    }

    /**
     * Get the {@link XWPFDocument} the footnote is part of.
     */
    @Override
    public XWPFDocument getXWPFDocument() {
        return document;
    }

    /**
     * Get the Part to which the footnote belongs, which you need for adding relationships to other parts
     * @return {@link POIXMLDocumentPart} that contains the footnote.
     */
    @Override
    public POIXMLDocumentPart getPart() {
        return footnotes;
    }

    /**
     * Get the part type  {@link BodyType} of the footnote.
     * @return The {@link BodyType} value.
     */
    @Override
    public BodyType getPartType() {
        return BodyType.FOOTNOTE;
    }

    /**
     * Get the ID of the footnote.
     * <p>Footnote IDs are unique across all bottom-of-the-page and
     * end note footnotes.</p>
     *
     * @return Footnote ID
     * @since 4.0.0
     */
    public BigInteger getId() {
        return this.ctFtnEdn.getId();
    }

    /**
     * Appends a new {@link XWPFParagraph} to this footnote.
     *
     * @return The new {@link XWPFParagraph}
     * @since 4.0.0
     */
    public XWPFParagraph createParagraph() {
        XWPFParagraph p = new XWPFParagraph(this.ctFtnEdn.addNewP(), this);
        paragraphs.add(p);
        bodyElements.add(p);

        // If the paragraph is the first paragraph in the footnote,
        // ensure that it has a footnote reference run.

        if (p.equals(getParagraphs().get(0))) {
            ensureFootnoteRef(p);
        }
        return p;
    }

    /**
     * Ensure that the specified paragraph has a reference marker for this
     * footnote by adding a footnote reference if one is not found.
     * <p>This method is for the first paragraph in the footnote, not
     * paragraphs that will refer to the footnote. For references to
     * the footnote, use {@link XWPFParagraph#addFootnoteReference(XWPFAbstractFootnoteEndnote)}.
     * </p>
     * <p>The first run of the first paragraph in a footnote should
     * contain a {@link CTFtnEdnRef} object.</p>
     *
     * @param p The {@link XWPFParagraph} to ensure
     * @since 4.0.0
     */
    public abstract void ensureFootnoteRef(XWPFParagraph p);

    /**
     * Appends a new {@link XWPFTable} to this footnote
     *
     * @return The new {@link XWPFTable}
     * @since 4.0.0
     */
    public XWPFTable createTable() {
        XWPFTable table = new XWPFTable(ctFtnEdn.addNewTbl(), this);
        if (bodyElements.isEmpty()) {
            XWPFParagraph p = createParagraph();
            ensureFootnoteRef(p);
        }
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    @Override
    public XWPFSDTBlock createSdt() {
        XWPFSDTBlock sdt = new XWPFSDTBlock(ctFtnEdn.addNewSdt(), this);
        bodyElements.add(sdt);
        sdtBlocks.add(sdt);
        return sdt;
    }

    @Override
    public void setSDTBlock(int pos, XWPFSDTBlock sdt) {
        sdtBlocks.set(pos, sdt);
        ctFtnEdn.setSdtArray(pos, sdt.getCtSdtBlock());
    }

    /**
     * Appends a new {@link XWPFTable} to this footnote
     * @param rows Number of rows to initialize the table with
     * @param cols Number of columns to initialize the table with
     * @return the new {@link XWPFTable} with the specified number of rows and columns
     * @since 4.0.0
     */
    public XWPFTable createTable(int rows, int cols) {
        XWPFTable table = new XWPFTable(ctFtnEdn.addNewTbl(), this, rows, cols);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    /**
     * Finds that for example the 2nd entry in the body list is the 1st paragraph
     */
    private int getBodyElementSpecificPos(int pos, List<? extends IBodyElement> list) {
        // If there's nothing to find, skip it
        if (list.isEmpty()) {
            return -1;
        }

        if (pos >= 0 && pos < bodyElements.size()) {
            // Ensure the type is correct
            IBodyElement needle = bodyElements.get(pos);
            if (needle.getElementType() != list.get(0).getElementType()) {
                // Wrong type
                return -1;
            }

            // Work back until we find it
            int startPos = Math.min(pos, list.size() - 1);
            for (int i = startPos; i >= 0; i--) {
                if (list.get(i) == needle) {
                    return i;
                }
            }
        }

        // Couldn't be found
        return -1;
    }

    /**
     * get with the position of a table in the bodyelement array list
     * the position of this table in the table array list
     *
     * @param pos position of the table in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getTablePos(int pos) {
        return getBodyElementSpecificPos(pos, tables);
    }

    /**
     * Look up the paragraph at the specified position in the body elements list
     * and return this paragraphs position in the paragraphs list
     *
     * @param pos The position of the relevant paragraph in the body elements
     *            list
     * @return the position of the paragraph in the paragraphs list, if there is
     * a paragraph at the position in the bodyelements list. Else it
     * will return -1
     */
    public int getParagraphPos(int pos) {
        return getBodyElementSpecificPos(pos, paragraphs);
    }

    /**
     * get with the position of a table in the bodyelement array list
     * the position of this SDT in the contentControls array list
     *
     * @param pos position of the SDT in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getSDTPos(int pos) {
        return getBodyElementSpecificPos(pos, sdtBlocks);
    }

    @Override
    public boolean removeBodyElement(int pos) {
        if (pos >= 0 && pos < bodyElements.size()) {
            BodyElementType type = bodyElements.get(pos).getElementType();
            if (type == BodyElementType.TABLE) {
                int tablePos = getTablePos(pos);
                tables.remove(tablePos);
                ctFtnEdn.removeTbl(tablePos);
            }
            if (type == BodyElementType.PARAGRAPH) {
                int paraPos = getParagraphPos(pos);
                paragraphs.remove(paraPos);
                ctFtnEdn.removeP(paraPos);
            }
            if (type == BodyElementType.CONTENTCONTROL) {
                int sdtPos = getSDTPos(pos);
                sdtBlocks.remove(sdtPos);
                ctFtnEdn.removeSdt(sdtPos);
            }
            bodyElements.remove(pos);
            return true;
        }
        return false;
    }
}
