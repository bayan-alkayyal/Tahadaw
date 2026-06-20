package org.example.tahadaw.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Model.Payment;
import org.example.tahadaw.Model.User;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Renders a clean, branded PDF receipt for a premium payment.
 */
@Service
public class PdfReceiptService {

    private static final Color NAVY = new Color(0x2D, 0x3A, 0x47);
    private static final Color GOLD = new Color(0xC9, 0xA2, 0x4B);
    private static final Color ROW_ALT = new Color(0xF4, 0xF5, 0xF7);
    private static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);
    private static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color TEXT = new Color(0x2D, 0x3A, 0x47);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public byte[] buildPremiumReceiptPdf(User user, Payment payment) {
        Document document = new Document(PageSize.A4, 48, 48, 54, 54);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(buildHeader(payment));
            document.add(spacer(18));
            document.add(buildDetailsTable(user, payment));
            document.add(spacer(22));
            document.add(buildFooter());

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new ApiException("Failed to generate receipt PDF: " + ex.getMessage());
        }
    }

    private PdfPTable buildHeader(Payment payment) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{60, 40});

        PdfPCell brandCell = new PdfPCell();
        brandCell.setBackgroundColor(NAVY);
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setPadding(22f);
        Phrase brand = new Phrase();
        brand.add(new com.lowagie.text.Chunk("TAHADAW\n", new Font(Font.HELVETICA, 22, Font.BOLD, Color.WHITE)));
        brand.add(new com.lowagie.text.Chunk("Premium Payment Receipt", new Font(Font.HELVETICA, 11, Font.NORMAL, GOLD)));
        brandCell.addElement(brand);
        header.addCell(brandCell);

        PdfPCell amountCell = new PdfPCell();
        amountCell.setBackgroundColor(NAVY);
        amountCell.setBorder(Rectangle.NO_BORDER);
        amountCell.setPadding(22f);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Phrase amount = new Phrase();
        amount.add(new com.lowagie.text.Chunk("AMOUNT PAID\n", new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(0xB8, 0xBF, 0xC8))));
        amount.add(new com.lowagie.text.Chunk(formatAmount(payment), new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE)));
        amountCell.addElement(amount);
        header.addCell(amountCell);

        return header;
    }

    private PdfPTable buildDetailsTable(User user, Payment payment) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{38, 62});

        addRow(table, "Receipt No.", "#" + payment.getId(), false);
        addRow(table, "Date", payment.getCreatedAt() != null ? payment.getCreatedAt().format(DATE_FMT) : "-", true);
        addRow(table, "Customer", safe(user.getFullName()), false);
        addRow(table, "Email", safe(user.getEmail()), true);
        addRow(table, "Plan", "Premium Access", false);
        addRow(table, "Payment Type", safe(payment.getPaymentType()), true);
        addRow(table, "Provider", safe(payment.getProvider()), false);
        addRow(table, "Transaction ID", safe(payment.getTransactionId()), true);
        addRow(table, "Status", safe(payment.getStatus()), false);

        return table;
    }

    private void addRow(PdfPTable table, String label, String value, boolean alt) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 10, Font.BOLD, MUTED)));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, new Font(Font.HELVETICA, 11, Font.NORMAL, TEXT)));

        for (PdfPCell cell : new PdfPCell[]{labelCell, valueCell}) {
            cell.setPadding(10f);
            cell.setBorderColor(BORDER);
            cell.setBorderWidth(0.5f);
            if (alt) {
                cell.setBackgroundColor(ROW_ALT);
            }
        }
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPTable buildFooter() throws DocumentException {
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);

        PdfPCell note = new PdfPCell(new Phrase(
                "Thank you for upgrading to Premium. Your account now unlocks the Surprise Plan and Gift Card features. "
                        + "This receipt was generated automatically by Tahadaw.",
                new Font(Font.HELVETICA, 9, Font.NORMAL, MUTED)));
        note.setBorder(Rectangle.TOP);
        note.setBorderColor(BORDER);
        note.setPaddingTop(12f);
        footer.addCell(note);
        return footer;
    }

    private PdfPTable spacer(float height) throws DocumentException {
        PdfPTable spacer = new PdfPTable(1);
        spacer.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        cell.setPhrase(new Phrase(" "));
        spacer.addCell(cell);
        return spacer;
    }

    private static String formatAmount(Payment payment) {
        double major = payment.getAmountMinor() != null ? payment.getAmountMinor() / 100.0 : 0.0;
        String currency = payment.getCurrency() != null ? payment.getCurrency() : "";
        return String.format("%.2f %s", major, currency).trim();
    }

    private static String safe(String value) {
        return (value != null && !value.isBlank()) ? value : "-";
    }
}
