var Api = require('../../utils/api.js');
var util = require('../../utils/util.js');

var navList = [
  { id: "top10", title: "十大" },
  { id: "topall", title: "精华" },
  { id: "more", title: "关注" },
];

Page({
  data: {
    activeIndex: '0',
    navList: navList,
    title: '话题列表',
    postsList: [],
    topallList:[],
    hidden: false,
    page: 0,
    limit: 20,
    tab: 'top10',
    scrollTop: 0,
    hotList: [{ id: "Pictures", content: "贴图版" }, { id: "FleaMarket", content: "跳蚤市场" }, { id: "AcademicReport", content: "学术讲座" }, { id: "V_Suggestions", content: "校长信箱" }, { id: "WarAndPeace", content: "百年好合" }, { id: "JobExpress", content: "就业特快" }, { id: "AutoSpeed", content: "车迷世界" }, { id: "JobExpress", content: "股市风云" }, { id: "Girls", content: "女生天地" }, { id: "NJUExpress", content: "南大校园生活" }, { id: "JobAndWork", content: "创业与求职" }, { id: "WorldFootball", content: "世界足球" }, { id: "Basketball", content: "篮球" }],
  },

  onLoad: function () {
    this.getData();
  },
  goTop: function (e) {
    this.setData({
      scrollTop: 0
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

  onPullDownRefresh: function () {
    this.getData();
    console.log('下拉刷新', new Date());
  },

  listClick: function (e) {
    wx.navigateTo({
      url: '/pages/detail/detail',
    })
    var appInstance = getApp();
    appInstance.href = e.currentTarget.dataset.item.href;
  },
  onReachBottom: function () {
    this.lower();
    console.log('上拉刷新', new Date());
  },

  excuteHotSearch: function (e) {
    var sub = e.currentTarget.dataset.sub;
    var content = this.data.hotList[sub].id;
    wx.navigateTo({
      url: '../zone/zone?board=' + content
    })
  },
  // listClick:function(e){
  //   let str = JSON.stringify(e.currentTarget.dataset.item);
  //   wx.navigateTo({
  //     url: '../pages/detail/detail?jsonStr=' + str,
  //     success: function (res) {
  //       // success
  //     },
  //     fail: function () {
  //       // fail
  //     },
  //     complete: function () {
  //       // complete
  //     }
  //   })
  // },
  // 点击获取对应分类的数据
  onTapTag: function (e) {
    var that = this;
    var tab = e.currentTarget.id;
    var index = e.currentTarget.dataset.index;
    that.setData({
      activeIndex: index,
      tab: tab,
      page: index
    });
    if (index == 2){
    }
    else{
      if (that.data.postsList.length == 0 || that.data.topallList.length == 0)
        that.getData();
    }


    // if (tab !== 'all') {
    //   that.getData({tab: tab});
    // } else {
    //   that.getData();
    // }
  },

  //获取文章列表数据
  getData: function () {
    var that = this;
    var tab = that.data.tab;
    var page = that.data.page;
    var limit = that.data.limit;
    //  var ApiUrl = Api.topics +'?tab='+ tab +'&page='+ page +'&limit='+ limit;
    var ApiUrl = Api.Host + tab;
    that.setData({ hidden: false });

    // if(page == 1) {
    //   that.setData({ postsList: [] });
    // }
    // get请求方法
    // return callback(null, top250)

    Api.fetchGet(ApiUrl, (err, res) => {
      //更新数据
      if (page == 0){
        that.setData({
          postsList: res.top10
        })
      }
      else{
        that.setData({
          postsList: res.topAll
        })
      }

   

      setTimeout(function () {
        that.setData({ hidden: true });
      }, 300);
    }

    )
    // console.log(postsList);
  },

  // 滑动底部加载
  lower: function () {
    // console.log('滑动底部加载', new Date());
    // var that = this;
    // that.setData({
    //   page: that.data.page + 1
    // });
    // if (that.data.tab !== 'all') {
    //   this.getData({ tab: that.data.tab, page: that.data.page });
    // } else {
    //   this.getData({ page: that.data.page });
    // }
  }


})
