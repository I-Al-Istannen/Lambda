package com.jvmrally.lambda.command.utility.javadoc;

import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;

/**
 * The request structure for the javadoc command.
 */
@ParsedEntity
class JavadocRequest {

    @Flag(shortName = 's', longName = "short",
            description = "Whether to show a short summary")
    private boolean shorten = false;

    @Flag(shortName = 'q', longName = "query", description = "The javadoc query.", required = true)
    private String query;

    /**
     * @return Whether to show a summary
     */
    boolean shorten() {
        return shorten;
    }

    /**
     * @return The javadoc query
     */
    String query() {
        return query;
    }
}
