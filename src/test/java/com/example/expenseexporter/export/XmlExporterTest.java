package com.example.expenseexporter.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static com.example.expenseexporter.export.ExportFixtures.approvedEur;
import static com.example.expenseexporter.export.ExportFixtures.rejectedUsd;
import static com.example.expenseexporter.export.ExportFixtures.withTexts;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class XmlExporterTest {

    private final XmlExporter exporter = new XmlExporter();

    @Test
    void exportsInvoicesAndSummary() {
        String xml = exporter.export(List.of(approvedEur(), rejectedUsd()));

        assertThat(xml).isEqualTo("""
                <?xml version="1.0" encoding="UTF-8"?>
                <expenseReport>
                  <invoices>
                    <invoice>
                      <invoiceNumber>INV-0001</invoiceNumber>
                      <issueDate>2026-10-02</issueDate>
                      <employeeId>E-001</employeeId>
                      <employeeName>Anna Schmidt</employeeName>
                      <position>JUNIOR</position>
                      <vendor>Office Depot</vendor>
                      <description>Notebooks, pens</description>
                      <amount currency="EUR">45.50</amount>
                      <limit>300.00</limit>
                      <status>APPROVED</status>
                      <rule>Fixed amount: 300.00</rule>
                    </invoice>
                    <invoice>
                      <invoiceNumber>INV-0002</invoiceNumber>
                      <issueDate>2026-10-05</issueDate>
                      <employeeId>E-002</employeeId>
                      <employeeName>Maria O&apos;Neil</employeeName>
                      <position>SENIOR</position>
                      <vendor>Smith &amp; Sons</vendor>
                      <description>Taxi &quot;airport&quot;</description>
                      <amount currency="USD">210.00</amount>
                      <limit>200.00</limit>
                      <status>REJECTED</status>
                      <rule>5% of salary</rule>
                      <message>Exceeds limit by 10.00</message>
                    </invoice>
                  </invoices>
                  <summary>
                    <count>2</count>
                    <approved>1</approved>
                    <rejected>1</rejected>
                    <errors>0</errors>
                    <totals>
                      <total currency="EUR">45.50</total>
                      <total currency="USD">210.00</total>
                    </totals>
                  </summary>
                </expenseReport>
                """);
    }

    @Test
    void emptyListIsWellFormed() throws Exception {
        String xml = exporter.export(List.of());

        assertThat(xml).isEqualTo("""
                <?xml version="1.0" encoding="UTF-8"?>
                <expenseReport>
                  <invoices>
                  </invoices>
                  <summary>
                    <count>0</count>
                    <approved>0</approved>
                    <rejected>0</rejected>
                    <errors>0</errors>
                    <totals/>
                  </summary>
                </expenseReport>
                """);
        assertThat(parse(xml).getElementsByTagName("invoice").getLength()).isZero();
    }

    @Test
    void parsedOutputKeepsValuesUnchanged() throws Exception {
        String vendor = "Smith & Sons <Ltd> 'A'";
        String description = "Line 1\nLine 2\r\nTab\there \"quoted\" back\\slash Кафе €";

        Document document = parse(exporter.export(List.of(withTexts(vendor, description))));

        Element invoice = (Element) document.getElementsByTagName("invoice").item(0);
        assertThat(text(invoice, "vendor")).isEqualTo(vendor);
        assertThat(text(invoice, "description")).isEqualTo(description);
        assertThat(((Element) invoice.getElementsByTagName("amount").item(0)).getAttribute("currency"))
                .isEqualTo("EUR");
        assertThat(invoice.getElementsByTagName("limit").getLength()).isZero();
        assertThat(invoice.getElementsByTagName("message").getLength()).isZero();
    }

    static Stream<Arguments> escapedDescriptions() {
        return Stream.of(
                Arguments.of("plain", "plain"),
                Arguments.of("", ""),
                Arguments.of("a & b", "a &amp; b"),
                Arguments.of("<tag>", "&lt;tag&gt;"),
                Arguments.of("\"q\" 'a'", "&quot;q&quot; &apos;a&apos;"),
                Arguments.of("&amp;", "&amp;amp;"),
                Arguments.of("tab\tnew\nline", "tab\tnew\nline"),
                Arguments.of("cr\r\n", "cr&#13;\n"),
                Arguments.of("Кафе €", "Кафе €"));
    }

    @ParameterizedTest
    @MethodSource("escapedDescriptions")
    void escapesDescription(String input, String expected) throws Exception {
        String xml = exporter.export(List.of(withTexts("Vendor", input)));

        assertThat(xml).contains("<description>" + expected + "</description>");
        assertThat(parse(xml).getElementsByTagName("description").item(0).getTextContent())
                .isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u0000", "a\u0001b", "bell\u0007", "\u0008", "\u000B", "\u001F", "￾", "￿"})
    void rejectsTextThatXmlCannotRepresent(String input) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> exporter.export(List.of(withTexts("Vendor", input))))
                .withMessageContaining("not allowed in XML 1.0");
    }

    @Test
    void reportsItsFormat() {
        assertThat(exporter.format()).isEqualTo(ExportFormat.XML);
    }

    private static Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent();
    }
}
