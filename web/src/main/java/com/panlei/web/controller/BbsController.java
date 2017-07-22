package com.panlei.web.controller;


import com.panlei.web.service.BbsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by 361pa on 2017/6/20.
 */
@Controller
@RequestMapping("/bbs")
public class BbsController {

    @Autowired
    BbsService bbsService;

    @RequestMapping(value = "/top10", method = RequestMethod.GET,  produces = "application/json; charset=utf-8")
    @ResponseBody
    public Map getTop10() {
        return bbsService.getTop10();
    }

    @RequestMapping(value = "/context", method = RequestMethod.POST)
    @ResponseBody
    public Map getBbsContext(@RequestBody Map<String, String> requestUrl) throws Exception {
        System.out.print(requestUrl.values());
        return bbsService.getBbsContext(requestUrl.get("url"));
    }

    @RequestMapping(value = "/topall", method = RequestMethod.GET)
    @ResponseBody
    public Map getTopAll() {
        return bbsService.getTopAll();
    }

    @RequestMapping("/file/{cateogry.rp}")
    public void getIcon(@PathVariable("cateogry.rp") String cateogry,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        if(StringUtils.isEmpty(cateogry)) {
            cateogry = "";
        }

        //获取当前系统信息
        Properties props=System.getProperties(); //获得系统属性集
        String osName = props.getProperty("os.name"); //操作系统名称

        File file;
        if (osName.indexOf("Windows") > -1){
            file = new File("d:/" + cateogry);
        }else {
            file = new File("./" + cateogry);
        }

        //File file = new File(fileName);

        //判断文件是否存在如果不存在就返回默认图标
        if(!(file.exists() && file.canRead())) {
            file = new File("./"  + "time.gif");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int)file.length()];
        int length = inputStream.read(data);
        inputStream.close();

        response.setContentType("image/png");

        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();
    }

}
