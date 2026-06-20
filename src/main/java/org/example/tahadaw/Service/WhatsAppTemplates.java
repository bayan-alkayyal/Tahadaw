package org.example.tahadaw.Service;

import org.example.tahadaw.Model.*;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class WhatsAppTemplates {

    private static final String SYSTEM_NAME = "تهادوا";

    private WhatsAppTemplates() {
    }


    public static String buildReminder(User user, Reminder reminder) {

        String recipientName = reminder.getRecipient() != null
                ? reminder.getRecipient().getName()
                : "المستلم";

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm a", Locale.forLanguageTag("ar-SA"));

        String formattedReminderDate = reminder.getReminderDate().format(formatter);

        return "🎁 تذكير من " + SYSTEM_NAME + "\n\n"
                + "مرحبًا " + user.getFullName() + " 👋\n\n"
                + "لديك تذكير مجدول لهدية " + recipientName + " 🎁\n\n"
                + "تفاصيل التذكير:\n"
                + reminder.getMessage() + "\n\n"
                + "وقت التذكير:\n"
                + formattedReminderDate + "\n\n"
                + "فريق " + SYSTEM_NAME;
    }


    public static String buildGroupGiftVoteReminder(GroupGiftInvite invite, GroupGift groupGift, String voteUrl) {
        return "Hello " + invite.getInviteeName() + "\n\n"
                + "Reminder: please vote on the group gift \"" + groupGift.getTitle() + "\".\n\n"
                + voteUrl + "\n\n"
                + SYSTEM_NAME + " team";
    }

    public static String buildOccasionReminder(User user, String recipientName, String occasionType, String occasionDate) {
        return "Hello " + user.getFullName() + "\n\n"
                + "Reminder: " + recipientName + "'s " + occasionType + " is coming up on " + occasionDate + ".\n\n"
                + "Plan a meaningful gift with " + SYSTEM_NAME + ".\n\n"
                + SYSTEM_NAME + " team";
    }


}
