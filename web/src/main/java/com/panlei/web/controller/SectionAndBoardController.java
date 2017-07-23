package com.panlei.web.controller;

import com.panlei.web.model.Board;
import com.panlei.web.model.Dock;
import com.panlei.web.service.BoardService;
import com.panlei.web.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 361pa on 2017/6/19.
 */
@Controller
@RequestMapping("/bbs")
public class SectionAndBoardController {


//    @Autowired
//    BoardService boardService;
    @Autowired
    BoardService boardService;

    @Autowired
    SectionService sectionService;

    @RequestMapping("/section/hello")
    @ResponseBody
    public String hello() {
        return "hello panleo";
    }

    @RequestMapping(value = "/board/{boardName}",method = RequestMethod.POST)
    @ResponseBody
    public Map writePost(@PathVariable("boardName") String boardName, @RequestBody Map<String, String> postText) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        boardService.postWritePost("testtitle","ddody");

        return result;
    }

//    @RequestMapping(value = "/context", method = RequestMethod.POST)
//    @ResponseBody
//    public Map getBbsContext(@RequestBody Map<String, String> requestUrl) throws Exception {
//        System.out.print(requestUrl.values());
//        return bbsService.getBbsContext(requestUrl.get("url"));
//    }

    @RequestMapping(value = "/section",method = RequestMethod.GET)
    @ResponseBody
    public Map getAllSections() throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("section", sectionService.getAllSections());
        return result;
    }

    @RequestMapping(value = "/section/{sectionValue}/board", method = RequestMethod.GET)
    @ResponseBody
    public Map getBoardsBySectionValue(@PathVariable("sectionValue") String sectionValue) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Board> list = boardService.getBoardsBySectionValue(sectionValue);
        result.put("board", list);
        return result;
    }

    @RequestMapping(value = "/section/board",method = RequestMethod.GET)
    @ResponseBody
    public Map getAllBoards() throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Board> list = boardService.getAllBoards();
        result.put("BoardList", list);
        return result;
    }

    @RequestMapping(value = "/section/board/{boardName}",method = RequestMethod.GET)
    @ResponseBody
    public Map getBoardsByBoardName(@PathVariable("boardName") String boardName) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Dock> list = boardService.getBoardsByBoardName(boardName);
        result.put("DockList", list);
        return result;
    }

    @RequestMapping(value = "/section/board/{boardName}/{numberFrom}",method = RequestMethod.GET)
    @ResponseBody
    public Map getBoardsByBoardName(@PathVariable("boardName") String boardName, @PathVariable("numberFrom") String numberFrom) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        //List<Dock> list = boardService.getBoardsByBoardName(boardName);
        List<Dock> list = boardService.getBoardsByBoardName(boardName, numberFrom);
        result.put("DockList", list);
        return result;
    }


}
