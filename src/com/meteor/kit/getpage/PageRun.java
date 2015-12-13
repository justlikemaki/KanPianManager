package com.meteor.kit.getpage;

import com.meteor.kit.*;
import com.meteor.model.po.errpage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Meteor on 2015/8/30.
 *
 * @category (这里用一句话描述这个类的作用)
 */
public class PageRun {
    private final Logger logger = LoggerFactory.getLogger(PageRun.class);
    private PageManager pm;
    public PageRun(PageManager pm){
        this.pm=pm;
    }

    public void doit(double threadnum,String nums,String type,String fhkey) {
        logger.error("正在获取"+type+"的资源,page:"+nums);
        pm.setBgtime(new Date().getTime());
        List parentArray=filterNums( threadnum, nums);
        int threadnumint=(int)threadnum;
        CyclicBarrier barrier = new CyclicBarrier(threadnumint,new MainGetPage(pm,type));
        ExecutorService exec = Executors.newFixedThreadPool(threadnumint);
        for (int i = 0; i < threadnum; i++) {
            List<String> numlist= (List<String>) parentArray.get(i);
            exec.submit(new SubGetPage(pm,barrier,numlist,type,fhkey));
        }
        exec.shutdown();
    }

    private List filterNums(double threadnum,String nums){
        String[] numarray=null;
        if(nums.indexOf("--")>-1){
            int bg=Integer.valueOf(nums.substring(0, nums.indexOf("--")));
            int ed=Integer.valueOf(nums.substring(nums.indexOf("--") + 2));
            List<String> sb=new ArrayList<String>();
            for(int i=bg;i<=ed;i++){
                sb.add(i+"");
            }
            numarray=(String[])sb.toArray(new String[sb.size()]);
        }else{
            numarray=nums.split(",");
        }

        List parentArray=new ArrayList();
        int length=numarray!=null ? numarray.length:1;
        double  l=Math.ceil(length/threadnum);
        int intlength= (int) Math.ceil(length/threadnum);
        for(int i = 0; i <threadnum; i++) {
            List<String> childArray=new ArrayList<String>();
            int ed=(i+1)*intlength;
            int bg=i*intlength;
            for (int j = bg; j <ed; j++) {
                if(numarray!=null){
                    if(j+1<=numarray.length){
                        String nm=numarray[j];
                        childArray.add(nm);
                    }else {
                        break;
                    }
                }else{
                    childArray.add(j+"");
                }
            }
            parentArray.add(childArray);
        }
        return parentArray;
    }

}

class SubGetPage implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(SubGetPage.class);
    private CyclicBarrier barrier;
    private PageManager pm;
    private List<String> nums;
    private String type;
    private String fhkey;

    public SubGetPage(PageManager pm,CyclicBarrier barrier, List<String> nums,String type,String fhkey){
        this.pm=pm;
        this.barrier=barrier;
        this.nums=nums;
        this.type=type;
        this.fhkey=fhkey;
    }

    @Override
    public void run() {
        Thread td=Thread.currentThread();
        String threadName=td.getName();
        threadName=threadName+":";
        pm.setSb("<br>"+threadName+"正在请求数据。。。");
        foreachpage(nums, type, fhkey, threadName);
        try {
            barrier.await();
        } catch (Exception e) {
            logger.error("barrier+1",e);
        }
    }

    /**
     *
     * @标题: BaseAction.java
     * @版权: Copyright (c) 2014
     * @公司: VETECH
     * @作者：LF
     * @时间：2014-8-9
     * @版本：1.0
     * @方法描述：循环拉取页面
     */
    private void foreachpage(List<String> nums,String type,String searchval,String threadname) {
        try {
            threadname=threadname+"---";
            /**得到所有的资源标题--begin**/
            String javtitles="";// PageKit.getSrcTitle(searchval);
            /**得到所有的资源标题--end**/
            //循环拉取页面
            forEachGetPage( threadname, nums, type, searchval, javtitles);

        } catch (Exception e) {
            logger.error(type+" page:" + JsonKit.bean2JSON(nums) + "---" + e.toString());
            pm.setSb("<br>"+type +" page:" + JsonKit.bean2JSON(nums) + "---" + e.toString());
        }

    }

    private void forEachGetPage(String threadname,List<String> nums,String type,String searchval,String javtitles) throws Exception{
        //循环拉取数据
        if(nums!=null&&nums.size()>0){
            //List errnums=new ArrayList();
            int nownum=0;
            for (int i = 0; i <nums.size();i++) {
                try{
                    String sstj = StringUtils.isNotBlank(searchval) ? searchval : "";//搜索条件
                    nownum = Integer.valueOf(nums.get(i));
                    if(type.equals("censored")) {
                          PageKit.getJavmoo( javtitles, sstj, nownum);
                    }
                    if(type.equals("uncensored")) {
                          PageKit.getJavlog(javtitles, sstj, nownum);
                    }
                    if(type.equals("westpron")) {
                          PageKit.getPornleech(javtitles, sstj, nownum);
                    }
                } catch (Exception e) {
                    if(!e.toString().contains("404")) {
                        pm.setErrnums(nownum);
                        logger.error(type+" 当前请求：" + nownum + "---" + e.toString());
                        errpage err= new errpage(type,nownum+"",e.toString(),searchval);
                        PgsqlKit.save(ClassKit.errTableName,err);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException g) {
                        //g.printStackTrace();
                    }
                } catch(Throwable t) {
                    if(!t.toString().contains("404")) {
                        pm.setErrnums(nownum);
                        logger.error(type+" 当前请求：" + nownum + "---" + t.toString());
                        errpage err= new errpage(type,nownum+"",t.toString(),searchval);
                        PgsqlKit.save(ClassKit.errTableName,err);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException g) {
                        //g.printStackTrace();
                    }
                }
            }

            //logger.error( JsonKit.bean2JSON(nums)+"更新完成！");
            pm.setSb("<br>"+threadname+type+" "+JsonKit.bean2JSON(nums) +"更新完成！");
        }else{
            pm.setSb("<br>"+threadname+type+" 没有指定请求页数！");
        }
    }
}

class MainGetPage implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(MainGetPage.class);
    private PageManager pm;
    private String type;

    public MainGetPage( PageManager pm,String type){
        this.pm=pm;
        this.type=type;
    }

    @Override
    public void run() {
        long edtime=new Date().getTime()-pm.getBgtime();
        String hs= DateKit.getTimeOff(edtime);
        String errnums=pm.getErrnums();
        if(StringUtils.isNotBlank(errnums)){
            logger.error("失败页数:" + errnums);
            pm.setSb("<br>" + "失败页数:" + errnums);
        }
        pm.setSb("<br>"+type +" All Right,"+"耗时："+hs);
        logger.error(type+" 全部执行完毕,"+"耗时："+hs);
        PageKit.delrepeated();
    }
}