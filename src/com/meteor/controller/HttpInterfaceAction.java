/**
 * 
 */
package com.meteor.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.meteor.kit.ClassKit;
import com.meteor.kit.DateKit;
import com.meteor.kit.FileOperateKit;
import com.meteor.kit.JsonKit;
import com.meteor.kit.PageKit;
import com.meteor.kit.PgsqlKit;
import com.meteor.kit.SecurityEncodeKit;
import com.meteor.model.po.javimg;
import com.meteor.model.po.javsrc;
import com.meteor.model.po.javtor;
import com.meteor.model.vo.BtList;
import com.meteor.model.vo.SearchQueryP;

/**
 * @author justlikemaki
 *
 */


public class HttpInterfaceAction extends Controller {
	private final Logger logger = LoggerFactory.getLogger(HttpInterfaceAction.class);
	
	public void test2() throws Exception{
		String search = getPara("search");
		loopFolder(search);
	}
	
	private void loopFolder(String folderPath){
		try {
			File file = new File(folderPath);
			if (!file.exists()) {
				return;
			}
			File[] tempList = file.listFiles();
			double length = tempList.length;
			for (int i = 0; i < tempList.length; i++) {
				File f=tempList[i];
				if(f.isDirectory()){
					System.out.println("进度条---"+(i+1)/length*100);
					System.out.println(f.getAbsolutePath());
					System.out.println(f.getName());
					
					loopFolder(f.getAbsolutePath());
					
					String path = f.getAbsolutePath()+"/";
					String search = f.getName();
					//List<BtList> btlist = PageKit.getBtNyaa(search,null,Boolean.FALSE);
					List<BtList> btlist = PageKit.getClp7(search,"000",Boolean.FALSE);
					if(btlist.isEmpty()) {
						continue;
					}
					if(btlist.size()>4) {
						btlist = btlist.subList(0,3);
					}
					if(btlist.get(0).getBtname().contains("403") && !btlist.get(0).getBtname().contains("magnet:?xt=")) {
						System.out.println("拒绝连接403："+btlist.get(0).getBtlink());
						return;
					}
					
					StringBuilder sb = new StringBuilder();
					for (BtList bt : btlist) {
						if(bt.getBtlink().equals("#")) {
							continue;
						}
						if(bt.getBtlink().equals("###")) {
							continue;
						}
						if (bt.getBtlink().contains("magnet:?xt=")) {
							String mg = bt.getBtlink().substring(bt.getBtlink().indexOf("magnet:?xt="));
							sb.append(mg).append("\r\n");
						} else {
							String filename = bt.getBtlink().substring(bt.getBtlink().lastIndexOf("/") + 1, bt.getBtlink().length());
							String filedest = path + filename;
							PageKit.downloadWithStatus(bt.getBtlink(), filedest, "3");
						}
					}
					if(sb.length()>0) {
						FileUtils.writeStringToFile(new File(path+search+".magnet.c.txt"),sb.toString());
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test() throws Exception{
		String dir = getPara("dir");
		SearchQueryP sp = new SearchQueryP();
		Map p = new LinkedHashMap();
//		p.put("tabtype", dir);
		p.put("tags", dir);
		p.put("NOT_tabtype", "classical");
		sp.setParameters(p);
		List<javsrc> js = new ArrayList<javsrc>();
		Map res = PgsqlKit.findByCondition(ClassKit.javClass, sp);
		js = (List<javsrc>) res.get("list");
		for (javsrc javsrc : js) {
			downloadSrc(javsrc, dir);
		}
		System.out.println();
	}
	
	private String downloadSrc(javsrc js,String dir) {
		String rootsavedir = "javsrc/";
		String sbm = js.getSbm()==null?js.getId():js.getSbm();
		String basedir = dir +"/"+ sbm;
		String fileorigpath = "/home/maki/桌面";
		String filepath = fileorigpath;
		filepath = filepath + "/onekeytmp/" + basedir + "/";
		File f0 = new File(filepath);
		if (!f0.exists()) {
			f0.mkdir();
		}
		try {
			String img = js.getImgsrc();
			if (img.startsWith(PageKit.getimgBase64Key())) {
				String imgid = img.split(PageKit.getimgBase64Key())[1];
				javimg jm = (javimg) PgsqlKit.findById(ClassKit.javimgClass, imgid);
				String baseimg = jm.getImgbase();
				String filename =  sbm + ".jpg";
				String filedest = filepath + filename;
				SecurityEncodeKit.GenerateImage(baseimg, filedest);
			} else if (img.contains("data:image/")) {
				img = img.replace(PageKit.getimgBase64Tip(), "");
				String filename = sbm + ".jpg";
				String filedest = filepath + filename;
				SecurityEncodeKit.GenerateImage(img, filedest);
			} else if (img.contains(rootsavedir)) {
				String imgpaht = img.replace(rootsavedir, "");
				String fileorig = fileorigpath + imgpaht;
				String filename = img.substring(img.lastIndexOf("/") + 1, img.length());
				String filedest = filepath + filename;
				FileUtils.copyFile(new File(fileorig), new File(filedest));
			} else {
				String url = PageKit.replace20(img);
				String filename = img.substring(img.lastIndexOf("/") + 1, img.length());
				String filedest = filepath + filename;
				String returncode = PageKit.downloadWithStatus(url, filedest, "3");
//				if (!returncode.equals("0")) {
//					return returncode;
//				}
			}
		} catch (Exception e) {
			logger.error("createPackage: " + e.toString());
			// renderText("3");//下载图片出错。
			return "3";
		}

		boolean isDownloadOne = false;
		String reCode = "0";
		try {
			if(StringUtils.isBlank(js.getBtfile()) || js.getBtfilelist().isEmpty()) {
				return "0";
			}
			List<String> bts = js.getBtfilelist();
			List<String> btns = js.getBtnamelist();
			for (int i = 0; i < bts.size(); i++) {
				if (bts.get(i).contains(PageKit.gettorBase64Key())) {
					String torid = bts.get(i).split(PageKit.gettorBase64Key())[1];
					javtor jtr = (javtor) PgsqlKit.findById(ClassKit.javtorClass, torid);
					String tor = jtr.getTorbase();
					String filename = btns.get(i) + ".torrent";
					String filedest = filepath + filename;
					SecurityEncodeKit.GenerateImage(tor, filedest);
				} else if (bts.get(i).contains(rootsavedir)) {
					String bt = bts.get(i).replace(rootsavedir, "");
					String fileorig = fileorigpath + bt;
					//String filename = bts.get(i).substring(bts.get(i).lastIndexOf("/") + 1, bts.get(i).length());
					String filename = btns.get(i) + ".torrent";
					String filedest = filepath + filename;
					FileUtils.copyFile(new File(fileorig), new File(filedest));
					isDownloadOne = true;
				} else {
					if (bts.get(i).contains("magnet:?xt=")) {
						String filename = btns.get(i) + ".magnet.txt";
						String filedest = filepath + filename;
						FileUtils.writeStringToFile(new File(filedest),bts.get(i));
					} 
				}
			}
		} catch (Exception e) {
			logger.error("createPackage: " + e.toString());
			// renderText("2");//下载种子出错。
			// return "2";
			reCode = "2";
		}
		if (!reCode.equals("0") && isDownloadOne == true) {
			reCode = reCode + "_isdown";
		}
		return reCode;
	}

	public void getRight(){
		ServletContext sct=getRequest().getServletContext();
		int withdate=getParaToInt("withdate");
		Map res=PageKit.getparametersInterface(sct, withdate);

		String callback=getPara("callback");
		if(StringUtils.isNotBlank(callback)){
			renderText("jsonp(" + JsonKit.bean2JSON(res)+")");
		}else {
			renderText(JsonKit.bean2JSON(res));
		}
	}

	private String getuuid(Map res){
		res.put("status", -3);
		res.put("errmsg","没有权限的设备！");
		return "jsonp("+JsonKit.bean2JSON(res)+")";
	}

	public void getLeft() {
		Map res = new HashMap();
		try {
			String spstr=getPara("sp");
			SearchQueryP p= JsonKit.json2Bean(spstr,SearchQueryP.class);
			int count=0;
			if(p.getSbtype().equals("web")){
				count=PropKit.getInt("webpagesize");
			}else{
				String uuid=getPara("uuid");
				if(StringUtils.isBlank(uuid)){
					throw new Exception("没有权限的设备！");
				}
				logger.info("当前访问设备号："+uuid);
				Prop pk=PropKit.getProp("accessuuid.txt");
				boolean flag=false;
				Iterator it= pk.getProperties().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String value = (String) entry.getValue();
					if(value.equals(uuid)){
						flag=true;
						break;
					}
				}
				if(flag) {
					count = PropKit.getInt("mbpagesize");
				}else{
					throw  new Exception("没有权限的设备！");
				}
			}
			p.setCount(count);
			p=istoday(p);

			if(p.getNowpage()!=0) {
				res = PgsqlKit.findByCondition(ClassKit.javClientClass, p);
				res.put("status", 0);
				res.put("pagecount",count);
			}else{
				res.put("status", -2);
				res.put("errmsg","参数错误");
			}
		} catch (Exception e) {
			logger.error("getLeft: " + e);
			res.put("errmsg",e.toString());
			res.put("status", -1);
		}

		String callback=getPara("callback");
		if(StringUtils.isNotBlank(callback)){
			renderText("jsonp(" + JsonKit.bean2JSON(res)+")");
		}else {
			renderText(JsonKit.bean2JSON(res));
		}
	}

	private SearchQueryP istoday(SearchQueryP p){
		int istoday=PropKit.getInt("showintoday");
		if(istoday==1){
			String overtime=DateKit.getStringDate();
			Map rp=p.getParameters();
			if(rp==null){
				rp=new HashMap();
			}
			rp.put("LTE_times",overtime);
			p.setParameters(rp);
		}
		return p;
	}

	public void getBt() {
		try {
			javsrc jav = getModel(javsrc.class);
			String sv = getPara("searchval");
			String id = getPara("mgid");
			String idtype=PropKit.get("selectbt");
			String res=PageKit.selectbt(idtype, sv, id, jav,false);

			String callback=getPara("callback");
			if(StringUtils.isNotBlank(callback)){
				renderText("jsonp(" + res+")");
			}else {
				renderText(res);
			}
		}catch (Exception e){
			logger.error("getBt: " + e.toString());
			renderText("");
		}
	}

	public void pageGetBt() {
		try {
			String sv = getPara("searchval");
			String idtype = getPara("idtype");
			String flag = getPara("islike");
			boolean likeflag=true;
			if(flag.equalsIgnoreCase("false")){
				likeflag=false;
			}
			String res = PageKit.selectbt(idtype, sv, null, null,likeflag);

			String callback=getPara("callback");
			if(StringUtils.isNotBlank(callback)){
				renderText("jsonp(" + res+")");
			}else {
				renderText(res);
			}
		}catch (Exception e){
			logger.error("getBt: " + e.toString());
			renderText("");
		}
	}

	public void getclweb(){
		Map resp=new HashMap();
		String res = PageKit.getCaoLiu();
		resp.put("status","0");
		resp.put("res",res);
		String callback=getPara("callback");
		if(StringUtils.isNotBlank(callback)){
			renderText("jsonp(" + JsonKit.bean2JSON(res)+")");
		}else {
			renderText(JsonKit.bean2JSON(res));
		}
	}

	public void imgbase() throws IOException{
		OutputStream out = null;
		try {
			String oneid=getPara();
			oneid=oneid.replace(PageKit.getimgBase64Key(), "");
			javimg js = (javimg) PgsqlKit.findById(ClassKit.javimgClass, oneid);
			String img=js.getImgbase();
			byte[] imgbytes=SecurityEncodeKit.GenerateImage(img);
			if(imgbytes!=null) {
				HttpServletResponse response=getResponse();
				response.setContentType("image/jpeg");
				response.setContentLength(imgbytes.length);
				response.setHeader("Accept-Ranges", "bytes");
				out = response.getOutputStream();
				out.write(imgbytes);
				out.flush();
				out.close();
				renderNull();
				return;
			}else{
				logger.error("imgbase: " +"不是base64编码的图片");
			}
		} catch (Exception e) {		
			if(e.toString().contains("Connection reset by peer")){
				renderNull();			
				return;
			}else{
				logger.error("imgbase: " + e.toString());
			}
		} finally {
			if(out!=null) {
				out.close();
				out=null;
			}
		}
		renderText("请求图片出错");
	}
	
	public void torbase() throws IOException{
		OutputStream out = null;
		try {
			String oneid=getPara();
			oneid=oneid.replace(PageKit.gettorBase64Key(), "");
			javtor js = (javtor) PgsqlKit.findById(ClassKit.javtorClass, oneid);
			String tor=js.getTorbase();
			byte[] torbytes=SecurityEncodeKit.GenerateImage(tor);
			if(torbytes!=null) {
				HttpServletResponse response=getResponse();
				response.setHeader("content-disposition", "inline; filename="+oneid+".torrent");
				response.setContentType("application/x-bittorrent");
				response.setContentLength(torbytes.length);
				response.setHeader("Accept-Ranges", "bytes");
				out = response.getOutputStream();
				out.write(torbytes);
				out.flush();
				out.close();
				renderNull();			
				return;
			}else{
				logger.warn("torbase: " +"不是base64编码的种子");
			}
		} catch (Exception e) {
			if(e.toString().contains("Connection reset by peer")){
				renderNull();			
				return;
			}else{
				logger.warn("torbase: " + e.toString());
			}
		} finally {
			if(out!=null) {
				out.close();
				out=null;
			}
		}
		renderText("请求种子出错");
	}
}
