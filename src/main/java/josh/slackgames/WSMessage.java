package josh.slackgames;

/**
 * Web socket message container
 * @author Josh Ellis - neuroclast@gmail.com
 */
public final class WSMessage {
    private final String type;
    private final String message;
    private final Object contents;

    public WSMessage(String type, String message, Object contents) {
        this.type = type;
        this.message = message;
        this.contents = contents;
    }

    public WSMessage(String type, String message) {
        this(type, message, null);
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Object getContents() {
        return contents;
    }
}
