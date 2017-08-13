package com.panlei.web.service.impl;


import com.panlei.web.dao.BoardMapper;
import com.panlei.web.dao.UpvoteMapper;
import com.panlei.web.dao.UserNjuMapper;
import com.panlei.web.model.*;
import com.panlei.web.service.BbsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    @Autowired
    private UserNjuMapper userNjuMapper;

    @Autowired
    private UpvoteMapper upvoteMapper;

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
            top10.setUpvoteNumber(String.valueOf(upvoteMapper.selectUpvoteByTopicID(top10.getHref()).size()));
            resultList.add(top10);

        }
        result.put("top10", resultList);
        return result;
    }

    public Map getBbsContext(String url, String wetbchatID) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Upvote> upvoteReturn = upvoteMapper.selectUpvoteByTopicID(url);
        url = "http://bbs.nju.edu.cn/bbstcon?" + url;
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
                    bbsContext.setThisAuthor(text.substring(start + 2, end));
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
                    while (context.indexOf("http://bbs.nju.edu.cn/file/") > -1){

                        int imageStart = context.indexOf("http://bbs.nju.edu.cn/file/");
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
                    //context= context.substring(2);
                    context = handleContextNewline(context);
//                        context = context.replace("\r\n\r\n", "\n");
//                        context = context.replace("\r\n", "");
//                        context = context.replace("\n", "\r\n\r\n");

                    bbsContext.setThisContext("<p>"+context+"</p>");

                    int startTitle = text.indexOf("标  题:");
                    int endTitle = text.indexOf("\n", startTitle);
                    bbsContext.setThisTitle(text.substring(startTitle + 5, endTitle));
                }
            }
            if (!upvoteReturn.isEmpty()){
                Map<String, String> thisFloorUpvote = new HashMap<String, String>();
                thisFloorUpvote = handleContextUpvoteByFloorID(upvoteReturn, bbsContext.getThisFloor(), wetbchatID);
                bbsContext.setThisFloorUpvoteNumber(thisFloorUpvote.get("number"));
                bbsContext.setThisFloorUpvoteIsMe(thisFloorUpvote.get("thisFloorUpvoteIsMe"));
            }else {
                bbsContext.setThisFloorUpvoteNumber("0");
            }
            resultList.add(bbsContext);
        }
        result.put("context", resultList);
        return result;
    }

