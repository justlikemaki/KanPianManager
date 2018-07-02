var dsq=null;
var nowsession=new Date().getTime();
function postpage(typeval,fhkeyval,jsonlistval,threadnum){
    var jsonres={type:typeval,fhkey:fhkeyval,jsonlist:jsonlistval,threadnum:threadnum,timesession:nowsession};
    showconsole("Waiting...");
    $.post("manager/newsrc",jsonres,function(data){
        initDialog(data);
        dsq=setInterval(function(){
            getPageManager();
        },3000);
    });
}

function getPageManager(){
    $.post("manager/getPageManager",{timesession:nowsession},function(data){
        initDialog(data);
        if(data.indexOf("All Right")>-1){
            clearInterval(dsq);
        }
    });
}

function showconsole(data){
    initDialog(data);
    $("#masklayer").off("click");
    $("#thedialogcontext").css("max-height", "240px");
    $("#thedialogcontext").css("overflow-y", "scroll");
}

$("#fildpage").on("click",function(){
    var fild=$("#fild").val();
    if(!!!fild){
        alert("页码数不能为空！");
        return;
    }
    var fhkey=$("#fhkey").val();
    if(confirm("确定么?")){;
        var searchtype=$("#settype").val();
        var threadnum=$("#thdnum").val();
        var jsonlist=fild;
        postpage(searchtype,fhkey,jsonlist,threadnum);
    }
});

$("#addpage").on("click",function(){
    var fild=$("#fild").val();
    if(!!!fild){
        alert("页码数不能为空！");
        return;
    }
    var fhkey=$("#fhkey").val();
    if(confirm("确定么?")){;
        var searchtype=$("#settype").val();
        var jsonstr="{type:'"+searchtype+"',fhkey:'"+fhkey+"',jsonlist:'"+fild+"'}";
        var jsonres=eval("("+jsonstr+")");
        $.post("manager/addToErrList",jsonres,function(data){
            alert(data);
        });
    }
});

$("#tobase64").on("click",function(){
    if(confirm("确定么?")){
        $.post("manager/tobase64",function(data){
            alert(data);
        })
    }
})

$("#classtobase64").on("click",function(){
    if(confirm("确定么?")){
        $.post("manager/classcalTo64",function(data){
            alert(data);
        })
    }
})

$("#westporntobase64").on("click",function(){
    if(confirm("确定么?")){
        $.post("manager/westpornTo64",function(data){
            alert(data);
        })
    }
})


$("#updatecache").on("click",function(){
    if(confirm("确定么?")){
        $.post("manager/updateCache",function(data){
            alert(data);
        })
    }
})

function writecof(){
    var text=$("#coftext").val();
    if(confirm("确定么?")){
        $.post("manager/writeconfig",{text:text},function(data){
           delDialog();
           alert(data);
        })
    }
}

function writecofuuid(){
    var text=$("#coftext").val();
    if(confirm("确定么?")){
        $.post("manager/writeuuidconfig",{text:text},function(data){
            delDialog();
            alert(data);
        })
    }
}

$("#readprop").on("click",function(){
    if(confirm("确定么?")){
        $.post("manager/readconfig",function(data){
            if(data=="-1"){
                alert("读取配置文件异常");
            }else {
                var textarea = '<textarea id="coftext">' + data + '</textarea><span class="spanbtn" onclick="writecof()">确定</span>'
                initDialog(textarea);
            }
        })
    }
})

$("#readuuid").on("click",function(){
    if(confirm("确定么?")){
        $.post("manager/readuuid",function(data){
            if(data=="-1"){
                alert("读取配置文件异常");
            }else {
                var textarea = '<textarea id="coftext">' + data + '</textarea><span class="spanbtn" onclick="writecofuuid()">确定</span>'
                initDialog(textarea);
            }
        })
    }
})


function writeexl(){
    var inpath=$("#inpath").val()+"";
    var outpath=$("#outpath").val()+"";
    var regxls=/.xls$/i;
    if(!regxls.test(outpath)){
        outpath=outpath+".xls";
    }
    var seleval=$("#seletype").val();
    delDialog();
    $.post("manager/writeExl",{inpath:inpath,outpath:outpath,cdlx:seleval},function(data){
        if(data=="1"){
            alert("生成exl成功");
        }else{
            alert("生成exl失败");
        }
    });
}

$("#doWriteExl").on("click",function(){
    var inpath=$("#inpath").val()+"";
    var outpath=$("#outpath").val()+"";
    var regxls=/.xls$/i;
    if(!regxls.test(outpath)){
        outpath=outpath+".xls";
    }
    if(inpath==""||outpath==""){
        alert("不能有空值");
    }else {
        var select = '<select id="seletype"> <option value="1">硬盘存储</option> <option value="2">网络存储</option></select>';
        var input = '<div class="inputdiv mdl-textfield mdl-js-textfield is-upgraded is-dirty" data-upgraded=",MaterialTextfield">' + select + '<span class="spanbtn" onclick="writeexl()">确定</span></div>';
        initDialog(input);
    }
});

$("#doExportFiles").on("click",function(){
    var inpath=$("#inpath").val();
    var outpath=$("#outpath").val();
    if(inpath==""||outpath==""){
        alert("不能有空值");
    }else{
        if(confirm("确定么?")){
            $.post("manager/expFiles",{inpath:inpath,outpath:outpath},function(data){
                if(data=="1"){
                    alert("导出成功");
                }else{
                    alert("导出失败");
                }
            });
        }
    }
});

$("#doExportJpg").on("click",function(){
    var inpath=$("#inpath").val();
    var outpath=$("#outpath").val();
    if(inpath==""||outpath==""){
        alert("不能有空值");
    }else{
        if(confirm("确定么?")){
            $.post("manager/expFiles",{inpath:inpath,outpath:outpath,containted:".jpg"},function(data){
                if(data=="1"){
                    alert("导出图片成功");
                }else{
                    alert("导出图片失败");
                }
            });
        }
    }
});