/**
 * Created by L on 2015/9/8.
 */
$(function(){
    $.DrLazyload();
});


$("body").on("click",function(){
    $(".mdl-menu-list").removeClass("is-visible");
});

$(".mdl-card__menu").on("click",function(e){
    e.stopPropagation();
});

function ismenuClick(){
    var that=$(this).next();
    if(that.hasClass("is-visible")){
        that.removeClass("is-visible");
    }else {
        that.addClass("is-visible");
    }
}
$(".ismenu").on("click",ismenuClick);

function editbtnClick(){
    if(confirm("确定要编辑么？")){
        window.location.href="manager/toedit?mgid="+$(this).attr("id")+"&isedit=1";
    }
}
$(".editbtn").on("click",editbtnClick);

function delbtnClick(){
    if(confirm("确定要删除么？")){
        var delobj=$(this);
        $.post('manager/deleteData',{"id":delobj.attr("id")},function(data) {
            if(!!+data){
                alert("已删除");
                delobj.parents(".singlediv").remove();
            }else{
                alert("删除失败");
            }
        });
    }
}
$(".delbtn").on("click",delbtnClick);

function findthis(){
    var findobj=$(this);
    var id=findobj.attr("id");
    var count=findobj.attr("count");
    var type=findobj.attr("type");
    $.post('manager/findthis',{"id":id,"type":type,"count":count},function(data) {
        if(data.indexOf("err")<0){
            $(findobj).css("font-family","\"Helvetica\",\"Arial\",sans-serif");
            $(findobj).css("font-size","10px");
            $(findobj).css("line-height","16px");
            $(findobj).css("width","100px");
            $(findobj).css("text-align","left")
            $(findobj).text("");
            $(findobj).append(data);
            $(findobj).off("click");
        }else{
            alert("定位失败");
        }
    });
}
$(".findthis").on("click",findthis);

$("#delallinpage").on("click",function(){
    var alogdata='<div class="deldiv"> <ul class="selectdellist">';
    var lilist="";
    $(".contextlist div[delid]").each(function(){
        var delid=$(this).attr("delid");
        var title=$(this).find(".actitle").text();
        lilist=lilist+"<li><span delid='"+delid+"'></span>"+title+"</li>";
    });
    alogdata=alogdata+lilist;
    alogdata=alogdata+'<li class="nohover" style="text-align: center;"><div class="fl" onclick="ckAllLi()"><span id="allselet"></span>全选</div><span class="btn selectedAll" onclick="delselect()">确定</span><span class="btn selectedAll" onclick="delcancel()">取消</span></li>';
    alogdata=alogdata+'</ul></div>';
    initDialog(alogdata);
    $("#thedialogcontext").css("max-height", "500px");
    $("#thedialogcontext").css("overflow-y", "scroll");
    $("#thedialog").css("top","30px");
    $("#thedialog").css("margin","0 15%");
    $("#thedialog").css("width","70%");

    $(".selectdellist li").on("click",toggleDelSelect);
});

function toggleDelSelect(){
    if(!$(this).hasClass("nohover")){
        $(this).find("span").toggleClass("selected");
    }
}

function ckAllLi(){
    var bool=$("#allselet").hasClass("selectedAll");
    $(".selectdellist li").each(function(){
        if(!$(this).hasClass("nohover")){
            if(bool){
                $(this).find("span").removeClass("selected");
                $("#allselet").removeClass("selectedAll");
            }else{
                $(this).find("span").addClass("selected");
                $("#allselet").addClass("selectedAll");
            }
        }
    })
}

function delcancel(){
    $(".selectdellist li").off("click");
    delDialog();
}

function delselect(){
    if($(".selectdellist li .selected").length>0){
        if(confirm("确定要删除么？")){
            var idlist=new Array();
            $(".selectdellist li .selected").each(function(){
                idlist.push($(this).attr("delid"));
            });
            var idstr=idlist.join("--");
            delcancel();
            $.post('manager/deletePageData',{"idstr":idstr},function(data) {
                //alert(data)
                if(+data>0){
                    $(idlist).each(function(){
                        var id=this;
                        $(".contextlist div[delid='"+id+"']").remove();
                    })
                    alert("已删除");
                    $(".pagediv").find("a")[0].click();
                }else{
                    alert("删除失败");
                }
            });
        }
    }else{
        delcancel();
    }
}

