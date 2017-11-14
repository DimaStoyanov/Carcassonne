package messages;

/**
 * Created by Dmitrii Stoianov
 */


public class OKMsg extends AbstractMsg {

    public OKMsg(String description, int code) {
        super(Type.OK, code, description);
    }
}
