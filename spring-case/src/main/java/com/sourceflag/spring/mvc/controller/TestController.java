package com.sourceflag.spring.mvc.controller;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 由 RequestMappingHandlerMapping 来处理 @RequestMapping 注解
 */
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

	@ModelAttribute
	public void populateModel(@RequestParam(required = false) String abc, Model model) {
		System.out.println("==== 调用了 modelAttribute 方法 ====");
		model.addAttribute("attributeName", abc);
	}

	@Data
	public static class UserEntity {
		private String name;
		private Integer age;
	}

}
