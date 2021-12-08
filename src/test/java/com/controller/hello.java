package com.controller;

import org.context.Bean.annotation.Resource;
import web.http.Controller.annotation.Controller;
import web.http.Controller.annotation.PostMapper;
import web.http.annotation.Component;
import web.http.annotation.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
class pojo {
    boolean code;
    String msg = "成功获取";
    Object id;
    String name;
    List<u> us = Arrays.asList(new u(null), new u(null));

    public boolean getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<com.controller.u> getUs() {
        return us;
    }

}

@Service
class u {
    int id;

    public u(pojo pojo) {
        this.id = 1;
    }

    public int getId() {
        return id;
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

    public void setPojo(com.controller.pojo pojo) {
        this.pojo = pojo;
    }

    @PostMapper("/api")
    pojo hello(List<Integer> test, Map<String, Integer> table) {
        System.out.println("test = " + test);
        System.out.println("table = " + table);
        return pojo;
    }
}
