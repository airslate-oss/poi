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

import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;

public class XWPFAnchor implements IDrawingContent {
    private final CTAnchor ctAnchor;
    private final XWPFDrawing parent;
    private XWPFGraphicalObject graphic;

    public XWPFAnchor(CTAnchor ctAnchor, XWPFDrawing parent) {
        this.ctAnchor = ctAnchor;
        this.parent = parent;
        if (ctAnchor.getGraphic() != null) {
            this.graphic = new XWPFGraphicalObject(ctAnchor.getGraphic(), this);
        }
    }

    public XWPFGraphicalObject createGraphicalObject() {
        CTGraphicalObject ctGraphicalObject = ctAnchor.addNewGraphic();
        XWPFGraphicalObject graphicalObject = new XWPFGraphicalObject(ctGraphicalObject, this);
        graphic = graphicalObject;
        return graphicalObject;
    }

    public CTAnchor getCTAnchor() {
        return ctAnchor;
    }

    public XWPFDrawing getParent() {
        return parent;
    }

    public XWPFGraphicalObject getGraphicalObject() {
        return graphic;
    }
}
