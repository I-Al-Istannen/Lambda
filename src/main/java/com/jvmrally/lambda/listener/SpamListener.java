package com.jvmrally.lambda.listener;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.jvmrally.lambda.db.enums.AuditAction;
import com.jvmrally.lambda.injectable.Auditor;
import com.jvmrally.lambda.utility.Util;
import com.jvmrally.lambda.utility.messaging.Messenger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * SpamListener
 */
public class SpamListener extends ListenerAdapter {

    private static final String ATTACHMENT_FILTER = "^.*\\.(jpg|jpeg|gif|png|mov|mp4|webm)";

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || Util.hasRole(e.getMember(), "Admin")) {
            return;
        }
        Message message = e.getMessage();
        if (testMessage(message, getMessagePredicates())) {
            message.delete().queue();
            Messenger.send(e.getMember(), getWarning());
            Auditor.getAuditor().log(AuditAction.AUTOMATED_WARN,
                    e.getJDA().getSelfUser().getIdLong(), e.getMember().getIdLong(),
                    "Automatic warning from spam prevention.");
        }
    }

    /**
     * Build the embed warning message that is sent to users that trigger a message predicate
     * 
     * @return the message embed
     */
    private MessageEmbed getWarning() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("**Warning**");
        eb.setColor(Color.RED);
        eb.setDescription("Your last message was deleted by our spam filter.");
        eb.addField("Invites", "We don't allow advertising. Don't post invite links.", false);
        eb.addField("Channel Mentions", "Don't spam channel mentions in messages.", false);
        eb.addField("User Mentions", "Don't spam user mentions in messages.", false);
        eb.addField("Attachments",
                "We only allow images and videos as attachments. If you're trying to post some code, please use a code sharing site and post the link.",
                false);
        return eb.build();
    }

    /**
     * Tests the input message against a collection of predicates
     * 
     * @param message    the message to test
     * @param predicates the predicates to test against
     * @return true if a message evaluates truthfully against one of the predicates
     */
    private boolean testMessage(Message message, List<Predicate<Message>> predicates) {
        for (var predicate : predicates) {
            if (predicate.test(message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of message predicates
     * 
     * @return the list of predicates
     */
    private List<Predicate<Message>> getMessagePredicates() {
        List<Predicate<Message>> messagePredicates = new ArrayList<>();
        messagePredicates.add(Message::mentionsEveryone);
        messagePredicates.add(m -> m.getMentionedUsers().size() > 4);
        messagePredicates.add(m -> m.getMentionedChannels().size() > 4);
        messagePredicates.add(m -> !m.getInvites().isEmpty());
        messagePredicates.add(m -> !m.getAttachments().stream()
                .filter(attachment -> !attachment.getFileName().matches(ATTACHMENT_FILTER))
                .collect(Collectors.toList()).isEmpty());
        return messagePredicates;
    }
}
