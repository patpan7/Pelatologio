package org.easytech.pelatologio;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

public class AfmResponseParser {

    public static Customer parseResponse(String responseXml) {
        try {
            // Φόρτωση της XML από το response
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(responseXml.getBytes()));

            // Δημιουργία XPath για αναζήτηση κόμβων
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            // Εξαγωγή δεδομένων
            String afm = getXPathValue(document, xpath, "//basic_rec/afm");
            String onomasia = getXPathValue(document, xpath, "//basic_rec/onomasia");
            String commerTitle = getXPathValue(document, xpath, "//basic_rec/commer_title");
            String postalAddress = getXPathValue(document, xpath, "//basic_rec/postal_address");
            String postalAddressNo = getXPathValue(document, xpath, "//basic_rec/postal_address_no");
            String postalAreaDescription = getXPathValue(document, xpath, "//basic_rec/postal_area_description");
            String epaggelma = getXPathValue(document, xpath, "//firm_act_tab/item/firm_act_descr");
            // Επιστροφή των πληροφοριών σε αντικείμενο
            return new Customer(onomasia,commerTitle,epaggelma,afm,"","","",postalAddress+" "+postalAddressNo,postalAreaDescription,"");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getXPathValue(Document document, XPath xpath, String expression) throws Exception {
        XPathExpression expr = xpath.compile(expression);
        Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
        return node != null ? node.getTextContent() : "";
    }
}
