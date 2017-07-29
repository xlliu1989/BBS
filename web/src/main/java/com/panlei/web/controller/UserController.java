package com.panlei.web.controller;

import com.panlei.web.model.User;
import com.panlei.web.model.UserNju;
import com.panlei.web.service.NjuUserService;
import com.panlei.web.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zhangxq on 2016/7/15.
 */

@Controller
@RequestMapping("/bbs")
public class UserController {

    private Logger log = Logger.getLogger(UserController.class);
    @Resource
    private UserService userService;

    @Autowired
    NjuUserService njuUserService;

    @RequestMapping("/showUser")
    public String showUser(HttpServletRequest request, Model model){
        log.info("查询所有用户信息");
        List<User> userList = userService.getAllUser();
        model.addAttribute("userList",userList);
        return "showUser";
    }

    @RequestMapping(value = "/listUser", method = RequestMethod.GET)
    @ResponseBody
    public Map showUserList(){
        log.info("查询所有用户信息");
        List<User> userList = userService.getAllUser();
        Map map = new HashMap();
        map.put("list", userList);
        return map;
    }

    @RequestMapping(value = "/user/bind",method = RequestMethod.POST)
    @ResponseBody
    public String bindUser(@RequestBody UserNju userNju) throws Exception {
        System.out.print(userNju.getUserName());


        return njuUserService.createUser(userNju);
    }


    @RequestMapping(value = "/user/bind/get",method = RequestMethod.POST)
    @ResponseBody
    public String getUserBind(@RequestBody UserNju userNju) throws Exception {

        return njuUserService.getUserBind(userNju);
    }

    @RequestMapping(value = "/user/bind/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteUserBind(@RequestBody UserNju userNju) throws Exception {

        return njuUserService.deleteUserBind(userNju);
    }

}
