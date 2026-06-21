package org.example.tahadaw.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Model.GiftCard;
import org.example.tahadaw.Model.GroupGift;
import org.example.tahadaw.Model.GroupGiftInvite;
import org.example.tahadaw.Model.Payment;
import org.example.tahadaw.Model.Reminder;
import org.example.tahadaw.Model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String SYSTEM_NAME = "تهادوا";
    private static final String GIFT_CARD_CID = "giftCardImage";

    private final JavaMailSender mailSender;
    private final PdfReceiptService pdfReceiptService;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public void sendGiftCardEmail(GiftCard giftCard, User sender, String toEmail) {
        String subject = SYSTEM_NAME + " — Your Gift Card";
        boolean hasImage = giftCard.getGiftCardImage() != null && giftCard.getGiftCardImage().length > 0;

        if (hasImage) {
            sendGiftCardEmailWithImage(
                    toEmail,
                    subject,
                    EmailHtmlTemplates.buildGiftCardEmailHtml(giftCard, sender, GIFT_CARD_CID),
                    EmailHtmlTemplates.buildGiftCardEmailPlainText(giftCard, sender),
                    giftCard.getGiftCardImage()
            );
        } else {
            sendHtmlEmail(
                    toEmail,
                    subject,
                    EmailHtmlTemplates.buildGiftCardEmailHtml(giftCard, sender, null),
                    EmailHtmlTemplates.buildGiftCardEmailPlainText(giftCard, sender)
            );
        }
    }

    public void sendPaymentReceiptEmail(User user, Payment payment) {
        String subject = SYSTEM_NAME + " — Premium Payment Receipt";
        byte[] pdf = pdfReceiptService.buildPremiumReceiptPdf(user, payment);

        sendHtmlEmailWithAttachment(
                user.getEmail(),
                subject,
                EmailHtmlTemplates.buildPaymentReceiptHtml(user, payment),
                EmailHtmlTemplates.buildPaymentReceiptPlainText(user, payment),
                "tahadaw-receipt.pdf",
                pdf,
                "application/pdf"
        );
    }

    public void sendReminderEmail(User user, Reminder reminder) {
        String subject = SYSTEM_NAME + " — Gift Reminder";

        sendHtmlEmail(
                user.getEmail(),
                subject,
                EmailHtmlTemplates.buildReminderHtml(user, reminder),
                EmailHtmlTemplates.buildReminderPlainText(user, reminder)
        );
    }

    //Bayan
    public void sendGroupGiftInviteEmail(GroupGiftInvite invite, GroupGift groupGift) {
        String subject = SYSTEM_NAME + " — دعوة للتصويت على هدية جماعية";

        String html = """
        <h2>تمت دعوتك للتصويت على هدية جماعية</h2>

        <p>مرحبًا %s،</p>

        <p>
            تمت دعوتك للمشاركة في التصويت على هدية جماعية عبر منصة <strong>تهادوا</strong>.
        </p>

        <p>
            <strong>عنوان التصويت:</strong> %s
        </p>

        <p>
            <strong>المستلم:</strong> %s
        </p>

        <p>
            مشاركتك تساعد في اختيار الهدية الأنسب من بين الخيارات المقترحة.
            يرجى الدخول إلى النظام والاطلاع على خيارات الهدايا، ثم اختيار الهدية الأنسب في نظرك.
        </p>

        <p>
            شكرًا لمشاركتك.
        </p>
        """.formatted(
                invite.getInviteeName(),
                groupGift.getTitle(),
                groupGift.getRecipient().getName()
        );

        String plainText = """
        مرحبًا %s،

        تمت دعوتك للمشاركة في التصويت على هدية جماعية عبر منصة تهادوا.

        عنوان التصويت:
        %s

        المستلم:
        %s

        مشاركتك تساعد في اختيار الهدية الأنسب من بين الخيارات المقترحة.
        يرجى الدخول إلى النظام والاطلاع على خيارات الهدايا، ثم اختيار الهدية الأنسب في نظرك.

        شكرًا لمشاركتك.
        """.formatted(
                invite.getInviteeName(),
                groupGift.getTitle(),
                groupGift.getRecipient().getName()
        );

        sendHtmlEmail(
                invite.getInviteeEmail(),
                subject,
                html,
                plainText
        );
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody, String plainBody) {
        if (to == null || to.isBlank()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainBody, htmlBody);

            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new ApiException("Failed to send email: " + e.getMessage());
        }
    }

    private void sendHtmlEmailWithAttachment(String to,
                                             String subject,
                                             String htmlBody,
                                             String plainBody,
                                             String attachmentName,
                                             byte[] attachmentBytes,
                                             String contentType) {
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainBody, htmlBody);

            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }

            helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes), contentType);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new ApiException("Failed to send email: " + e.getMessage());
        }
    }

    private void sendGiftCardEmailWithImage(String to,
                                            String subject,
                                            String htmlBody,
                                            String plainBody,
                                            byte[] imageBytes) {
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainBody, htmlBody);

            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }

            ByteArrayResource image = new ByteArrayResource(imageBytes);
            // Inline preview shown in the email body, plus a downloadable copy.
            helper.addInline(GIFT_CARD_CID, image, "image/png");
            helper.addAttachment("gift-card.png", image, "image/png");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new ApiException("Failed to send gift card email: " + e.getMessage());
        }
    }
}