function likesearchClick(){
    if($(this).hasClass("likeactive")){
        $(this).removeClass("likeactive")
    }else{
        $(this).addClass("likeactive")
    }
}
$(".likesearch").on("click",likesearchClick);


function searchspanClick(){
    var that=$(this);
    if(that.attr("id")=='all'){
        $(".selectWebsite span").removeClass("active");
        that.addClass("active");
    }else{
        if(that.hasClass("active")){
            if($(".active").length>1){
                that.removeClass("active");
            }
        }else {
            $(".selectWebsite span[id=all]").removeClass("active");
            that.addClass("active");
        }

        var flag=$(".active").length== $(".selectWebsite span").length-1;
        if(flag){
            $(".selectWebsite span").removeClass("active");
            $(".selectWebsite span[id=all]").addClass("active");
        }
    }
}
$(".selectWebsite span").on("click",searchspanClick);

function searchbt(){
    var sv=$("#btsearch").val();
    if(!!!sv){
        alert("不能为空！");
        return;
    }

    var idlist=new Array();
    $(".selectWebsite .active").each(function(index,item){
        idlist.push($(item).attr("id"));
    });
    var ids=idlist.join("--");

    var likeflag=$(".likesearch").hasClass("likeactive");

    var thatol=$(".contextlist");
    $(".loading").off("click");
    $(".loading").css("opacity","1");
    var bgtime=new Date().getTime();

    $.post('manager/pageGetBt',{"searchval":sv,"idtype":ids,"islike":likeflag},function(data) {
        if(!!data){
            $(thatol).empty();

            var edtime=new Date().getTime();
            var timecount=(edtime-bgtime)/1000;
            var times = "<li class='clearnum'>耗时："+timecount+"秒</li>";
            $(thatol).append(times);

            var rejson=eval("("+data+")");
            if(rejson.length<=1){
                var li = "<li class='clearnum'>暂无种子下载</li>";
                $(thatol).append(li);
            }else {
                for (var i = 0; i < rejson.length; i++) {
                    var btname = rejson[i].btname;
                    var btlink = rejson[i].btlink;
                    if (btlink != "###") {
                        if (btlink != "#") {
                            var li = "<li><a target=\"_blank\" href=\"" + btlink + "\">" + btname + "</a></li>";
                        } else {
                            var li = "<li class='title'><a href=\"javascript:void(0)\">" + btname + "</a></li>";
                        }
                    } else {
                        var li = "<li class='clearnum'>" + btname + "</li>";
                    }
                    $(thatol).append(li);
                }
            }
        }else{
            $(thatol).empty();
            var li="<li class='errorli'><div>数据获取异常，请点击重新获取</div></li>";
            $(thatol).append(li);
        }
        $(".loading").on("click",searchbt);
        $(".loading").css("opacity","0");
    });
}
$(".loading").on("click",searchbt);

function isdownloadclick(){
        $(this).children().children().addClass("is-animating");
        setTimeout(function(){
            $(this).children().children().removeClass("is-animating");
        },1000);
        var btliistdiv= $(this).parent().next();
        if($(btliistdiv).find(".errorli").length>0){
            getbt(this,btliistdiv);
        }else {
            btliistdiv.fadeToggle();
        }
        if($(btliistdiv).find("li").length==0){
            getbt(this,btliistdiv);
        }
}
$(".isdownload").on("click",isdownloadclick);


function regetbt(){
    if(confirm("确定重新获取bt？")) {
        $("body").click();
        var btliistdiv = $(this).parents(".singlediv").find("#isbtlist");
        btliistdiv.fadeIn();
        var thisobj=$(this).parents(".singlediv").find(".isdownload");
        getbt(thisobj, btliistdiv);
    }
}
$(".regetbtbtn").on("click",regetbt);

