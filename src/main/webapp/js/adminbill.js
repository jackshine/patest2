$(".applyMana").next(".treeview-menu").toggle("slow");
$(".applyMana").addClass("leftActive");
$(".billset").css("color", "white");

var program = {
    page: '1',
    data: [],
    billhtml: '',
    count: '',
    sysusrid: '',
    perbill: '',
    allbill: '',
    examname: [],
    showallbill: function () {
        var order = 1;
        var billlist = program.data;
        var epApplys = program.epApplyInfoList;
        var epUsers = program.epUserInfoList;
        program.billhtml = "";
        for (var i = 0; i < billlist.length; i++) {
            var payinfo;
            if (billlist[i].isPay === 0) {
                payinfo = "未支付";
            } else if (billlist[i].isPay === 1) {
                payinfo = "已支付";
            }
            var orderId = billlist[i].orderId;
            program.billhtml += '<tr  class="' + orderId + '">'
                + '<td><input type="checkbox" value="' + orderId + '" name="title"/></td>'
                + '<td>' + order + '</td>'
                + '<td>' + billlist[i].orderNum + '</td>'
                + '<td><a class="title"  href="../eph/applyexam.html?applyid=' + epApplys[i].applyId + '">' + epApplys[i].examName + '</a></td>'
                + '<td>' + epUsers[i].realName + '</td>'
                + '<td>' + epUsers[i].phone + '</td>'
                + '<td>' + billlist[i].createTime + '</td>'
                + '<td>' + billlist[i].money + '</td>'
                + '<td>' + payinfo + '</td>'
                + '</tr>';
            order++;
        }
    },
    //查询账单信息
    selectallbill: function () {
        $.ajax({
            type: "get",
            content: "application/x-www-form-urlencoded;charset=UTF-8",
            url: "../epOrderInfo/selectByCondition",
            dataType: 'json',
            async: false,
            data: {
                page: program.page,
                rows: pubMeth.rowsnum
            },
            success: function (result) {
                console.log(result);
                program.data = result.data.list;
                program.epApplyInfoList = result.data.epApplyInfoList;
                program.epUserInfoList = result.data.epUserInfoList;
                program.count = result.data.total;
                program.showallbill();
                $("#billInfo").empty();
                $("#billInfo").append(program.billhtml);
            }
        });
    },
    //删除账单信息
    deletebill: function () {
        $('.delete').on('click', function (e) {
            var valArr = new Array;
            $(":checkbox[name='title']:checked").each(function (i) {
                valArr[i] = $(this).val();
            });
            var vals = valArr.join(',');// 转换为逗号隔开的字符串
            console.log(vals);
            if (vals !== "") {
                $("#modalexamdelete").modal(function () {
                    backdrop : 'static'
                });
                $(".examquess").html(vals);
                $(".examdelete").click(function () {
                    $.ajax({
                        type: "post",
                        content: "application/x-www-form-urlencoded;charset=UTF-8",
                        url: "../epOrderInfo/deleteByIds",
                        dataType: 'json',
                        async: false,
                        data: {
                            ids: vals
                        },
                        success: function (result) {
                            console.log(result);
                            if (result.status > 0) {
                                pubMeth.alertInfo("alert-success", "删除成功！");
                                program.selectallbill();
                                $("#modalexamdelete").modal('hide');
                            } else {
                                pubMeth.alertInfo("alert-danger", "删除失败！");
                            }
                        }
                    });
                });
            } else {
                pubMeth.alertInfo("alert-info", "请先勾选删除项！");
            }
        });
    }
};

pubMeth.getRowsnum("rowsnum");
program.selectallbill();
program.deletebill();
if (program.count > 0) {
    $(".countnum").html(program.count);
    $(".perbill").html(program.perbill);
    $(".allbill").html(program.allbill);
    $.jqPaginator('#pagination', {
        totalCounts: program.count,
        visiblePages: 5,
        currentPage: 1,
        pageSize: parseInt(pubMeth.rowsnum),
        first: '<li class="first"><a href="javascript:;">首页</a></li>',
        last: '<li class="last"><a href="javascript:;">尾页</a></li>',
        page: '<li class="page"><a href="javascript:;">{{page}}</a></li>',
        onPageChange: function (num, type) {
            if (type == 'init') {
                return;
            }
            program.page = num;
            program.selectallbill();
        }
    });
} else {
    $(".pagenum").css("display", "none");
}