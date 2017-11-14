package messages;

/**
 * Created by Dmitrii Stoianov
 */


public class TokenMsg extends AbstractMsg {

    @SuppressWarnings("unused")
    private final String token;

    public TokenMsg(String token) {
        super(Type.TOKEN, 200, "Save this token to local storage of browser." +
                "It must be attached to the socket header and also as a parameter " +
                "of a requests associated with account management");
        this.token = token;
    }
}
