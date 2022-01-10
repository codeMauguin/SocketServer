package com.test;

import com.alibaba.fastjson.JSON;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 7:43 PM 2022/1/8
 * @Modified By:
 */
public class T2 {
    public static void main(String[] args) {
        p p = JSON.parseObject("{id:1,name:\"陈浩\",hobby:[{1:[1]}],raw:{a:1,b:2}}", p.class);
        System.out.println(p);
    }
}
