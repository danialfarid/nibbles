package com.df.nibbles.api;

import java.util.Map;

public class Response {
    public String type;
    public Map<String, Object> data;

    public Response() {
    }

    public Response(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }
}
