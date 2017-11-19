package messages;

/**
 * Created by Dmitrii Stoianov
 */


public class EmailMsg extends DefaultMsg {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String email;

    public EmailMsg(String email, String description, int code) {
        super(Type.EMAIL, code, description);
        this.email = email;
    }
}