function getloading(){
    var loading='<div class="mdl-spinner mdl-spinner--single-color mdl-js-spinner is-active is-upgraded" data-upgraded=",MaterialSpinner"><div class="mdl-spinner__layer mdl-spinner__layer-1"><div class="mdl-spinner__circle-clipper mdl-spinner__left"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__gap-patch"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__circle-clipper mdl-spinner__right"><div class="mdl-spinner__circle"></div></div></div><div class="mdl-spinner__layer mdl-spinner__layer-2"><div class="mdl-spinner__circle-clipper mdl-spinner__left"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__gap-patch"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__circle-clipper mdl-spinner__right"><div class="mdl-spinner__circle"></div></div></div><div class="mdl-spinner__layer mdl-spinner__layer-3"><div class="mdl-spinner__circle-clipper mdl-spinner__left"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__gap-patch"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__circle-clipper mdl-spinner__right"><div class="mdl-spinner__circle"></div></div></div><div class="mdl-spinner__layer mdl-spinner__layer-4"><div class="mdl-spinner__circle-clipper mdl-spinner__left"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__gap-patch"><div class="mdl-spinner__circle"></div></div><div class="mdl-spinner__circle-clipper mdl-spinner__right"><div class="mdl-spinner__circle"></div></div></div></div>';
    return loading;
}

function getbt(thatis,btliistdiv){
    var that=thatis;
    var sbm= $(that).attr("sbm");
    var mgid= $(that).attr("mgid");
    var thatol=btliistdiv.find("ol")[0];
    var loading= getloading();
    $(thatol).append("<li class='clearnum'><div style=\"float:left\">正在请求数据，请稍后！</div>"+loading+"</li>");
    $(that).off("click");
    $.post('manager/getBt',{"searchval":sbm,"mgid":mgid},function(data) {
        if(!!data){
            $(thatol).empty();
            var rejson=eval("("+data+")");
            for(var i=0;i<rejson.length;i++){
                var btname=rejson[i].btname;
                var btlink=rejson[i].btlink;
                if(btlink!="###"){
                    var li="<li><a target=\"_blank\" href=\""+btlink+"\">"+btname+"</a></li>";
                }else{
                    var li="<li class='clearnum'>"+btname+"</li>";
                }
                $(thatol).append(li);
            }
        }else{
            $(thatol).empty();
            var li="<li class='errorli'><div>数据获取异常，请点击重新获取</div></li>";
            $(thatol).append(li);
        }
        $(that).on("click",isdownloadclick);
    });
}

function onekydownClick(){
    if(confirm("确定下载？")) {
        $("body").click();
        $(this).off("click");
        var thisobj = $(this);
        var mgid = $(this).attr("id");
        var img = $(this).attr("img");
        var parentdiv = $(this).parents(".singlediv")
        var btlist = parentdiv.find("#isbtlist").find("a");
        var thatol=parentdiv.find("#isbtlist").find("ol")[0];
        var lilen = btlist.length;
        var bts = new Array();
        var title = $(this).attr("sbm");
        addtoScheduleBar(mgid, title);

        var iszip = $(this).attr("iszip");
        if (lilen > 0) {
            $(btlist).each(function () {
                bts.push($(this).attr("href"));
            });
            createPackage(iszip,bts, img, title, thisobj, mgid);
        } else {
            $(parentdiv).find("#isbtlist").find("ol").each(function () {
                $.post('manager/getBt', {"searchval": title, "mgid": mgid}, function (data) {
                    var arrli=new Array();
                    if (!!data) {
                        var rejson = eval("(" + data + ")");
                        for (var i = 0; i < rejson.length; i++) {
                            bts.push(rejson[i].btlink);
                            var btname=rejson[i].btname;
                            var btlink=rejson[i].btlink;
                            if(btlink!="###") {
                                var li = "<li><a target=\"_blank\" href=\"" + btlink + "\">" + btname + "</a></li>";
                                $(thatol).append(li);
                                arrli.push(li);
                            }
                        }
                        if(arrli.length>0) {
                            createPackage(iszip, bts, img, title, thisobj, mgid);
                        }else{
                            changeScheduleBar(mgid, "-1");
                            addScheduleErr(mgid, "没有种子可以下载");
                        }
                    } else {
                        changeScheduleBar(mgid, "-1");
                        addScheduleErr(mgid, "获取BT异常,请稍后再试");
                    }
                });
            });
        }
        $(thisobj).on("click", onekydownClick);
    }
}
$(".onekydown").on("click",onekydownClick);

