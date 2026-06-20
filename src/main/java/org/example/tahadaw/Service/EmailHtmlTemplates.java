package org.example.tahadaw.Service;

import org.example.tahadaw.Model.GiftCard;
import org.example.tahadaw.Model.GroupGift;
import org.example.tahadaw.Model.GroupGiftInvite;
import org.example.tahadaw.Model.Payment;
import org.example.tahadaw.Model.Reminder;
import org.example.tahadaw.Model.User;

public final class EmailHtmlTemplates {

    private static final String SYSTEM_NAME = "Tahadaw";

    private EmailHtmlTemplates() {
    }

    private static final String NAVY = "#2D3A47";
    private static final String GOLD = "#C9A24B";
    private static final String BG = "#F4F5F7";
    private static final String TEXT = "#4B5563";
    private static final String MUTED = "#9AA1AC";

    /**
     * @param inlineCid content-id of the inline gift-card image, or {@code null} to omit the preview.
     */
    public static String buildGiftCardEmailHtml(GiftCard giftCard, User sender, String inlineCid) {
        String recipient = escape(giftCard.getRecipientName());
        String senderName = escape(sender.getFullName());

        String imageBlock = inlineCid == null ? "" : """
                <tr><td style="padding:4px 32px 8px;text-align:center;">
                  <img src="cid:%s" alt="Gift Card" width="360"
                       style="max-width:100%%;border-radius:14px;border:1px solid #e5e7eb;" />
                </td></tr>
                """.formatted(inlineCid);

        return """
                <!doctype html>
                <html><body style="margin:0;padding:0;background:%s;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;padding:24px 0;font-family:'Segoe UI',Arial,sans-serif;">
                  <tr><td align="center">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 6px 24px rgba(0,0,0,0.08);">
                      <tr><td style="background:%s;padding:28px 32px;text-align:center;">
                        <div style="color:#ffffff;font-size:24px;font-weight:700;letter-spacing:3px;">TAHADAW</div>
                        <div style="color:%s;font-size:12px;margin-top:8px;letter-spacing:2px;">YOUR GIFT CARD</div>
                      </td></tr>
                      <tr><td style="padding:30px 32px 8px;">
                        <p style="font-size:16px;color:%s;margin:0 0 12px;">Hello <strong>%s</strong>,</p>
                        <p style="font-size:15px;color:%s;line-height:1.6;margin:0 0 8px;">
                          <strong>%s</strong> has sent you a gift card through Tahadaw. We hope it brings a smile to your day.
                        </p>
                      </td></tr>
                      %s
                      <tr><td style="padding:8px 32px 28px;">
                        <p style="font-size:13px;color:%s;line-height:1.6;margin:14px 0 0;">
                          A copy of your gift card is attached to this email so you can download and keep it.
                        </p>
                      </td></tr>
                      <tr><td style="background:%s;padding:18px 32px;text-align:center;color:%s;font-size:12px;">
                        Sent with care by Tahadaw
                      </td></tr>
                    </table>
                  </td></tr>
                </table>
                </body></html>
                """.formatted(BG, BG, NAVY, GOLD, NAVY, recipient, TEXT, senderName, imageBlock, MUTED, BG, MUTED);
    }

    public static String buildGiftCardEmailPlainText(GiftCard giftCard, User sender) {
        return SYSTEM_NAME + " — Your Gift Card\n\n"
                + "Hello " + giftCard.getRecipientName() + ",\n\n"
                + sender.getFullName() + " has sent you a gift card through " + SYSTEM_NAME + ".\n\n"
                + "Your gift card is attached to this email. Enjoy your gift!";
    }

