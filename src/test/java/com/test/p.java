package com.test;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 10:15 AM 2022/1/10
 * @Modified By:
 */
public class p implements Serializable {
    @Serial
    private static final long serialVersionUID = 7984217311850560455L;
    int id = 12;
    String name = "2112";
    List<Map<Integer, ArrayList<String>>> hobby;
    Map<String, Integer> raw;

    @Override
    public String toString() {
        return "ui{" + "id=" + id + ", name='" + name + '\'' + ", hobby=" + hobby + ", raw=" + raw
                + '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Map<Integer, ArrayList<String>>> getHobby() {
        return hobby;
    }

    public void setHobby(List<Map<Integer, ArrayList<String>>> hobby) {
        this.hobby = hobby;
    }

    public Map<String, Integer> getRaw() {
        return raw;
    }

    public void setRaw(Map<String, Integer> raw) {
        this.raw = raw;
    }
}