//    public String handleHref(String context){
//        int indexStart = 0;
//        while (context.indexOf("http://", indexStart) > -1 || context.indexOf("https://", indexStart) > -1){
//            if (context.indexOf("http://bbs.nju.edu.cn/file/", indexStart) >-1){
//                indexStart = context.indexOf("http://bbs.nju.edu.cn/file/")+4;
//                continue;
//            }
//
//            int start = context.indexOf("http://", indexStart);
//            int end = context.indexOf("\n", start);
//            String href = context.substring(start, end);
//            context.replace()
//
//
//        }
//    }

    public Map<String, String> handleContextUpvoteByFloorID(List<Upvote> listUpvote, String floorID, String webcahtID){
        Map<String, String> result = new HashMap<String, String>();
        Integer number = 0;
        String isMe = "0";
        for(int i = 0; i < listUpvote.size(); i++){
            Upvote upvote = listUpvote.get(i);
            if (floorID.equals(upvote.getFloorID())) {
                if (webcahtID.equals(upvote.getWebchatID())){
                    isMe = "1";
                }
                number++;
            }

        }
        result.put("number", number.toString());
        result.put("thisFloorUpvoteIsMe", isMe);
        return result;
    }

    public String handleContextNewline(String context){
        String N = "\n";
        //String RN = "\r\n";
        int subInt ;
        String nextString = null;
      //

            nextString = N;
            subInt = 1;

        int indexStart = 0;
        int indexRN = context.indexOf(nextString, indexStart);
        boolean nextFlag = true;

        while (nextFlag){
            if (indexRN - indexStart < 36){
                indexStart = indexRN + subInt;
                indexRN = context.indexOf(nextString, indexStart);
                if (indexStart == context.length() || indexStart > context.length() || indexRN == -1){
                    //判断是否是结尾
                    nextFlag = false;
                }
                continue;
            }
            indexStart = indexRN + subInt;
            if (indexStart == context.length() || indexStart > context.length() || indexRN == -1){
                //判断是否是结尾
                nextFlag = false;
            }else {
                //不是结尾，去掉\n
                context = context.substring(0, indexRN) + context.substring(indexRN + subInt);
                indexRN = context.indexOf(nextString, indexStart);
            }

        }
        context = context.replace("\r", "");
        return context;
    }
    public Map getUserInfo(String userId){
        Map<String, Object> result = new HashMap<String, Object>();
        Document doc = null;

        try {
            doc = Jsoup.connect("http://bbs.nju.edu.cn/vd26437/bbsqry?userid=" + userId).get();
            //String url = "http://bbs.nju.edu.cn/vd26437/bbsqry?userid=" + userId;
            //doc = Jsoup.parse(new URL(url).openStream(),"GB2312", url);
        }catch (IOException ex){
            ex.printStackTrace();
            System.out.print(ex.getMessage());
        }

        Element userInfoElement = doc.getElementsByTag("textarea").first();
        String text = userInfoElement.text();
        UserInfo userInfo = ConverToModel(text);
        result.put("userInfo", userInfo);

        return result;
    }

    public Map getUserInfoByWxId(String wxId){
        UserNju user = userNjuMapper.selectUserByWebchatId(wxId);
        if(user != null){
            Map map = getUserInfo(user.getUserName());
            map.put("wxId", wxId);
            return map;
        }

        return null;
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

    public Map getTopAllByNumber(String number){
        Map<String, Object> result = new HashMap<String, Object>();
        List resultList = new ArrayList<TopAll>();
        Map<String, List<TopAll>> topAllMap = getTopAll();

        List<TopAll> listTopAll = topAllMap.get("topAll");
        try {
            int numberInt = Integer.parseInt(number);
            int start = numberInt*5 -4;
            int end = numberInt*5;
            if (end > listTopAll.size()){
                end = listTopAll.size();
            }
            for (int i=start-1; i<end; i++){
                TopAll topAll = listTopAll.get(i);
                topAll.setId(i+1);
                Map<String, List<BbsContext>> mapBbsContext = getBbsContext(topAll.getHref(), "0");
                List<BbsContext> BbsContextList = new ArrayList<BbsContext>();
                BbsContextList = mapBbsContext.get("context");
                topAll.setAuthor(BbsContextList.get(0).getThisAuthor());
                topAll.setReplyNumber(String.valueOf(BbsContextList.size()));
                topAll.setUpvoteNumber(String.valueOf(upvoteMapper.selectUpvoteByTopicID(topAll.getHref()).size()));
                resultList.add(topAll);
            }

           // listTopAll.get()
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.put("topAll", resultList);
        return result;
    }
    public Map getTopAll(){
        Map<String, Object> result = new HashMap<String, Object>();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstopall").get();
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.print("&&&&&" + e.getMessage());
        }
        Elements trs = doc.select("table").select("tr");
        List resultList = new ArrayList<TopAll>();
        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            if (i == 0) {
                continue;
            }
            //System.out.println(tds);

            for (int j = 0; j < tds.size(); j++) {

                String text = tds.get(j).text();
                //System.out.println(j + ">"+text);
                if (text.length() > 0){
                    TopAll topAll = new TopAll();
                    int startBoard = text.lastIndexOf("[");
                    topAll.setTitle(text.substring(0, startBoard));
                    topAll.setBoard(text.substring(startBoard));


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
                    //topAll.setUpvoteNumber(String.valueOf(upvoteMapper.selectUpvoteByTopicID(topAll.getHref()).size()));
                    resultList.add(topAll);
                }
                //System.out.println(text);

            }
        }
        result.put("topAll", resultList);
        return result;
    };

    private UserInfo ConverToModel(String textArea){

        if(textArea == null || textArea.length() == 0){
            return new UserInfo();
        }

        UserInfo userInfo = new UserInfo();

        String text = textArea;

        // parse id
        int idEndIndex = text.indexOf("(");
        String id = text.substring(0, idEndIndex - 1);
        userInfo.setId(id);

        // parse name
        text = text.substring(idEndIndex);
        int nameAreaEndIndex = text.indexOf("共上站");
        String nameArea = text.substring(0, nameAreaEndIndex);
        String name = GetFieldFromText(nameArea, "[33m","[37m");
        userInfo.setName(name);

        // parse login count
        text = text.substring(nameAreaEndIndex);
        int loginCountAreaEndIndex = text.indexOf("发表文章");
        String loginCountArea = text.substring(0, loginCountAreaEndIndex);
        String loginCount = GetFieldFromText(loginCountArea, "[32m","[m");
        userInfo.setLoginCount(loginCount);

        // parse publish count
        text = text.substring(loginCountAreaEndIndex);
        int publishCountAreaEndIndex = text.indexOf("篇");
        String publishCountArea = text.substring(0, publishCountAreaEndIndex);
        String publishCount = GetFieldFromText(publishCountArea, "[32m","[m");
        userInfo.setPublishCount(publishCount);

        // parse constellation
        text = text.substring(publishCountAreaEndIndex);
        int constellationAreaEndIndex = text.indexOf("上次在");
        String constellationArea = text.substring(0, constellationAreaEndIndex);
        String constellation = GetFieldFromText(constellationArea, "m","[m");
        userInfo.setConstellation(constellation);

        // parse last login time
        text = text.substring(constellationAreaEndIndex);
        int lastLoginTimeEndIndex = text.indexOf("从");
        String lastLoginTimeArea = text.substring(0, lastLoginTimeEndIndex);
        String lastLoginTime = GetFieldFromText(lastLoginTimeArea, "[32m","[37m");
        userInfo.setLastLoginTime(lastLoginTime);

        // parse last login ip
        text = text.substring(lastLoginTimeEndIndex);
        int lastLoginIpEndIndex = text.indexOf("到本站一游");
        String lastLoginIpArea = text.substring(0, lastLoginIpEndIndex);
        String lastLoginIp = GetFieldFromText(lastLoginIpArea, "[32m","[37m");
        userInfo.setLastLoginIp(lastLoginIp);

        // parse mail
        text = text.substring(lastLoginIpEndIndex);
        int mailEndIndex = text.indexOf("经验值");
        String mailArea = text.substring(0, mailEndIndex);
        String mail = GetFieldFromText(mailArea, "[32m","[37m");
        userInfo.setMail(mail);

        // parse experience value, experience title
        text = text.substring(mailEndIndex);
        int experienceAreaEndIndex = text.indexOf("表现值");
        String experienceArea = text.substring(0, experienceAreaEndIndex);
        String experienceValue = GetFieldFromText(experienceArea, "[32m","[37m");
        String experienceTitle = GetFieldFromText(experienceArea, "[33m","[37m");
        userInfo.setExperienceValue(experienceValue);
        userInfo.setExperienceTitle(experienceTitle);

        // parse behaviour value, behaviour title
        text = text.substring(experienceAreaEndIndex);
        int behaviourAreaEndIndex = text.indexOf("生命力");
        String behaviourArea = text.substring(0, behaviourAreaEndIndex);
        String behaviourValue = GetFieldFromText(behaviourArea, "[32m","[37m");
        String behaviourTitle = GetFieldFromText(behaviourArea, "[33m","[37m");
        userInfo.setBehaviourValue(behaviourValue);
        userInfo.setBehaviourTitle(behaviourTitle);

        // parse life value
        text = text.substring(behaviourAreaEndIndex);
        int lifeAreaEndIndex = text.indexOf("目前");
        String lifeArea = text.substring(0, lifeAreaEndIndex);
        String lifeValue = GetFieldFromText(lifeArea, "[32m","[37m");
        userInfo.setLifeValue(lifeValue);

        return userInfo;
    }

    private String GetFieldFromText(String text, String beginTag, String endTag){
        int startIndex = text.indexOf(beginTag);

        if(startIndex < 0){
            return "";
        }

        int endIndex = text.substring(startIndex).indexOf(endTag);

        if(endIndex < 0){
            return "";
        }

        return text.substring(startIndex).substring(beginTag.length(), endIndex).trim();
    }

    public Map addUpvote(Upvote upvoteRequest){
        Map<String, Object> result = new HashMap<String, Object>();
        Upvote upvoteReturn = upvoteMapper.selectUpvoteByWebchatIdAndTopicIdAndFloorID(upvoteRequest.getWebchatID(), upvoteRequest.getTopicID(), upvoteRequest.getFloorID());

        if (upvoteReturn == null){
            upvoteMapper.insert(upvoteRequest);
            result.put("Upvote", "1");
            return result;
        }else {
            upvoteMapper.deleteUpvoteByWebchatIdAndTopicIdAndFloorID(upvoteRequest.getWebchatID(), upvoteRequest.getTopicID(), upvoteRequest.getFloorID());
            result.put("Upvote", "0");
            return result;
        }
    }
}