    public static String buildPaymentReceiptHtml(User user, Payment payment) {
        String name = escape(user.getFullName());
        String amount = formatAmount(payment.getAmountMinor(), payment.getCurrency());
        String date = payment.getCreatedAt() != null ? payment.getCreatedAt().toString().replace('T', ' ') : "-";
        String status = escape(payment.getStatus());
        String receiptNo = "#" + payment.getId();

        return """
                <!doctype html>
                <html><body style="margin:0;padding:0;background:%s;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;padding:24px 0;font-family:'Segoe UI',Arial,sans-serif;">
                  <tr><td align="center">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 6px 24px rgba(0,0,0,0.08);">
                      <tr><td style="background:%s;padding:28px 32px;text-align:center;">
                        <div style="color:#ffffff;font-size:24px;font-weight:700;letter-spacing:3px;">TAHADAW</div>
                        <div style="color:%s;font-size:12px;margin-top:8px;letter-spacing:2px;">PAYMENT RECEIPT</div>
                      </td></tr>
                      <tr><td style="padding:30px 32px 6px;">
                        <p style="font-size:16px;color:%s;margin:0 0 12px;">Hello <strong>%s</strong>,</p>
                        <p style="font-size:15px;color:%s;line-height:1.6;margin:0 0 20px;">
                          Thank you for upgrading to Premium. Your account now unlocks the Surprise Plan and Gift Card features.
                        </p>
                      </td></tr>
                      <tr><td style="padding:0 32px;">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;border-radius:12px;">
                          <tr><td style="padding:18px 22px;">
                            <div style="font-size:12px;color:%s;letter-spacing:1px;">AMOUNT PAID</div>
                            <div style="font-size:26px;color:%s;font-weight:700;margin-top:4px;">%s</div>
                          </td></tr>
                        </table>
                      </td></tr>
                      <tr><td style="padding:18px 32px 4px;">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="font-size:14px;color:%s;">
                          <tr><td style="padding:8px 0;border-bottom:1px solid #eef0f2;color:%s;">Receipt No.</td><td style="padding:8px 0;border-bottom:1px solid #eef0f2;text-align:right;color:%s;">%s</td></tr>
                          <tr><td style="padding:8px 0;border-bottom:1px solid #eef0f2;color:%s;">Date</td><td style="padding:8px 0;border-bottom:1px solid #eef0f2;text-align:right;color:%s;">%s</td></tr>
                          <tr><td style="padding:8px 0;color:%s;">Status</td><td style="padding:8px 0;text-align:right;color:%s;">%s</td></tr>
                        </table>
                      </td></tr>
                      <tr><td style="padding:18px 32px 28px;">
                        <p style="font-size:13px;color:%s;line-height:1.6;margin:0;">A detailed PDF receipt is attached to this email for your records.</p>
                      </td></tr>
                      <tr><td style="background:%s;padding:18px 32px;text-align:center;color:%s;font-size:12px;">
                        Tahadaw — Intelligent Gift Planning
                      </td></tr>
                    </table>
                  </td></tr>
                </table>
                </body></html>
                """.formatted(
                        BG, BG, NAVY, GOLD, NAVY, name, TEXT,
                        BG, MUTED, NAVY, amount,
                        TEXT, MUTED, NAVY, receiptNo, MUTED, NAVY, date, MUTED, NAVY, status,
                        MUTED, BG, MUTED);
    }

    public static String buildPaymentReceiptPlainText(User user, Payment payment) {
        return SYSTEM_NAME + " — Premium Payment Receipt\n\n"
                + "Hello " + user.getFullName() + ",\n\n"
                + "Thank you for your premium purchase. Your account now unlocks Surprise Plan and Gift Card features.\n\n"
                + "Receipt No.: #" + payment.getId() + "\n"
                + "Amount: " + formatAmount(payment.getAmountMinor(), payment.getCurrency()) + "\n"
                + "Status: " + payment.getStatus() + "\n\n"
                + "A detailed PDF receipt is attached to this email.";
    }

    private static String formatAmount(Long amountMinor, String currency) {
        double major = amountMinor != null ? amountMinor / 100.0 : 0.0;
        String cur = currency != null ? currency : "";
        return String.format("%.2f %s", major, cur).trim();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String buildReminderHtml(User user, Reminder reminder) {
        return """
                <html><body>
                <h2>%s — Gift Reminder</h2>
                <p>Hello %s,</p>
                <p>%s</p>
                </body></html>
                """.formatted(SYSTEM_NAME, user.getFullName(), reminder.getMessage());
    }

    public static String buildReminderPlainText(User user, Reminder reminder) {
        return SYSTEM_NAME + " — Gift Reminder\n\n"
                + "Hello " + user.getFullName() + ",\n\n"
                + reminder.getMessage();
    }

    public static String buildGroupGiftInviteHtml(GroupGiftInvite invite, GroupGift groupGift, String voteUrl) {
        return """
                <html><body>
                <h2>%s — Group Gift Vote</h2>
                <p>Hello %s,</p>
                <p>You are invited to vote on a group gift: <strong>%s</strong>.</p>
                <p><a href="%s">Vote here</a></p>
                </body></html>
                """.formatted(SYSTEM_NAME, invite.getInviteeName(), groupGift.getTitle(), voteUrl);
    }

    public static String buildGroupGiftInvitePlainText(GroupGiftInvite invite, GroupGift groupGift, String voteUrl) {
        return SYSTEM_NAME + " — Group Gift Vote\n\n"
                + "Hello " + invite.getInviteeName() + ",\n\n"
                + "You are invited to vote on a group gift: " + groupGift.getTitle() + ".\n\n"
                + "Vote here: " + voteUrl;
    }
}
