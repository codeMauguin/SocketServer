package com.controller;

import org.context.Bean.annotation.Resource;
import web.http.Controller.annotation.Controller;
import web.http.Controller.annotation.GetMapper;
import web.http.annotation.Component;
import web.http.annotation.Service;

import java.util.Arrays;
import java.util.List;

@Component
class pojo {
    int code = 200;
    String msg = "成功获取";
    Object id;
    String name;
    List<u> us = Arrays.asList(new u(), new u());
    @Resource
    private u u;

    public pojo(u id) {
        this.id = id;
        name = "陈浩-" + id;
    }
}

@Service
class u {
    int id;

    public u() {
        this.id = 1;
    }

    @Override
    public String toString() {
        return "u{" +
                "id=" + id +
                '}';
    }
}

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 4:54 下午 2021/11/29
 * @Modified By:
 */
@Controller
public class hello {
    @Resource
    private pojo pojo;

    @GetMapper("/api")
    pojo hello() {
        return pojo;
    }
}
