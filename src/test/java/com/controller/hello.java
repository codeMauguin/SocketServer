package com.controller;

import org.context.Bean.annotation.Resource;
import web.http.Controller.annotation.Controller;
import web.http.Controller.annotation.PutMapper;
import web.http.Controller.annotation.RequestMapper;
import web.http.Libary.RequestMethod;
import web.http.annotation.Component;
import web.http.annotation.Service;

@Component
class pojo {
    boolean code;
    String msg = "成功获取";
    int id;
    String name;

    private pojo() {
    }

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

    @PutMapper("/put")
    pojo put() {
        return pojo;
    }

    @RequestMapper(value = "/api", methods = {RequestMethod.GET, RequestMethod.POST})
    pojo hello(int id) {
        pojo.id = id;
        return pojo;
    }
}
