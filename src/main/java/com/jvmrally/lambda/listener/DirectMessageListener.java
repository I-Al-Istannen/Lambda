package com.jvmrally.lambda.listener;

import static com.jvmrally.lambda.db.tables.DmTimeouts.DM_TIMEOUTS;
import com.jvmrally.lambda.JooqConn;
import com.jvmrally.lambda.db.tables.pojos.DmTimeouts;
import org.jooq.DSLContext;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * DirectMessageListener
 */
public class DirectMessageListener extends ListenerAdapter {
    private static DSLContext dsl = JooqConn.getContext();

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }
        long authorId = e.getAuthor().getIdLong();
        long now = System.currentTimeMillis();
        dsl.selectFrom(DM_TIMEOUTS).where(DM_TIMEOUTS.USERID.eq(authorId))
                .fetchOptionalInto(DmTimeouts.class).ifPresentOrElse(
                        timeout -> updateTimeout(timeout, now), () -> insertTimeout(authorId, now));
        logMessage(e);
    }

    /**
     * Updates the time of the last message sent by the user
     * 
     * @param timeout the existing timeout record
     * @param now     the current time in milliseconds from the epoch
     */
    private void updateTimeout(DmTimeouts timeout, long now) {
        dsl.update(DM_TIMEOUTS).set(DM_TIMEOUTS.LAST_MESSAGE_TIME, now)
                .where(DM_TIMEOUTS.USERID.eq(timeout.getUserid())).execute();
    }

    /**
     * Inserts a new record of a timeout for a user
     * 
     * @param authorId the userid
     * @param now      the current time in milliseconds from the epoch
     */
    private void insertTimeout(long authorId, long now) {
        dsl.insertInto(DM_TIMEOUTS).values(authorId, now).execute();
    }

    /**
     * Sends the received message to any modmail channel the bot has access to
     * 
     * @param e the received message event
     */
    private void logMessage(PrivateMessageReceivedEvent e) {
        String message = "User: " + e.getAuthor().getAsMention() + " sent message: "
                + e.getMessage().getContentRaw();
        var channels = e.getJDA().getTextChannelsByName("modmail", true);
        channels.forEach(channel -> channel.sendMessage(message).queue());
    }
}