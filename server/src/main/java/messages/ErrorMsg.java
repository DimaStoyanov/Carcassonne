package messages;

/**
 * Created by Dmitrii Stoianov
 */


public class ErrorMsg extends AbstractMsg {

    public ErrorMsg(String description, int code) {
        super(Type.ERROR, code, description);
    }
}