function createPackage(iszip,bts,imgs,basedir,thisobj,id){
    var btlist=bts.join("-9-");
    $.post('manager/createPackage',{"btlist":btlist,"oneid":id,"basedir":basedir,"iszip":iszip},function(data) {
        if(data=="is_ok"){
            changeScheduleBar(id,"1");
        }else if(data=="2"){
            changeScheduleBar(id,"-1");
            addScheduleErr(id,"下载种子出错");
        }else if(data=="3"){
            changeScheduleBar(id,"-1");
            addScheduleErr(id,"下载图片出错");
        }else if(data=="4"){
            changeScheduleBar(id,"-1");
            addScheduleErr(id,"忽略磁力链接");
        }else if(data=="5"){
            changeScheduleBar(id,"-1");
            addScheduleErr(id,"磁力链接不存在");
        }else if(data=="404"){
            changeScheduleBar(id,"-1");
            addScheduleErr(id,"链接不存在");
        }else{
            changeScheduleBar(id,"1");
            var imgid = Math.random();
            imgid=(imgid+"").replace("0.","");
            var downloadlink='<a href="'+data+'" target="_blank"><span id="down'+imgid+'">点击下载</span></a>';
            initDialog(downloadlink);
            $("#masklayer").off("click");
        }
    });
}

function swifeDel(rid,id){
    touch.on("#"+rid, 'swiperight', function(ev) {
        $("[id=sli"+id+"]").remove();
        $("[id=slierr"+id+"]").remove();
    });
}

function addtoScheduleBar(id,title){
    var ihref=window.location.href;
    ihref=ihref.split("#")[0];
    var rid = "sli"+id;
    var eid = "slierr"+id;
    if($("#"+rid).length==0){
        $(".schedulelist").append("<li class='sdoit' id='"+rid+"' title='"+title+"'><a href='"+ihref+"#"+id+"'>"+title+"</a></li>");
        $("#"+rid).slideDown(500);
        swifeDel(rid,id);
    }else{
        $("#"+rid).attr("class","").addClass("sdoit");
        $("#"+eid).remove();
    }
}

function changeScheduleBar(id,stu){
    var rid = "sli"+id;
    if(stu=="1"){
        $("#"+rid).removeClass("sdoit").addClass("sdone");
    }else{
        $("#"+rid).removeClass("sdoit").addClass("serr");
    }
}

function addScheduleErr(id,msg){
    var rid = "sli"+id;
    var eid = "slierr"+id;
    $("#"+rid).after("<li class='smsg' id='"+eid+"' title='"+msg+"'>"+msg+"</li>");
    swifeDel(eid,id);
}


$(function () {
    $(window).scroll(function () {
        //$(window).scrollTop()这个方法是当前滚动条滚动的距离
        //$(window).height()获取当前窗体的高度
        //$(document).height()获取当前文档的高度
        if(ispc()){
            var bot = 320; //bot是底部距离的高度
            if ((bot + $(window).scrollTop()) >= ($(document).height() - $(window).height())) {
                $(".totop").show();
            }
            if($(window).scrollTop()<=300){
                $(".totop").hide();
            }

            var heightc=$(window).scrollTop() - ($(document).height() - $(window).height());
            if(heightc<-180){
                $(".totop").addClass("totop2");
            }else{
                $(".totop").removeClass("totop2");
            }

            if($(".schedulebar").length>0){
                if($(window).scrollTop()>284){
                    $(".schedulebar").addClass("schedulebar2");
                }else{
                    $(".schedulebar").removeClass("schedulebar2");
                }
            }
        }
    });
});

$(".totop").click(function(){
    //window.scrollTo(0,0);
    $("body").animate({
        scrollTop:0
    }, 500 );
});