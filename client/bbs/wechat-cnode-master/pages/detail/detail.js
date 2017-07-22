// posts.js
var Api = require('../../utils/api.js');
var util = require('../../utils/util.js');
var WxParse = require('../../wxParse/wxParse.js');
var bbsurl = "http://bbs.nju.edu.cn/bbstcon?";
Page({
  data: {
    title: '话题详情',
    collectText: "收藏",
    detail: [],
    replyArr: [],
    hidden: true,
    modalHidden: true,
    article: '',
    scrollTop: 0
  },
  goTop: function (e) {
    this.setData({
      scrollTop: 0,
      floorstatus: false
    })
  },
  scroll: function (e, res) {
    // 容器滚动时将此时的滚动距离赋值给 this.data.scrollTop
    if (e.detail.scrollTop > 500) {
      this.setData({
        floorstatus: true
      });
    } else {
      this.setData({
        floorstatus: false
      });
    }
  },

  onLoad: function () {
    var that = this;
    var appInstance = getApp();
    that.fetchData(appInstance.href);

    //  console.log(detail)

    //var article = '<div>我是HTML代码</div>';
    /**
    * WxParse.wxParse(bindName , type, data, target,imagePadding)
    * 1.bindName绑定的数据名(必填)
    * 2.type可以为html或者md(必填)
    * 3.data为传入的具体数据(必填)
    * 4.target为Page对象,一般为this(必填)
    * 5.imagePadding为当图片自适应是左右的单一padding(默认为0,可选)
    */
    // WxParse.wxParse('detail[0]', 'html', detail[0].thisContext, that);
    //  for (let i = 0; i < detail.length; i++) {
    //    WxParse.wxParse('detail', 'html', detail[0].thisContext, that);
    //    }
    // WxParse.wxParse('detail', 'html', detail, that, 5);
  },

  scrolltolower: function () {
    this.setData({
      floorstatus: true
    })
    this.lower();
    console.log('上拉刷新', new Date());
  },
  // 滑动底部加载
  lower: function () {
    console.log('滑动底部加载', new Date());
    var that = this;
    if ((that.data.detail.length - 1) % 30 == 0) {
      var appInstance = getApp();
      var url = appInstance.href + "&start=" + (that.data.detail.length - 1);
      that.fetchData(url);
    }
  },

  // 获取数据
  fetchData: function (href) {
    var that = this;
    var ApiUrl = Api.context;
    // var ApiUrl = 'https://78560817.bbsnju.cn/ssm/bbs/context';
    var href = "http://bbs.nju.edu.cn/bbstcon?" + href;
    //   var href = bbsurl + "board=" + options.board + "&amp=" + options.amp + "&file=" + options.file; 
    //  var href = "http://bbs.nju.edu.cn/bbstcon?board=Pictures&amp&file=M.1495992445.A";
    that.setData({
      hidden: false
    });
    Api.fetchPost(ApiUrl, href, (err, res) => {
      //   if (res.success) {
      // var detail = that.data.detail;
      // var replies = detail.replies[index];

      // if (res.action === "up") {
      //   replies.zanNum = replies.zanNum + 1;
      // } else {
      //   replies.zanNum = replies.zanNum - 1;
      // }
      var detailLength = that.data.detail.length;
      if (detailLength != 0){
        var appendContext = res.context.shift();
      }
      that.setData({
        detail: that.data.detail.concat(res.context),
        // article_content: WxParse.wxParse('article_content', 'html', res.data[0].thisContext, that, 5)
      })

      console.log(that.data.detail);
      if (detailLength == 0){
        var article = that.data.detail[0].thisContext;

        var r1 = new RegExp('\n', "g");
        article = article.replace(r1, '<br />');

        WxParse.wxParse('article', 'html', article, that, 5);
        console.log(article);
      }
      

      var repliesArray = that.data.detail;
      var l = 100;
      if (repliesArray.length < l) {
        l = repliesArray.length;
      }
    
      for (var i = detailLength; i < l; i++) {
        if (repliesArray[i].thisContext) {
          var c = repliesArray[i].thisContext;
          var r2 = new RegExp('\n', "g");
          c = c.replace(r2, '<br />');
          if (c.length > 0) {
            that.data.replyArr.push(c);
          }
        }
      }
      /**
      * WxParse.wxParseTemArray(temArrayName,bindNameReg,total,that)
      * 1.temArrayName: 为你调用时的数组名称
      * 3.bindNameReg为循环的共同体 如绑定为reply1，reply2...则bindNameReg = 'reply'
      * 3.total为reply的个数
      */
      var replyArrLength = that.data.replyArr.length;
      console.log('replies:' + replyArrLength);
      if (replyArrLength > 0) {
        for (let i = detailLength; i < replyArrLength; i++) {
          WxParse.wxParse('reply' + i, 'html', that.data.replyArr[i], that);
          if (i === replyArrLength - 1) {
            WxParse.wxParseTemArray("replyTemArray", 'reply', replyArrLength, that)
          }
        }
      }

      setTimeout(function () {
        that.setData({ hidden: true });
      }, 50);

      //     }

    })



  },

  // 收藏文章
  collect: function (e) {
    var that = this;
    var ApiUrl = Api.collect;
    var accesstoken = wx.getStorageSync('CuserInfo').accesstoken;
    var id = e.currentTarget.id;
    if (!id) return;
    if (!accesstoken) {
      that.setData({ modalHidden: false });
      return;
    }

    Api.fetchPost(ApiUrl, { accesstoken: accesstoken, topic_id: id }, (err, res) => {
      if (res.success) {
        var detail = that.data.detail;
        detail.is_collect = true;
        that.setData({
          collectText: "取消收藏",
          detail: detail
        });
      }
    })
  },

  // 点赞
  reply: function (e) {
    console.log(e);
    var that = this;
    var accesstoken = wx.getStorageSync('CuserInfo').accesstoken;
    var id = e.currentTarget.id;
    var index = e.currentTarget.dataset.index;
    var ApiUrl = Api.reply(id);
    if (!id) return;
    if (!accesstoken) {
      that.setData({ modalHidden: false });
      return;
    }

    Api.fetchPost(ApiUrl, { accesstoken: accesstoken }, (err, res) => {
      if (res.success) {
        var detail = that.data.detail;
        var replies = detail.replies[index];

        if (res.action === "up") {
          replies.zanNum = replies.zanNum + 1;
        } else {
          replies.zanNum = replies.zanNum - 1;
        }

        that.setData({ detail: detail });

      }
    })

  },

  // 关闭--模态弹窗
  cancelChange: function () {
    this.setData({ modalHidden: true });
  },
  // 确认--模态弹窗
  confirmChange: function () {
    this.setData({ modalHidden: true });
    wx.navigateTo({
      url: '/pages/login/login'
    });
  }

})
