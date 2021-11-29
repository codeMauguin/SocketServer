package com.controller;

import web.http.Controller.annotation.Controller;
import web.http.Controller.annotation.GetMapper;

import java.util.Map;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:54 下午 2021/11/29
 * @Modified By:
 */
class pojo {
    int code = 200;
    String msg = "成功获取";
    Object id;
    String name;


    public pojo(Object id) {
        this.id = id;
        name = "陈浩-" + id;
    }
}

@Controller
public class hello {
    @GetMapper("/api")
    pojo hello(Map<String, Object> test) {
        return new pojo(test.get("test"));
    }
}
