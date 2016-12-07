/**
 * 
 */
package com.meteor.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.meteor.kit.ClassKit;
import com.meteor.kit.DateKit;
import com.meteor.kit.JsonKit;
import com.meteor.kit.PageKit;
import com.meteor.kit.PgsqlKit;
import com.meteor.kit.SecurityEncodeKit;
import com.meteor.model.po.javimg;
import com.meteor.model.po.javsrc;
import com.meteor.model.po.javtor;
import com.meteor.model.vo.SearchQueryP;

/**
 * @author justlikemaki
 *
 */


public class HttpInterfaceAction extends Controller {
	private final Logger logger = LoggerFactory.getLogger(HttpInterfaceAction.class);

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
