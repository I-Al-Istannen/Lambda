package com.jvmrally.lambda.command.utility.javadoc.formatting;

import com.jvmrally.lambda.utility.messaging.Messenger;
import de.ialistannen.htmljavadocparser.model.doc.JavadocComment;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sends result messages for javadoc queries.
 */
public class JavadocMessageSender {

    private final JavadocMessageFormatter messageFormatter;

    /**
     * Creates a new Javadoc message sender.
     *
     * @param messageFormatter the message formatter to use
     */
    public JavadocMessageSender(JavadocMessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    /**
     * Sends a message containing the javadoc results. Either a single javadoc, a list with found
     * classes or an error.
     *
     * @param elements the found elements
     * @param channel  the channel to send it to
     */
    public void sendResult(List<? extends JavadocElement> elements, MessageChannel channel) {
        if (elements.isEmpty()) {
            sendNothingFound(channel);
            return;
        }
        if (elements.size() == 1) {
            sendSingleJavadoc(channel, elements.get(0));
            return;
        }
        sendMultipleFoundError(channel, elements);
    }

    /**
     * Sends a single javadoc result.
     *
     * @param channel the channel to send it to
     * @param element the element
     */
    public void sendSingleJavadoc(MessageChannel channel, JavadocElement element) {
        EmbedBuilder embed = new EmbedBuilder();
        Optional<JavadocComment> javadoc = element.getJavadoc();
        if (javadoc.isEmpty()) {
            embed.setDescription("No javadoc found on that element");
        } else {
            messageFormatter.format(embed, element);
        }

        Messenger.send(channel, embed.build());
    }

    /**
     * Sends an error message saying that no types were found.
     *
     * @param channel the channel to send it to
     */
    public void sendNothingFound(MessageChannel channel) {
        Messenger.send(channel, new EmbedBuilder().setDescription("Nothing found :(").build());
    }

    /**
     * Sends an error with a list of the found elements.
     *
     * @param channel       the channel to send it to
     * @param foundElements the found elements
     */
    public void sendMultipleFoundError(MessageChannel channel, List<? extends JavadocElement> foundElements) {
        String types = foundElements.stream()
                .map(JavadocElement::getFullyQualifiedName)
                .map(s -> "`" + s + "`")
                .limit(10)
                .collect(Collectors.joining("\n**\\*** ", "**\\*** ", ""));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("I found at least the following types:")
                .setDescription(types);

        Messenger.send(channel, embed.build());
    }
}
