package com.jvmrally.lambda.command.utility;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmrally.lambda.utility.Util;
import com.jvmrally.lambda.utility.messaging.EmbedMessage;
import com.jvmrally.lambda.utility.messaging.Messenger;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.ParsedEntity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import disparse.parser.reflection.Flag;

/**
 * Embed
 */
public class Embed {

    private Embed() {
    }

    @ParsedEntity
    static class EmbedRequest {
        @Flag(shortName = 'j', longName = "json", description = "Json input to create embed.")
        private String json;
    }

    @CommandHandler(commandName = "embed", description = "Create and post an embed from json input",
            roles = "admin")
    public static void execute(EmbedRequest req, MessageReceivedEvent e) throws IOException {
        ObjectMapper om = new ObjectMapper();
        EmbedMessage embed = om.readValue(req.json, EmbedMessage.class);
        Util.getTargetChannel(e).ifPresentOrElse(channel -> Messenger.send(channel, embed.build()),
                () -> Messenger.send(e.getChannel(), "Must provide a target channel"));
    }
}
