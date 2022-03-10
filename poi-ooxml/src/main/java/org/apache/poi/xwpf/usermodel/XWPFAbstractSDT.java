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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.formula.functions.T;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtPr;

/**
 * Experimental abstract class that is a base for XWPFSDT and XWPFSDTCell
 * <p>
 * WARNING - APIs expected to change rapidly.
 * <p>
 * These classes have so far been built only for read-only processing.
 */
public abstract class XWPFAbstractSDT {
    protected XWPFSDTPr sdtPr;

    public XWPFAbstractSDT(CTSdtPr sdtPr) {
        this.sdtPr = new XWPFSDTPr(sdtPr);
    }

    public XWPFSDTPr getSdtPr() {
        return this.sdtPr;
    }

    /**
     * @return the content object
     */
    public abstract ISDTContent getContent();

    /**
     * @return create Properties for SDT
     */
    public abstract XWPFSDTPr createSdtPr();

    /**
     * @return create Properties for SDT
     */
    public abstract ISDTContent createSdtContent();
}
