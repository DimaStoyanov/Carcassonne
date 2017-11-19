package messages;

/**
 * Created by Dmitrii Stoianov
 */


public class DefaultMsg {

    /**
     * Unique code of message. Every error has different code.
     */
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final int code;


    /**
     * Type of message (e.g. Error message)
     */
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Type type;


    /**
     * Description of message
     */
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String description;

    DefaultMsg(Type type, int code, String description) {
        this.type = type;
        this.code = code;
        this.description = description;
    }

    public DefaultMsg(String description, int code) {
        this(Type.DEFAULT, code, description);
    }

    public int getCode() {
        return code;
    }

    enum Type {
        DEFAULT, TOKEN, EMAIL
    }

}
