package org.example.tahadaw.Service;

import org.example.tahadaw.Api.ApiException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * Renders a branded gift card PNG (navy #2D3A47 theme, Tahadaw logo, gold accents,
 * centered RTL Arabic text, QR panel) for storage as LONGBLOB.
 * Uses headless AWT (no extra dependency) so it works on any server.
 */
@Service
public class GiftCardImageService {

    // Warm cream canvas behind the card.
    private static final Color CANVAS_TOP = new Color(0xFB, 0xF7, 0xEF);
    private static final Color CANVAS_BOTTOM = new Color(0xEC, 0xE3, 0xD2);
    // Exact brand navy + a slightly lifted top for a soft vertical gradient.
    private static final Color CARD_TOP = new Color(0x39, 0x49, 0x59);
    private static final Color CARD_BOTTOM = new Color(0x2D, 0x3A, 0x47);
    private static final Color GOLD = new Color(0xC9, 0xA2, 0x5C);
    private static final Color GOLD_SOFT = new Color(0xC9, 0xA2, 0x5C, 90);
    private static final Color HEADING = new Color(0xF4, 0xEC, 0xDC);
    private static final Color MESSAGE = new Color(0xC9, 0xD0, 0xD7);
    private static final Color CAPTION = new Color(0x97, 0xA3, 0xAD);
    private static final Color SHADOW = new Color(0x1B, 0x24, 0x2D);

    private static volatile BufferedImage cachedLogo;
    private static volatile boolean logoLoaded;

    public byte[] renderCard(String cardSize,
                             String recipientName,
                             String senderName,
                             String messageText,
                             byte[] qrPngBytes) {
        int width;
        int height;
        switch (cardSize == null ? "" : cardSize.trim().toUpperCase()) {
            case "SMALL" -> {
                width = 600;
                height = 820;
            }
            case "LARGE" -> {
                width = 1000;
                height = 1360;
            }
            default -> {
                width = 800;
                height = 1080;
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.setPaint(new GradientPaint(0, 0, CANVAS_TOP, 0, height, CANVAS_BOTTOM));
            g.fillRect(0, 0, width, height);

            int margin = Math.round(width * 0.07f);
            int cardX = margin;
            int cardY = margin;
            int cardW = width - (2 * margin);
            int cardH = height - (2 * margin);
            int arc = Math.round(width * 0.07f);
            float centerX = cardX + cardW / 2f;

            // Soft drop shadow (layered for a smooth falloff).
            for (int i = 6; i >= 1; i--) {
                int spread = i * Math.max(2, width / 220);
                g.setColor(new Color(SHADOW.getRed(), SHADOW.getGreen(), SHADOW.getBlue(), 10));
                g.fill(new RoundRectangle2D.Float(
                        cardX - spread, cardY - spread + (cardH * 0.012f),
                        cardW + (2 * spread), cardH + (2 * spread), arc + spread, arc + spread));
            }

            // Card body with subtle vertical gradient.
            g.setPaint(new GradientPaint(0, cardY, CARD_TOP, 0, cardY + cardH, CARD_BOTTOM));
            g.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, arc, arc));

            // Thin gold inner frame.
            float frameInset = cardW * 0.045f;
            g.setColor(GOLD_SOFT);
            g.setStroke(new BasicStroke(Math.max(1.2f, width / 600f)));
            float fArc = arc - frameInset * 0.6f;
            g.draw(new RoundRectangle2D.Float(
                    cardX + frameInset, cardY + frameInset,
                    cardW - (2 * frameInset), cardH - (2 * frameInset),
                    Math.max(8f, fArc), Math.max(8f, fArc)));

            float innerPad = cardW * 0.10f;
            float contentWidth = cardW - (2 * innerPad);

            // ---- Top: logo ----
            float cursorY = cardY + cardH * 0.085f;
            BufferedImage logo = loadLogo();
            if (logo != null) {
                int logoH = Math.round(cardH * 0.19f);
                int logoW = Math.round(logoH * (logo.getWidth() / (float) logo.getHeight()));
                int maxLogoW = Math.round(contentWidth * 0.92f);
                if (logoW > maxLogoW) {
                    logoW = maxLogoW;
                    logoH = Math.round(logoW * (logo.getHeight() / (float) logo.getWidth()));
                }
                g.drawImage(logo, Math.round(centerX - logoW / 2f), Math.round(cursorY), logoW, logoH, null);
                cursorY += logoH + cardH * 0.022f;
            } else {
                Font brand = pickFont(Font.BOLD, Math.max(30, Math.round(cardW / 9f)));
                g.setColor(HEADING);
                drawCenteredRtl(g, "تهادوا", brand, centerX, cursorY + brand.getSize());
                cursorY += brand.getSize() * 1.6f;
            }

            // ---- Ornamental divider ----
            drawOrnament(g, centerX, cursorY, cardW * 0.34f);
            cursorY += cardH * 0.026f;

            // ---- Heading: إلى {recipient} ----
            Font headingFont = pickFont(Font.BOLD, Math.max(28, Math.round(cardW / 13f)));
            g.setColor(HEADING);
            float headingBaseline = cursorY + headingFont.getSize();
            drawCenteredRtl(g, "إلى " + safe(recipientName), headingFont, centerX, headingBaseline);
            cursorY = headingBaseline + cardH * 0.012f;

            // ---- Bottom-anchored elements (compute upward) ----
            Font fromFont = pickFont(Font.BOLD, Math.max(17, Math.round(cardW / 24f)));
            Font captionFont = pickFont(Font.PLAIN, Math.max(13, Math.round(cardW / 33f)));
            float fromBaseline = cardY + cardH - cardH * 0.075f;
            float bottomDividerY = fromBaseline - fromFont.getSize() * 1.7f;
            float captionBaseline = bottomDividerY - captionFont.getSize() * 0.9f;

            int qr = Math.round(cardW * 0.33f);
            int qrX = Math.round(centerX - qr / 2f);
            int qrBottom = Math.round(captionBaseline - captionFont.getSize() * 1.7f);
            int qrY = qrBottom - qr;

            // ---- Message (centered RTL, auto-fit to the available space) ----
            if (messageText != null && !messageText.isBlank()) {
                String msg = messageText.trim();
                g.setColor(MESSAGE);
                float areaTop = cursorY + cardH * 0.012f;
                float areaBottom = qrY - cardH * 0.03f;
                float areaHeight = Math.max(0f, areaBottom - areaTop);
                float maxTextWidth = contentWidth * 0.96f;

                // Pick the largest font (within bounds) whose wrapped text fits the area;
                // long messages shrink down to minFs so they always fit inside the card.
                int maxFs = Math.max(16, Math.round(cardW / 22f));
                int minFs = Math.max(10, Math.round(cardW / 58f));
                Font messageFont = pickFont(Font.PLAIN, maxFs);
                float textHeight = areaHeight;
                float lineGapFactor = 0.42f;
                for (int fs = maxFs; fs >= minFs; fs--) {
                    Font candidate = pickFont(Font.PLAIN, fs);
                    float h = measureParagraphHeight(g, msg, candidate, maxTextWidth, fs * lineGapFactor);
                    if (h <= areaHeight || fs == minFs) {
                        messageFont = candidate;
                        textHeight = h;
                        break;
                    }
                }

                float startY = areaTop + Math.max(0f, (areaHeight - textHeight) / 2f);
                drawCenteredParagraph(g, msg, messageFont, centerX, maxTextWidth,
                        startY, areaBottom, messageFont.getSize() * lineGapFactor);
            }

            // ---- QR panel ----
            int qrPad = Math.max(12, qr / 14);
            int panelArc = Math.max(14, qr / 8);
            g.setColor(new Color(SHADOW.getRed(), SHADOW.getGreen(), SHADOW.getBlue(), 60));
            g.fill(new RoundRectangle2D.Float(qrX - qrPad, qrY - qrPad + 4,
                    qr + (2 * qrPad), qr + (2 * qrPad), panelArc, panelArc));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(qrX - qrPad, qrY - qrPad,
                    qr + (2 * qrPad), qr + (2 * qrPad), panelArc, panelArc));
            BufferedImage qrImage = readImage(qrPngBytes);
            if (qrImage != null) {
                g.drawImage(qrImage, qrX, qrY, qr, qr, null);
            } else {
                drawQrPlaceholder(g, qrX, qrY, qr);
            }

