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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestXWPFPictureData {

    @Test
    void testRead() throws InvalidFormatException, IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("VariousPictures.docx")) {
            List<XWPFPictureData> pictures = sampleDoc.getAllPictures();

            assertEquals(5, pictures.size());
            String[] ext = {"wmf", "png", "emf", "emf", "jpeg"};
            for (int i = 0; i < pictures.size(); i++) {
                assertEquals(ext[i], pictures.get(i).suggestFileExtension());
            }

            int num = pictures.size();

            byte[] pictureData = XWPFTestDataSamples.getImage("nature1.jpg");

            String relationId = sampleDoc.addPictureData(pictureData, XWPFDocument.PICTURE_TYPE_JPEG);
            // picture list was updated
            assertEquals(num + 1, pictures.size());
            XWPFPictureData pict = (XWPFPictureData) sampleDoc.getRelationById(relationId);
            assertNotNull(pict);
            assertEquals("jpeg", pict.suggestFileExtension());
            assertArrayEquals(pictureData, pict.getData());

            byte[] pictureData2 = XWPFTestDataSamples.getImage("nature1.png");

            String relationId2 = sampleDoc.addPictureData(pictureData2, PictureType.PNG);
            assertNotEquals(relationId, relationId2);
            // picture list was updated
            assertEquals(num + 2, pictures.size());
            XWPFPictureData pict2 = (XWPFPictureData) sampleDoc.getRelationById(relationId2);
            assertNotNull(pict2);
            assertEquals("png", pict2.suggestFileExtension());
            assertArrayEquals(pictureData2, pict2.getData());
        }
    }

    @Test
    void testReadMaxSize() throws InvalidFormatException, IOException {
        int prev = XWPFPictureData.getMaxImageSize();
        try {
            // check for a regression in 5.2.1:
            // even if we set the maximum to a very high value it should not
            // simply allocate that much here
            XWPFPictureData.setMaxImageSize(Integer.MAX_VALUE-1);
            testRead();
        } finally {
            XWPFPictureData.setMaxImageSize(prev);
        }
    }

    @Test
    void testPictureInHeader() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("headerPic.docx")) {
            verifyOneHeaderPicture(sampleDoc);

            XWPFDocument readBack = XWPFTestDataSamples.writeOutAndReadBack(sampleDoc);
            verifyOneHeaderPicture(readBack);
        }
    }

    @Test
    void testCreateHeaderPicture() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {

            // Starts with no header
            XWPFHeaderFooterPolicy policy = doc.getHeaderFooterPolicy();
            assertNull(policy);

            // Add a default header
            policy = doc.createHeaderFooterPolicy();
            XWPFHeader header = policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
            header.createParagraph().createRun().setText("Hello, Header World!");
            header.createParagraph().createRun().setText("Paragraph 2");
            assertEquals(0, header.getAllPictures().size());
            assertEquals(2, header.getParagraphs().size());

            // Add a picture to the first paragraph
            header.getParagraphs().get(0).getRuns().get(0).addPicture(
                    new ByteArrayInputStream(new byte[]{1, 2, 3, 4}),
                    Document.PICTURE_TYPE_JPEG, "test.jpg", 2, 2);

            // Check
            verifyOneHeaderPicture(doc);

            // Save, re-load, re-check
            XWPFDocument readBack = XWPFTestDataSamples.writeOutAndReadBack(doc);
            verifyOneHeaderPicture(readBack);
        }
    }

    private void verifyOneHeaderPicture(XWPFDocument sampleDoc) {
        XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();

        XWPFHeader header = policy.getDefaultHeader();

        List<XWPFPictureData> pictures = header.getAllPictures();
        assertEquals(1, pictures.size());
    }

    @Test
    void testNew() throws InvalidFormatException, IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("EmptyDocumentWithHeaderFooter.docx")) {
            byte[] jpegData = XWPFTestDataSamples.getImage("nature1.jpg");
            assertNotNull(jpegData);
            byte[] gifData = XWPFTestDataSamples.getImage("nature1.gif");
            assertNotNull(gifData);
            byte[] pngData = XWPFTestDataSamples.getImage("nature1.png");
            assertNotNull(pngData);

            List<XWPFPictureData> pictures = doc.getAllPictures();
            assertEquals(0, pictures.size());

            // Document shouldn't have any image relationships
            assertEquals(13, doc.getPackagePart().getRelationships().size());
            for (PackageRelationship rel : doc.getPackagePart().getRelationships()) {
                assertNotEquals(XSSFRelation.IMAGE_JPEG.getRelation(), rel.getRelationshipType(), "Shouldn't have JPEG yet");
            }

            // Add the image
            String relationId = doc.addPictureData(jpegData, XWPFDocument.PICTURE_TYPE_JPEG);
            assertEquals(1, pictures.size());
            XWPFPictureData jpgPicData = (XWPFPictureData) doc.getRelationById(relationId);
            assertNotNull(jpgPicData);
            assertEquals("jpeg", jpgPicData.suggestFileExtension());
            assertArrayEquals(jpegData, jpgPicData.getData());

            // Ensure it now has one
            assertEquals(14, doc.getPackagePart().getRelationships().size());
            PackageRelationship jpegRel = null;
            for (PackageRelationship rel : doc.getPackagePart().getRelationships()) {
                if (rel.getRelationshipType().equals(XWPFRelation.IMAGE_JPEG.getRelation())) {
                    assertNull(jpegRel, "Found 2 jpegs!");
                    jpegRel = rel;
                }
            }
            assertNotNull(jpegRel, "JPEG Relationship not found");

            // Check the details
            assertNotNull(jpegRel);
            assertEquals(XWPFRelation.IMAGE_JPEG.getRelation(), jpegRel.getRelationshipType());
            assertEquals("/word/document.xml", jpegRel.getSource().getPartName().toString());
            assertEquals("/word/media/image1.jpeg", jpegRel.getTargetURI().getPath());

            XWPFPictureData pictureDataByID = doc.getPictureDataByID(jpegRel.getId());
            assertArrayEquals(jpegData, pictureDataByID.getData());

            // Save an re-load, check it appears
            try (XWPFDocument docBack = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertEquals(1, docBack.getAllPictures().size());
                assertEquals(1, docBack.getAllPackagePictures().size());

                // verify the picture that we read back in
                pictureDataByID = docBack.getPictureDataByID(jpegRel.getId());
                assertArrayEquals(jpegData, pictureDataByID.getData());
            }
        }
    }

    @Test
    void testBug51770() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug51170.docx")) {
            XWPFHeaderFooterPolicy policy = doc.getHeaderFooterPolicy();
            XWPFHeader header = policy.getDefaultHeader();

            for (XWPFParagraph xwpfParagraph : header.getParagraphs()) {
                for (XWPFRun xwpfRun : xwpfParagraph.getRuns()) {
                    for (IDrawing iDrawing : xwpfRun.getIDrawings()) {
                        XWPFDrawing xwpfDrawing = (XWPFDrawing) iDrawing;
                        assertInstanceOf(XWPFDrawing.class, iDrawing);
                        assertEquals(1, xwpfDrawing.getDrawingContents().size());
                        IDrawingContent drawingContent = xwpfDrawing.getDrawingContents().get(0);
                        XWPFPicture picture = null;
                        if (drawingContent instanceof XWPFAnchor) {
                            XWPFAnchor xwpfAnchor = (XWPFAnchor) drawingContent;
                            picture = xwpfAnchor.getGraphicalObject().getGraphicalObjectData().getPicture();
                        } else if (drawingContent instanceof XWPFInline) {
                            XWPFInline xwpfInline = (XWPFInline) drawingContent;
                            picture = xwpfInline.getGraphicalObject().getGraphicalObjectData().getPicture();
                        } else {
                            fail();
                        }
                        XWPFPictureData pictureData = picture.getPictureData();
                        assertNull(pictureData);
                    }
                }
            }
        }
    }
}
