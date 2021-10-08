package org.apache.poi.xwpf.usermodel;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XWPFSDTContentBlock implements ISDTContent, ISDTContentBlock {

    // private final XWPFDocument document;
    // private List<XWPFSDT> contentControls = new ArrayList<>();
    private IBody parent;
    private CTSdtContentBlock ctSdtContentBlock;
    private List<IBodyElement> bodyElements = new ArrayList<>();
    private List<XWPFParagraph> paragraphs = new ArrayList<>();
    private List<XWPFTable> tables = new ArrayList<>();

    public XWPFSDTContentBlock(CTSdtContentBlock block, IBody part) {
        if (block == null) {
            return;
        }
        this.ctSdtContentBlock = block;
        this.parent = part;

        XmlCursor cursor = block.newCursor();
        cursor.selectPath("./*");
        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTP) {
                XWPFParagraph p = new XWPFParagraph((CTP) o, part);
                bodyElements.add(p);
                paragraphs.add(p);
            } else if (o instanceof CTTbl) {
                XWPFTable t = new XWPFTable((CTTbl) o, part);
                bodyElements.add(t);
                tables.add(t);
            } else if (o instanceof CTSdtBlock) {
                XWPFSDTBlock c = new XWPFSDTBlock(((CTSdtBlock) o), part);
                bodyElements.add(c);
//                contentControls.add(c);
            }
        }
        cursor.dispose();
    }

    public CTSdtContentBlock getCtSdtContentBlock() {
        return ctSdtContentBlock;
    }

    public List<IBodyElement> getBodyElements() {
        return Collections.unmodifiableList(bodyElements);
    }

    public List<XWPFParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    public List<XWPFTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    @Override
    public XWPFParagraph getParagraph(CTP p) {
        for (int i = 0; i < getParagraphs().size(); i++) {
            if (getParagraphs().get(i).getCTP() == p) {
                return getParagraphs().get(i);
            }
        }
        return null;
    }

    /**
     * verifies that cursor is on the right position
     *
     * @param cursor
     */
    private boolean isCursorInSdtContentBlock(XmlCursor cursor) {
        XmlCursor verify = cursor.newCursor();
        verify.toParent();
        boolean result = (verify.getObject() == this.ctSdtContentBlock);
        verify.dispose();
        return result;
    }

    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (isCursorInSdtContentBlock(cursor)) {
            String uri = CTP.type.getName().getNamespaceURI();
            String localPart = "p";
            // creates a new Paragraph, cursor is positioned inside the new
            // element
            cursor.beginElement(localPart, uri);
            // move the cursor to the START token to the paragraph just created
            cursor.toParent();
            CTP p = (CTP) cursor.getObject();
            XWPFParagraph newP = new XWPFParagraph(p, parent);
            XmlObject o = null;
            /*
             * move the cursor to the previous element until a) the next
             * paragraph is found or b) all elements have been passed
             */
            while (!(o instanceof CTP) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            /*
             * if the object that has been found is a) not a paragraph or b) is
             * the paragraph that has just been inserted, as the cursor in the
             * while loop above was not moved as there were no other siblings,
             * then the paragraph that was just inserted is the first paragraph
             * in the body. Otherwise, take the previous paragraph and calculate
             * the new index for the new paragraph.
             */
            if ((!(o instanceof CTP)) || o == p) {
                paragraphs.add(0, newP);
            } else {
                int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
                paragraphs.add(pos, newP);
            }

            /*
             * create a new cursor, that points to the START token of the just
             * inserted paragraph
             */
            XmlCursor newParaPos = p.newCursor();
            try {
                /*
                 * Calculate the paragraphs index in the list of all body
                 * elements
                 */
                int i = 0;
                cursor.toCursor(newParaPos);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl) {
                        i++;
                    }
                }
                bodyElements.add(i, newP);
                cursor.toCursor(newParaPos);
                cursor.toEndToken();
                return newP;
            } finally {
                newParaPos.dispose();
            }
        }
        return null;
    }

    /**
     * Appends a new paragraph to this document
     *
     * @return a new paragraph
     */
    public XWPFParagraph createParagraph() {
        XWPFParagraph p = new XWPFParagraph(ctSdtContentBlock.addNewP(), parent);
        bodyElements.add(p);
        paragraphs.add(p);
        return p;
    }

    /**
     * Create an empty table with one row and one column as default.
     *
     * @return a new table
     */
    public XWPFTable createTable() {
        XWPFTable table = new XWPFTable(ctSdtContentBlock.addNewTbl(), parent);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    @Override
    public XWPFTable getTable(CTTbl ctTbl) {
        for (int i = 0; i < tables.size(); i++) {
            if (getTables().get(i).getCTTbl() == ctTbl) {
                return getTables().get(i);
            }
        }
        return null;
    }

    @Override
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInSdtContentBlock(cursor)) {
            String uri = CTTbl.type.getName().getNamespaceURI();
            String localPart = "tbl";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTTbl t = (CTTbl) cursor.getObject();
            XWPFTable newT = new XWPFTable(t, parent);
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
            XmlCursor tableCursor = t.newCursor();
            try {
                cursor.toCursor(tableCursor);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl) {
                        i++;
                    }
                }
                bodyElements.add(i, newT);
                cursor.toCursor(tableCursor);
                cursor.toEndToken();
                return newT;
            } finally {
                tableCursor.dispose();
            }
        }
        return null;
    }

    @Override
    public IBodyElement cloneExistingIBodyElement(IBodyElement elem) {
        if (elem instanceof XWPFParagraph) {
            CTP ctp = ctSdtContentBlock.addNewP();
            ctp.set(((XWPFParagraph) elem).getCTP());
            XWPFParagraph p = new XWPFParagraph(ctp, parent);
            paragraphs.add(p);
            bodyElements.add(p);
            return p;
        } else if (elem instanceof XWPFTable) {
            if (((XWPFTable) elem).fetchTblText().toString().equals("")) {
                CTP ctp = ctSdtContentBlock.addNewP();
                XWPFParagraph p = new XWPFParagraph(ctp, parent);
                paragraphs.add(p);
                bodyElements.add(p);
                return p;
            }
            CTTbl ctTbl = ctSdtContentBlock.addNewTbl();
            ctTbl.set(((XWPFTable) elem).getCTTbl());
            XWPFTable tbl = new XWPFTable(ctTbl, parent);
            tables.add(tbl);
            bodyElements.add(tbl);
            return tbl;
        }
        return null;
    }

    @Override
    public boolean removeIBodyElement(int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        boolean addNewLine = false;
        for (int i = 0; i < bodyElements.size(); i++) {
            Object o = bodyElements.get(i);
            if (o instanceof XWPFParagraph) {
                appendParagraph((XWPFParagraph) o, text);
                addNewLine = true;
            } else if (o instanceof XWPFTable) {
                appendTable((XWPFTable) o, text);
                addNewLine = true;
            } else if (o instanceof XWPFSDTBlock) {
                text.append(((XWPFSDTBlock) o).getContent().getText());
                addNewLine = true;
            }
            if (addNewLine && i < bodyElements.size() - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }

    private void appendTable(XWPFTable table, StringBuilder text) {
        //this works recursively to pull embedded tables from within cells
        for (XWPFTableRow row : table.getRows()) {
            List<ICell> cells = row.getTableICells();
            for (int i = 0; i < cells.size(); i++) {
                ICell cell = cells.get(i);
                if (cell instanceof XWPFTableCell) {
                    text.append(((XWPFTableCell) cell).getTextRecursively());
                } else if (cell instanceof XWPFSDTCell) {
                    text.append(((XWPFSDTCell) cell).getContent().getText());
                }
                if (i < cells.size() - 1) {
                    text.append("\t");
                }
            }
            text.append('\n');
        }
    }

    private void appendParagraph(XWPFParagraph paragraph, StringBuilder text) {
        for (IRunElement run : paragraph.getRuns()) {
            text.append(run);
        }
    }

    @Override
    public String toString() {
        return getText();
    }
}