            // ---- Caption under QR ----
            g.setColor(CAPTION);
            drawCenteredRtl(g, "امسح الرمز لرسالة خاصة", captionFont, centerX, captionBaseline);

            // ---- Bottom divider + sender ----
            drawOrnament(g, centerX, bottomDividerY, cardW * 0.26f);
            g.setColor(GOLD);
            drawCenteredRtl(g, "من: " + safe(senderName), fromFont, centerX, fromBaseline);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new ApiException("Failed to generate gift card image: " + ex.getMessage());
        } finally {
            g.dispose();
        }
    }

    private TextLayout rtlLayout(Graphics2D g, String text, Font font) {
        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, font);
        as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        return new TextLayout(as.getIterator(), g.getFontRenderContext());
    }

    private void drawCenteredRtl(Graphics2D g, String text, Font font, float centerX, float baselineY) {
        if (text == null || text.isBlank()) {
            return;
        }
        TextLayout layout = rtlLayout(g, text, font);
        layout.draw(g, centerX - layout.getAdvance() / 2f, baselineY);
    }

    private void drawCenteredParagraph(Graphics2D g, String text, Font font, float centerX, float maxWidth,
                                       float startY, float maxBottom, float lineGap) {
        FontRenderContext frc = g.getFontRenderContext();
        float y = startY;
        for (String paragraph : text.split("\n")) {
            if (paragraph.isBlank()) {
                y += font.getSize() * 0.6f;
                continue;
            }
            AttributedString as = new AttributedString(paragraph);
            as.addAttribute(TextAttribute.FONT, font);
            as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
            AttributedCharacterIterator it = as.getIterator();
            LineBreakMeasurer measurer = new LineBreakMeasurer(it, frc);
            while (measurer.getPosition() < it.getEndIndex()) {
                TextLayout layout = measurer.nextLayout(maxWidth);
                y += layout.getAscent();
                if (y > maxBottom) {
                    return;
                }
                layout.draw(g, centerX - layout.getAdvance() / 2f, y);
                y += layout.getDescent() + layout.getLeading() + lineGap;
            }
        }
    }

    private float measureParagraphHeight(Graphics2D g, String text, Font font, float maxWidth, float lineGap) {
        FontRenderContext frc = g.getFontRenderContext();
        float y = 0f;
        for (String paragraph : text.split("\n")) {
            if (paragraph.isBlank()) {
                y += font.getSize() * 0.6f;
                continue;
            }
            AttributedString as = new AttributedString(paragraph);
            as.addAttribute(TextAttribute.FONT, font);
            as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
            AttributedCharacterIterator it = as.getIterator();
            LineBreakMeasurer measurer = new LineBreakMeasurer(it, frc);
            while (measurer.getPosition() < it.getEndIndex()) {
                TextLayout layout = measurer.nextLayout(maxWidth);
                y += layout.getAscent() + layout.getDescent() + layout.getLeading() + lineGap;
            }
        }
        return y;
    }

    /** A short gold line on each side of a small centered diamond. */
    private void drawOrnament(Graphics2D g, float centerX, float y, float totalWidth) {
        float gap = Math.max(8f, totalWidth * 0.06f);
        float half = totalWidth / 2f;
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(Math.max(1.4f, totalWidth / 180f)));
        g.draw(new Line2D.Float(centerX - half, y, centerX - gap, y));
        g.draw(new Line2D.Float(centerX + gap, y, centerX + half, y));
        float s = Math.max(4f, totalWidth / 70f);
        Path2D.Float diamond = new Path2D.Float();
        diamond.moveTo(centerX, y - s);
        diamond.lineTo(centerX + s, y);
        diamond.lineTo(centerX, y + s);
        diamond.lineTo(centerX - s, y);
        diamond.closePath();
        g.fill(diamond);
    }

    private void drawQrPlaceholder(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(0xDD, 0xDD, 0xDD));
        int cells = 9;
        int cell = size / cells;
        for (int r = 0; r < cells; r++) {
            for (int c = 0; c < cells; c++) {
                if (((r + c) % 2) == 0) {
                    g.fillRect(x + (c * cell), y + (r * cell), cell - 2, cell - 2);
                }
            }
        }
    }

    private BufferedImage readImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage loadLogo() {
        if (logoLoaded) {
            return cachedLogo;
        }
        synchronized (GiftCardImageService.class) {
            if (logoLoaded) {
                return cachedLogo;
            }
            try (InputStream in = getClass().getResourceAsStream("/images/tahadaw-logo.png")) {
                if (in != null) {
                    BufferedImage raw = ImageIO.read(in);
                    cachedLogo = raw != null ? makeBlackTransparent(raw) : null;
                }
            } catch (Exception ex) {
                cachedLogo = null;
            }
            logoLoaded = true;
            return cachedLogo;
        }
    }

    private static BufferedImage makeBlackTransparent(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int gr = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r < 28 && gr < 28 && b < 28) {
                    out.setRGB(x, y, 0x00000000);
                } else {
                    out.setRGB(x, y, argb);
                }
            }
        }
        return out;
    }

    // Bundled Arabic fonts (Amiri, OFL) so rendering is identical on any OS/server,
    // including headless Linux containers where no system Arabic font is installed.
    private static volatile Font baseRegular;
    private static volatile Font baseBold;
    private static volatile boolean fontsLoaded;

    private static void loadBundledFonts() {
        if (fontsLoaded) {
            return;
        }
        synchronized (GiftCardImageService.class) {
            if (fontsLoaded) {
                return;
            }
            baseRegular = loadFontResource("/fonts/Amiri-Regular.ttf");
            baseBold = loadFontResource("/fonts/Amiri-Bold.ttf");
            fontsLoaded = true;
        }
    }

    private static Font loadFontResource(String path) {
        try (InputStream in = GiftCardImageService.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Font pickFont(int style, int size) {
        loadBundledFonts();
        Font base = ((style & Font.BOLD) != 0) ? baseBold : baseRegular;
        if (base == null) {
            base = baseRegular != null ? baseRegular : baseBold;
        }
        if (base != null) {
            // The bundled file already carries the right weight; only size it.
            return base.deriveFont(Font.PLAIN, (float) size);
        }
        // Fallback: rely on a system Arabic-capable font.
        for (String name : new String[]{"Segoe UI", "Tahoma", "Arial", "Dialog"}) {
            Font font = new Font(name, style, size);
            if (font.canDisplay('\u062A')) {
                return font;
            }
        }
        return new Font("Dialog", style, size);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
