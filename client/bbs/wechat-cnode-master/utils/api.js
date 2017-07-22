'use strict';
// api 路径
var HOST = 'https://78560817.bbsnju.cn/ssm/bbs/';
// get /section 讨论区'https://78560817.bbsnju.cn/ssm/bbs/section/board/'
var section = HOST + '/section/board/';
//get /context/ 帖子详情https://78560817.bbsnju.cn/ssm/bbs/context
var context = HOST + '/context';
// post /accesstoken 验证 accessToken 的正确性
var accesstoken = HOST + '/accesstoken';
// post /topic_collect/collect 收藏主题
var collect = HOST + '/topic_collect/collect';
// post /topic_collect/de_collect 取消主题
var de_collect = HOST + '/topic_collect/de_collect';
// post /reply/:reply_id/ups 为评论点赞
function reply (id) {
  return HOST + "/reply/"+ id +"/ups"
}

// get请求方法
function fetchGet(url, callback) {
  // return callback(null, top250)
  wx.request({
    url: url,
    header: { 'Content-Type': 'application/json' },
    success (res) {
      console.log(res);
      callback(null, res.data)
    },
    fail (e) {
      console.error(e)
      callback(e)
    }
  })
}

// post请求方法
function fetchPost(url, data, callback) {
  wx.request({
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    url: url,

    data: {
      url: data,
      y: ''
    },
    success (res) {
      console.log(res);
      callback(null, res.data)
    },
    fail (e) {
      console.error(e)
      callback(e)
    }
  })
}

function fetchUserPost(url, data, callback) {
  wx.request({
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    url: url,

    data: {
      "userName": data.userName,
      "passwd": data.passwd,
      "webchatID": data.webchatID
    },
    success(res) {
      console.log(res);
      callback(null, res.data)
    },
    fail(e) {
      console.error(e)
      callback(e)
    }
  })
}

module.exports = {
  // API
  section: section,
  context: context,
  accesstoken: accesstoken,
  collect: collect,
  de_collect: de_collect,
  reply: reply,
  Host: HOST,

  // METHOD
  fetchGet: fetchGet,
  fetchPost: fetchPost,
fetchUserPost: fetchUserPost

}
