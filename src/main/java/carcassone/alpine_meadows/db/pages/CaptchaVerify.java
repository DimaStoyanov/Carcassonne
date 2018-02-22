package carcassone.alpine_meadows.db.pages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Dmitrii Stoianov
 */


// Class for get json response from request that checks google captcha response
public class CaptchaVerify {

    private Boolean success;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public Boolean getSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return String.format("CaptchaVerify{success=%s, error_codes=%s}", success, errorCodes);
    }
}
