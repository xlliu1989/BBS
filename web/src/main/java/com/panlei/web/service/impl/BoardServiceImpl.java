package com.panlei.web.service.impl;

import com.panlei.web.dao.BoardMapper;
import com.panlei.web.model.Board;
import com.panlei.web.model.Dock;
import com.panlei.web.service.BoardService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 361pa on 2017/6/19.
 */
@Service("boardService")
public class BoardServiceImpl implements BoardService {

    private BoardMapper boardMapper;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public void setBoardMapper(BoardMapper boardMapper){
        this.boardMapper = boardMapper;
    }

    public List<Board> getBoardsBySectionValue(String value) throws Exception{
        return boardMapper.getBoardsBySectionValue(value);
    }

    public List<Board> getAllBoards() throws Exception{
        return boardMapper.getAllBoards();
    }

    public List<Dock> getBoardsByBoardName(String boardName){

        String urlBoard = "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName;
        Document boardDoc = null;
        try {
            boardDoc = Jsoup.connect(urlBoard).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("&&&&&" + e.getMessage());
        }
        Elements trs = boardDoc.select("table").select("tr");
        List resultList = new ArrayList<Dock>();
        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            if (i == 0) {
                continue;
            }

            Dock dock = new Dock();
            for (int j = 0; j < tds.size(); j++) {
                String text = tds.get(j).text();
                System.out.print(j+">"+text);
                switch (j) {
                    case 0:
                        if (text.length()>0){
                            dock.setNumber(text);
                        }
                        break;
                    case 1:
                        if (text.length()>0){
                            dock.setTop(true);
                        }else {
                            dock.setTop(false);
                        }
                        break;
                    case 2:
                        dock.setAuthor(text);
                        break;
                    case 3:
                        dock.setCreateTime(text);
                        break;
                    case 4:
                        String href = null;
                        String pattern = "href=\"([^\"]*)\"";
                        Pattern pKey = Pattern.compile(pattern, 2 | Pattern.DOTALL);
                        Matcher mKey = pKey.matcher(tds.get(j).toString());
                        if (mKey.find()) {
                            //System.out.print(mKey.group(1).replace(";", "&"));
                            href = "http://bbs.nju.edu.cn/" + mKey.group(1).replace(";", "&");
                            int start = href.indexOf("?");
                            dock.setUrl(href.substring(start + 1));

                        }
                        //System.out.print(dock.getUrl());
                        dock.setTitle(text);
                        break;
                    case 5:
                        if (text.indexOf("/")>0){
                            dock.setReplyNumber(text.split("/")[0]);
                            dock.setWatchNumber(text.split("/")[1]);
                        }else {
                            dock.setWatchNumber(text);
                        }
                        //dock.setReplyNumber(text);
                        break;
                }
                //System.out.println(text);
            }
            resultList.add(dock);

        }

        System.out.print(resultList);
        return resultList;
    }

    public List<Dock> getBoardsByBoardName(String boardName, String numberFrom){
        String urlBoard;
        if (numberFrom.equals("latest")){
            urlBoard = "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName;
        }else {
            urlBoard = "http://bbs.nju.edu.cn/bbstdoc?board=" + boardName + "&start=" + numberFrom;
        }

        Document boardDoc = null;
        try {
            boardDoc = Jsoup.connect(urlBoard).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("&&&&&" + e.getMessage());
        }
        Elements trs = boardDoc.select("table").select("tr");
        List resultList = new ArrayList<Dock>();
        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            if (i == 0) {
                continue;
            }

            Dock dock = new Dock();
            for (int j = 0; j < tds.size(); j++) {
                String text = tds.get(j).text();
                System.out.print(j+">"+text);
                switch (j) {
                    case 0:
                        if (text.length()>0){
                            dock.setNumber(text);
                        }
                        break;
                    case 1:
                        if (text.length()>0){
                            dock.setTop(true);
                        }else {
                            dock.setTop(false);
                        }
                        break;
                    case 2:
                        dock.setAuthor(text);
                        break;
                    case 3:
                        dock.setCreateTime(text);
                        break;
                    case 4:
                        String href = null;
                        String pattern = "href=\"([^\"]*)\"";
                        Pattern pKey = Pattern.compile(pattern, 2 | Pattern.DOTALL);
                        Matcher mKey = pKey.matcher(tds.get(j).toString());
                        if (mKey.find()) {
                            //System.out.print(mKey.group(1).replace(";", "&"));
                            href = "http://bbs.nju.edu.cn/" + mKey.group(1).replace(";", "&");
                            int start = href.indexOf("?");
                            dock.setUrl(href.substring(start + 1));

                        }
                        //System.out.print(dock.getUrl());
                        dock.setTitle(text);
                        break;
                    case 5:
                        if (text.indexOf("/")>0){
                            dock.setReplyNumber(text.split("/")[0]);
                            dock.setWatchNumber(text.split("/")[1]);
                        }else {
                            dock.setWatchNumber(text);
                        }
                        //dock.setReplyNumber(text);
                        break;
                }
                //System.out.println(text);
            }
            resultList.add(dock);

        }

        System.out.print(resultList);
        return resultList;
    }

    public void postWritePost(String boardName, String title, String text, String sendCookie){
        try {
            Connection con = Jsoup.connect("http://bbs.nju.edu.cn/vd36389/bbssnd?board=" + boardName);
            con.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0");
            con.header("Host", "bbs.nju.edu.cn");
            con.header("Connection", "keep-alive");
            con.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            con.header("Accept-Encoding", "gzip,deflate,sdch");
            con.header("Accept-Language", "zh-CN,zh;q=0.8");
            con.header("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
            con.header("Cookie", sendCookie);
            con.data("title", title);
            con.data("pid", "0");
            con.data("reid", "0");
            con.data("signature", "1");
            con.data("autocr", "on");
            con.data("text", text);

            con.timeout(5000);
            Document doc = con.post(); //将获取到的内容打印出来
            String CookieString = doc.toString();
            System.out.println(CookieString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
