var Api = require('../../utils/api.js');
var util = require('../../utils/util.js');

Page({
  data: {
    loading: false,
    userName: "",
    password: "",
    error: ""
  },

  onLoad: function() {

  },

  bindKeyInput: function(e) {
    this.setData({
      userName: e.detail.value
    })
  },
  bindPassKeyInput: function(e){
    this.setData({
      password: e.detail.value
    })
  },

  // 验证token(登录)
  isLogin: function() {
    var that = this;
    //var accesstoken = that.data.accesstoken;
    var ApiUrl = "https://78560817.bbsnju.cn/ssm/bbs/user/bind";
    var wechat = wx.getStorageSync('user');

    //if (userName == "" && password == "" && wechat.openid == "") return;

    var CuserInfo = {
      "userName": that.data.userName,
      "passwd": that.data.password,
      "webchatID": wechat.openid
    };

    that.setData({ loading: true });

    Api.fetchUserPost(ApiUrl, CuserInfo, (err, res) => {

      if(res.success){
        // var CuserInfo = {
        //   accesstoken: accesstoken,
        //   id: res.id,
        //   loginname: res.loginname,
        //   avatar_url: res.loginname
        // };
        // console.log(CuserInfo)
        // wx.setStorageSync('CuserInfo', CuserInfo);

        setTimeout(function(){
          that.setData({ loading: false });
          // wx.navigateTo({
          //   url: '/pages/index/index'
          // })
          wx.navigateBack();
        },3000);

      }else{
        that.setData({ error: res.error_msg });
        that.setData({ loading: false });
        setTimeout(function(){
          that.setData({ error: "" });
        },2000);
      }

    })


  }
})
