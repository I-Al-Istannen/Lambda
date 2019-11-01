package com.jvmrally.lambda.command.utility.javadoc;

import com.jvmrally.lambda.command.utility.javadoc.docs.DocsApi;
import com.jvmrally.lambda.command.utility.javadoc.formatting.JavadocDescriptionFormatter;
import com.jvmrally.lambda.command.utility.javadoc.formatting.JavadocMessageFormatter;
import com.jvmrally.lambda.command.utility.javadoc.formatting.JavadocMessageSender;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Sends javadoc information in discord.
 */
public class JavadocCommand {

    private static DocsApi api;
    private static JavadocMessageSender longSender;
    private static JavadocMessageSender shortSender;

    static {
        api = new DocsApi();

        longSender = new JavadocMessageSender(new JavadocMessageFormatter(
                new JavadocDescriptionFormatter()
        ));

        shortSender = new JavadocMessageSender(new JavadocMessageFormatter(
                new ShortDescriptionFormatter()
        ));
    }

    private JavadocCommand() {
    }

    @CommandHandler(commandName = "docs.java", description = "Shows you the javadoc of a method, class, package or field.")
    public static void showDoc(JavadocRequest request, MessageReceivedEvent e) {
        JavadocSelector selector = JavadocSelector.fromString(request.query());
        List<? extends JavadocElement> elements = api.find(selector);

        if (request.shorten()) {
            shortSender.sendResult(elements, e.getChannel());
        } else {
            longSender.sendResult(elements, e.getChannel());
        }
    }

    private static class ShortDescriptionFormatter extends JavadocDescriptionFormatter {

        @Override
        protected String adjustDescription(String description) {
            return shortenToFirstParagraph(description);
        }

        private String shortenToFirstParagraph(String description) {
            int end = !description.contains("\n")
                    ? description.length()
                    : description.indexOf("\n");
            description = description.substring(0, end);
            return description;
        }
    }
}
