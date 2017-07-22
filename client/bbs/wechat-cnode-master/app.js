//app.js
App({
  onLaunch: function () {
    //调用API从本地缓存中获取数据
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)
  },
  getUserInfo:function(cb){
    var that = this
    if(this.globalData.userInfo){
      typeof cb == "function" && cb(this.globalData.userInfo)
    }else{
      //调用登录接口
      wx.login({
        success: function (res) {
          console.log(res);
          wx.getUserInfo({
            success: function (res) {
              that.globalData.userInfo = res.userInfo
              typeof cb == "function" && cb(that.globalData.userInfo)
            }
          })

          var d = that.globalData;//这里存储了appid、secret、token串  
          var l = 'https://api.weixin.qq.com/sns/jscode2session?appid=' + d.appid + '&secret=' + d.secret + '&js_code=' + res.code + '&grant_type=authorization_code';
          console.log(l);
          wx.request({
            url: l,
            data: {},
            method: 'GET', // OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT  
            // header: {}, // 设置请求的 header  
            header: {
              'content-type': 'application/json'
            },
            success: function (res) {
              console.log(res);
              var obj = {};
              obj.openid = res.data.openid;
              obj.expires_in = Date.now() + res.data.expires_in;
              //console.log(obj);
              wx.setStorageSync('user', obj);//存储openid  
            }
          });
        }
      });
      wx.checkSession({
        success: function () {
          //登录态未过期
        },
        fail: function () {
          //登录态过期
          wx.login()
        }
      });
    }
  },
  globalData:{
    userInfo:null,
    appid: "wxa7ff67769340f01d",
    secret: "f1f8f6a1250d935f0ba24b51a4f0c507",
    host: 'https://78560817.bbsnju.cn/ssm/bbs/'
  }
})