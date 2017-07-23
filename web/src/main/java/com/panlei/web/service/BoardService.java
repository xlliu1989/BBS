package com.panlei.web.service;


import com.panlei.web.model.Board;
import com.panlei.web.model.Dock;

import java.util.List;

/**
 * Created by 361pa on 2017/6/19.
 */
public interface BoardService {
    List<Board> getBoardsBySectionValue(String value) throws Exception;
    List<Board> getAllBoards() throws Exception;
    List<Dock> getBoardsByBoardName(String boardName);
    List<Dock> getBoardsByBoardName(String boardName, String numberFrom);
    void postWritePost(String boardName, String title, String text, String cookie);
}
