// pages/zone/zone.js
var Api = require('../../utils/api.js');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    href: '',
    DockList: [],
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this;
    that.data.href = options.board;
    that.getData(options.board + "/latest")
  },

  onReachBottom: function () {
    this.lower();
    console.log('上拉刷新', new Date());
  },
  // 滑动底部加载
  lower: function () {
    console.log('滑动底部加载', new Date());
    var that = this;
    var startnumber = that.data.DockList[that.data.DockList.length - 1].number - 20;
    that.getData(that.data.href + "/" + startnumber)
  },

  listClick: function (e) {
    wx.navigateTo({
      url: '/pages/detail/detail',
    })
    var appInstance = getApp();
    appInstance.href = e.currentTarget.dataset.item.url;
  },

  //获取文章列表数据
  getData: function (tab) {
    var that = this;
    // var tab = that.data.tab;
    // var page = that.data.page;
    // var limit = that.data.limit;
    //  var ApiUrl = Api.topics +'?tab='+ tab +'&page='+ page +'&limit='+ limit;
    var ApiUrl = Api.section + tab;
    that.setData({ hidden: false });

    // if(page == 1) {
    //   that.setData({ postsList: [] });
    // }
    // get请求方法
    // return callback(null, top250)

    Api.fetchGet(ApiUrl, (err, res) => {
      //更新数据
      that.setData({
        // postsList: that.data.postsList.concat(res.data.map(function (item) {
        //   item.last_reply_at = util.getDateDiff(new Date(item.last_reply_at));
        //   return item;
        // }))
        DockList: that.data.DockList.concat(res.DockList)
      })

      setTimeout(function () {
        that.setData({ hidden: true });
      }, 300);
    }

    )
    // console.log(postsList);
  },
})