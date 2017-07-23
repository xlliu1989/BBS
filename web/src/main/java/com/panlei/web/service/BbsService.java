package com.panlei.web.service;

import java.util.Map;

/**
 * Created by 361pa on 2017/6/20.
 */
public interface BbsService {
    Map getTop10();
    Map getBbsContext(String url) throws Exception;
    Map getTopAll();
    Map getUserInfo(String userId);
}
