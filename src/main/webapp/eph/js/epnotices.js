var epnotices = {
    html: '',
    data: '',
    count: '',
    init: function () {
        patest.getRowsnum("rowsnum");
        loginreg.init();
        epnotices.footcon();
        commonet.selectEpinfo();
        epnotices.selectNotice();

        if (epnotices.count > 0) {
            $.jqPaginator('#pagination', {
                totalCounts: epnotices.count,
                visiblePages: 5,
                currentPage: 1,
                pageSize: parseInt(patest.rowsnum),
                first: '<li class="first"><a href="javascript:;">首页</a></li>',
                last: '<li class="last"><a href="javascript:;">尾页</a></li>',
                page: '<li class="page"><a href="javascript:;">{{page}}</a></li>',
                onPageChange: function (num, type) {
                    if (type == 'init') {
                        return;
                    }
                    epnotices.page = num;
                    epnotices.selectallInfo();
                }
            });
        }
    },
    /**
     * 公告展示
     */
    selectNotice: function () {
        patest.request({
            url: "../ep/epNotice/selectByCondition"
        }, {
            page: epnotices.page,
            row: patest.rowsnum
        }, function (result) {
            epnotices.data = result.data.list;
            epnotices.count = result.data.total;
            epnotices.showNotice();
            $("#applyInfo").empty();
            $("#applyInfo").append(epnotices.html);
        });
    },
    showNotice: function () {
        epnotices.html = "";
        for (var i = 0; i < epnotices.data.length; i++) {
            epnotices.html += '<tr><td>' + epnotices.data[i].epnoId + '</td>'
                + '<td><a href="epnoticetext.html?epid=' + epnotices.data[i].epnoId + '">' + epnotices.data[i].title + '</a></td>'
                + '<td>' + epnotices.data[i].createTime + '</td>'
                + '</tr>';
        }
    },
    footcon: function () {
        var foothtml = '<div class="row"><div class="col-md-4 col-md-offset-1">' +
            '<p style="font-size:20px;font-weight:bold;"><span class="glyphicon glyphicon-asterisk"></span>团队介绍</p>' +
            '<p class="teaminfo"></p>' +
            '</div>' +
            '<div class="col-md-5 col-md-offset-1">' +
            '<div class="linkt"><span class="glyphicon glyphicon-envelope"></span> 关于我们</div>' +
            '<ul class="contact">' +
            '<li><span class="glyphicon glyphicon-phone"></span> <span>联系人:</span>  <span class="principal"></span>  <span class="phone"></span></li>' +
            '<li><span class="glyphicon glyphicon-envelope"></span> <span>E-Mail：</span> <span class="email"></span></li>' +
            '<li><span class="glyphicon glyphicon-map-marker"></span> <span>实验室地址：</span> <span class="taddress"> </span></li>' +
            '<li><span class="glyphicon glyphicon-home"></span> <span>地址：</span> <span class="address"></span></li>' +
            '</ul></div></div>' +
            '<div class="row copyright">' +
            '©Copyright 2016 - 2017<a href="http://www.cs.swust.edu.cn/academic/lab-kownledge.html">西南科技大学数据与知识工程实验室</a>版权所有' +
            '</div>';
        $("#footer").append(foothtml);
    }
};

