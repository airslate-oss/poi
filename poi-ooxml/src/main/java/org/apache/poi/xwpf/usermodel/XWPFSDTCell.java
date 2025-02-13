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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtCell;

/**
 * Experimental class to offer rudimentary read-only processing of
 * of StructuredDocumentTags/ContentControl that can appear
 * in a table row as if a table cell.
 * <p>
 * These can contain one or more cells or other SDTs within them.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTCell extends XWPFAbstractSDT implements ICell {
    private XWPFSDTContentCell cellContent;
    private IBody part;
    private CTSdtCell ctSdtCell;
    private XWPFTableRow xwpfTableRow;

    public XWPFSDTCell(CTSdtCell sdtCell, XWPFTableRow xwpfTableRow, IBody part) {
        super(sdtCell.getSdtPr());
        this.part = part;
        this.ctSdtCell = sdtCell;
        this.xwpfTableRow = xwpfTableRow;
        cellContent = new XWPFSDTContentCell(sdtCell.getSdtContent(), xwpfTableRow, part);
    }

    @Override
    public ISDTContent getContent() {
        return cellContent;
    }

    @Override
    public XWPFSDTPr createSdtPr() {
        XWPFSDTPr xwpfsdtPr = new XWPFSDTPr(this.ctSdtCell.addNewSdtPr());
        this.sdtPr = xwpfsdtPr;
        return xwpfsdtPr;
    }

    @Override
    public ISDTContent createSdtContent() {
        XWPFSDTContentCell xwpfsdtContentCell = new XWPFSDTContentCell(
                this.ctSdtCell.addNewSdtContent(),
                this.xwpfTableRow,
                this.part
        );
        this.cellContent = xwpfsdtContentCell;
        return xwpfsdtContentCell;
    }

    @Override
    public XWPFDocument getDocument() {
        if (part != null) {
            return part.getXWPFDocument();
        }
        return null;
    }
}
