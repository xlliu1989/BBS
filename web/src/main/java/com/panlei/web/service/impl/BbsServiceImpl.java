package com.panlei.web.service.impl;


import com.panlei.web.dao.BoardMapper;
import com.panlei.web.dao.UserNjuMapper;
import com.panlei.web.model.BbsContext;
import com.panlei.web.model.Top10;
import com.panlei.web.model.TopAll;
import com.panlei.web.service.BbsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 361pa on 2017/6/20.
 */
@Service("bbsService")
public class BbsServiceImpl implements BbsService {

    private UserNjuMapper userNjuMapper;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public void setBoardMapper(BoardMapper boardMapper){
        this.userNjuMapper = userNjuMapper;
    }

    public Map getTop10() {
        Map<String, Object> result = new HashMap<String, Object>();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstop10").get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("&&&&&" + e.getMessage());
        }
        Elements trs = doc.select("table").select("tr");
        List resultList = new ArrayList<Top10>();
        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            if (i == 0) {
                continue;
            }
            Top10 top10 = new Top10();
            for (int j = 0; j < tds.size(); j++) {
                String text = tds.get(j).text();
                switch (j) {
                    case 0:
                        top10.setId(text);
                        break;
                    case 1:
                        top10.setZone(text);
                        break;
                    case 2:
                        String href = null;
                        String pattern = "href=\"([^\"]*)\"";
                        Pattern pKey = Pattern.compile(pattern, 2 | Pattern.DOTALL);
                        Matcher mKey = pKey.matcher(tds.get(j).toString());
                        if (mKey.find()) {
                            //System.out.print(mKey.group(1).replace(";", "&"));
                            href = "http://bbs.nju.edu.cn/" + mKey.group(1).replace(";", "&");
                            int start = href.indexOf("?");
                            top10.setHref(href.substring(start + 1));

                        }
                        System.out.print(top10.getHref());
                        top10.setTitle(text);
                        break;
                    case 3:
                        top10.setAuthor(text);
                        break;
                    case 4:
                        top10.setNumber(text);
                        break;
                }
                System.out.println(text);
            }
            resultList.add(top10);

        }
        result.put("top10", resultList);
        return result;
    }

    public Map getBbsContext(String url) throws Exception {
            Map<String, Object> result = new HashMap<String, Object>();
            Document doc = null;
            try {
                doc = Jsoup.connect(url).timeout(5000).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements trs = doc.select("table");
            List resultList = new ArrayList<BbsContext>();
            for (int i = 0; i < trs.size(); i++) {
                Elements tds = trs.get(i).select("td");

                BbsContext bbsContext = new BbsContext();
                for (int j = 0; j < tds.size(); j++) {

                    String text = tds.get(j).text();

                    if (j == 0) {
                        Integer start = text.indexOf(":");
                        Integer end = text.indexOf("]", start);
                        bbsContext.setThisAuthor(text.substring(start + 1, end));
                        int WatchNumberStart = text.lastIndexOf(":");
                        bbsContext.setThisWatchNumber(text.substring(WatchNumberStart + 1, text.length() - 1));
                    }

                    System.out.println("#" + i + "#" + j);
                    System.out.println(text);
                    if (j == 1) {
                        bbsContext.setThisFloor(text);
                    }
                    if (j == 2) {
                        int start = text.indexOf(bbsContext.getThisAuthor());
                        int end = text.indexOf(")", start);
                        bbsContext.setThisAuthorNickname(text.substring(start + bbsContext.getThisAuthor().length() + 2, end));

                        int startZone = text.indexOf("信区: ");
                        int endZone = text.indexOf("\n", startZone);
                        bbsContext.setThisZone(text.substring(startZone + 4, endZone));


                        int startTime = text.indexOf("南京大学小百合站");
                        int startTime1 = text.indexOf("(", startTime);
                        int endTime = text.indexOf(")", startTime1);
                        bbsContext.setThisTime(text.substring(startTime1 + 1, endTime));

                        int endContext = 0;
                        if (text.indexOf("--") == -1){
                            endContext = text.indexOf("※");
                        }else {
                            endContext = text.indexOf("--");
                        }

                        String context = text.substring(endTime + 1, endContext);
                        while (context.indexOf("http://bbs.nju.edu.cn/file/Pictures/") > -1){

                            int imageStart = context.indexOf("http://bbs.nju.edu.cn/file/Pictures/");
                            if (imageStart > 0){
                                int imageEnd = context.indexOf("\r\n",imageStart);
                                if (imageEnd == -1){
                                    imageEnd = context.indexOf("\n",imageStart);
                                }
                                String imageString = context.substring(imageStart, imageEnd);
                                readUrlPicture(imageString);
                                //imageString = "https://78560817.bbsnju.cn/ssm/bbs/file/BeautyGirl";
                                String imageStringNew = "</p><p><img src=\"" + "https://78560817.bbsnju.cn/ssm/bbs/file/"
                                        + imageString.substring(imageString.lastIndexOf("/")+1)+ "\" ></p><p>";
                                //<img src="http://bbs.nju.edu.cn/file/Pictures/LilyDroid0605102511.jpg" >
                                context = context.replace(imageString, imageStringNew);
                            }
                        }
                        bbsContext.setThisContext("<p>"+context+"</p>");

                        int startTitle = text.indexOf("标  题:");
                        int endTitle = text.indexOf("\n", startTitle);
                        bbsContext.setThisTitle(text.substring(startTitle + 5, endTitle));
                    }
                }
                resultList.add(bbsContext);
            }
            result.put("context", resultList);
            return result;
    }
    public void readUrlPicture(String urlString) throws Exception {
        //new一个URL对象
        URL url = new URL(urlString);
        //打开链接
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为5秒
        conn.setConnectTimeout(5 * 1000);
        //通过输入流获取图片数据
        InputStream inStream = conn.getInputStream();
        //得到图片的二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = readInputStream(inStream);
        //获取当前系统信息
        Properties props=System.getProperties(); //获得系统属性集
        String osName = props.getProperty("os.name"); //操作系统名称
        //根据系统创建文件路径；new一个文件对象用来保存图片，默认保存当前工程根目录
        Integer fileNameEnd = urlString.lastIndexOf("/");
        String fileName = urlString.substring(fileNameEnd + 1);
        File imageFile;
        if (osName.indexOf("Windows") > -1){
            imageFile = new File("d:/" + fileName);
        }else {
            imageFile = new File("./" + fileName);
        }

        //创建输出流
        FileOutputStream outStream = new FileOutputStream(imageFile);
        //写入数据
        outStream.write(data);
        //关闭输出流
        outStream.close();
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while( (len=inStream.read(buffer)) != -1 ){
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }


    public Map getTopAll(){
        Map<String, Object> result = new HashMap<String, Object>();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstopall").get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("&&&&&" + e.getMessage());
        }
        Elements trs = doc.select("table").select("tr");
        List resultList = new ArrayList<TopAll>();
        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            if (i == 0) {
                continue;
            }
            System.out.println(tds);

            for (int j = 0; j < tds.size(); j++) {

                String text = tds.get(j).text();
                //System.out.println(j + ">"+text);
                if (text.length() > 0){
                    TopAll topAll = new TopAll();
                    topAll.setTitle(text.split("\\[")[0]);
                    topAll.setBoard(text.split("\\[")[1].split("\\]")[0]);


                    String href = null;
                    String pattern = "href=\"([^\"]*)\"";
                    Pattern pKey = Pattern.compile(pattern, 2 | Pattern.DOTALL);
                    Matcher mKey = pKey.matcher(tds.get(j).toString());
                    if (mKey.find()) {
                        //System.out.print(mKey.group(1).replace(";", "&"));
                        href = "http://bbs.nju.edu.cn/" + mKey.group(1).replace(";", "&");
                        int start = href.indexOf("?");
                        //top10.setHref(href.substring(start + 1));
                        topAll.setHref(href.substring(start + 1));
                    }
                    resultList.add(topAll);
                }
                //System.out.println(text);

            }
        }
        result.put("topAll", resultList);
        return result;
    };
}


