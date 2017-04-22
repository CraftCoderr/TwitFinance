package com.qvim.hs.api;

import lombok.Data;
import org.json.simple.JSONObject;

/**
 * Created by RINES on 22.04.17.
 */
@Data
public class ApiRequest {

    private JSONObject result;

    public void error(ApiError error) {
        this.result = new JSONObject();
        this.result.put("error", true);
        this.result.put("error_code", error.ordinal() + 1);
        this.result.put("error_desc", error.getDescription());
    }

    public void error(ApiError error, String... replacers) {
        this.result = new JSONObject();
        this.result.put("error", true);
        this.result.put("error_code", error.ordinal() + 1);
        this.result.put("error_desc", String.format(error.getDescription(), replacers));
    }

    public String toJsonString() {
        if(this.result == null)
            error(ApiError.EMPTY_RESULT);
        return this.result.toJSONString();
    }

}
