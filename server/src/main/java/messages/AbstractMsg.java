package messages;

/**
 * Created by Dmitrii Stoianov
 */


public abstract class AbstractMsg {

    /**
     * Unique code of message. Every error has different code.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int code;


    /**
     * Type of message (e.g. Error message)
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final Type type;


    /**
     * Description of message
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String description;

    AbstractMsg(Type type, int code, String description) {
        this.type = type;
        this.code = code;
        this.description = description;
    }


    enum Type {
        ERROR, OK, TOKEN
    }

}
