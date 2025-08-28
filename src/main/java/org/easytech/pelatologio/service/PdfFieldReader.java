package org.easytech.pelatologio.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.io.IOException;

public class PdfFieldReader {

    public static void main(String[] args) {
        // IMPORTANT: Make sure the PDF is in the root of your project folder
        File pdfFile = new File("A1_EDPS.pdf");

        if (!pdfFile.exists()) {
            System.err.println("Error: A1_EDPS.pdf not found in the project root directory.");
            return;
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                System.out.println("This PDF does not contain an AcroForm (interactive form).");
                return;
            }

            System.out.println("--- PDF Form Fields ---");
            for (PDField field : acroForm.getFields()) {
                String fieldName = field.getFullyQualifiedName();
                String fieldValue = field.getValueAsString();
                System.out.println("Field Name: '" + fieldName + "', Current Value: '" + fieldValue + "'");
            }
            System.out.println("-----------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
