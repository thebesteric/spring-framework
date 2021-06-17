package com.sourceflag.spring.mvc.controller;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TestController {


    @RequestMapping("/test.do")
    @ResponseBody
    public Object test(String name, HttpServletRequest req, HttpServletResponse resp, UserEntity userEntity) {
        System.out.println("name = " + name);
        System.out.println("req = " + req);
        System.out.println("resp = " + resp);
        System.out.println("userEntity = " + userEntity);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("userEntity", userEntity);
        return map;
    }

    @RequestMapping("/index.do")
    public String index() {
        return "index";
    }

    @Data
	public static class UserEntity {
		private String name;
		private Integer age;
	}

}
