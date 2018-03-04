/**
 * 
 */
package com.meteor.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.meteor.interceptor.LoginCheck;
import com.meteor.kit.PageKit;
import com.meteor.kit.PgsqlKit;

/**
 * @author justlikemaki
 *
 */

@Before(LoginCheck.class)
public class WebAction extends Controller {
	private final Logger logger = LoggerFactory.getLogger(WebAction.class);

	@Clear
	public void tologin(){
//		StringBuffer sb =new StringBuffer();
//		for (int i = 0; i < 5620; i++) {
//			PgsqlKit.tobk(i+1, 30,sb);
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		try {
//			FileUtils.writeStringToFile(new File("D:\\Development\\Servers\\Tomcats\\sql.txt"), sb.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		render("login.jsp");
	}
	
	@Clear
	public void login(){
		String root=getPara("root");
		String pass=getPara("pass");
		String cfroot=PropKit.get("root");
		String cfpass=PropKit.get("pass");
		if(root.equals(cfroot)&&pass.equals(cfpass)) {
			Map p = new HashMap();
			p.put("root", root);
			p.put("pass", pass);
			getSession().setAttribute("lguser", p);
			redirect("/");
		}else{
			getRequest().setAttribute("errmsg","账号密码错误");
			tologin();
		}
	}

	@Clear
	public void clweb(){
		String res = PageKit.getCaoLiu();
		getRequest().setAttribute("clres", res);
		editForm();
		render("clweb.jsp");
	}

	public void index(){
		newspage();
	}
	
	public void newspage(){
		HttpServletRequest request=getRequest();
		String p=getPara();
		editForm();
		int num=1;
		if(StringUtils.isNotBlank(p)) {
			num = getParaToInt();
		}
		int pagesize= PropKit.getInt("pagesize");
		render(PageKit.topage(request, num, pagesize, "newspage", "list", null) + ".jsp");
	}

	public void censored(){
		HttpServletRequest request=getRequest();
		String p=getPara();
		editForm();
		int num=1;
		if(StringUtils.isNotBlank(p)) {
			num = getParaToInt();
		}
		int pagesize= PropKit.getInt("pagesize");
		Map scmap=new HashMap();
		scmap.put("tabtype","censored");
		render(PageKit.topage(request, num, pagesize, "censored", "list", scmap)+ ".jsp");
	}

	public void uncensored(){
		HttpServletRequest request=getRequest();
		String p=getPara();
		editForm();
		int num=1;
		if(StringUtils.isNotBlank(p)) {
			num = getParaToInt();
		}
		int pagesize= PropKit.getInt("pagesize");
		Map scmap=new HashMap();
		scmap.put("tabtype","uncensored");
		render(PageKit.topage(request, num, pagesize, "uncensored", "list", scmap)+ ".jsp");
	}

	public void westporn(){
		HttpServletRequest request=getRequest();
		String p=getPara();
		editForm();
		int num=1;
		if(StringUtils.isNotBlank(p)) {
			num = getParaToInt();
		}
		int pagesize= PropKit.getInt("pagesize");
		Map scmap=new HashMap();
		scmap.put("tabtype","westporn");
		render(PageKit.topage(request, num, pagesize, "westporn", "list", scmap)+ ".jsp");
	}

	public void classical(){
		HttpServletRequest request=getRequest();
		String p=getPara();
		editForm();
		int num=1;
		if(StringUtils.isNotBlank(p)) {
			num = getParaToInt();
		}
		int pagesize= PropKit.getInt("pagesize");
		Map scmap=new HashMap();
		scmap.put("tabtype","classical");
		render(PageKit.topage(request, num, pagesize, "classical", "list", scmap)+ ".jsp");
	}


	public void addedit(){
		HttpServletRequest request=getRequest();
		PageKit.setpc(request);
		render(PageKit.topage(request, 0, 0, "addsrc", "addedit", null)+ ".jsp");
	}

	public void setting(){
		HttpServletRequest request=getRequest();
		PageKit.setpc(request);
		editForm();
		render(PageKit.topage(request, 0, 0, "setting", "setting", null)+ ".jsp");
	}

	public void getbtlist(){
		HttpServletRequest request=getRequest();
		PageKit.setpc(request);
		editForm();
		render(PageKit.topage(request, 0, 0, "getbtlist", "getbtlist",null)+ ".jsp");
	}

	public void search(){
		HttpServletRequest request=getRequest();
		String p=getPara();
		editForm();
		String tagstr="";
		String searchzd = "";
		String sp = getPara("sp");
		String time = getPara("time");

		Map scmap=new HashMap();
		String type = getPara("type");
		if(StringUtils.isNotBlank(type)) {
			String zhetype=PageKit.getTabType(type);
			if(StringUtils.isNotBlank(zhetype)){
				type=zhetype;
			}
			request.setAttribute("searchtype", type);
			scmap.put("tabtype", type);
		}

		if(StringUtils.isNotBlank(sp)||StringUtils.isNotBlank(time)) {
			if (StringUtils.isBlank(sp)) {
				tagstr = time;
				searchzd = "times";
				scmap.put(searchzd,tagstr);
				if (StringUtils.isNotBlank(tagstr)) {
					request.setAttribute("searchname", "time");
					request.setAttribute("searchvalue", tagstr);
				}
			} else {
				tagstr = sp;
				searchzd = "tags";
				scmap.put(searchzd,tagstr);
				if (StringUtils.isNotBlank(tagstr)) {
					request.setAttribute("searchname", "sp");
					request.setAttribute("searchvalue", tagstr.toUpperCase());
				}
			}
		}else{
				tagstr = "";
				searchzd = "tags";
				scmap.put(searchzd,tagstr);
				request.setAttribute("searchname", "sp");
				request.setAttribute("searchvalue", "");
		}

		int num=1;
		if(StringUtils.isNotBlank(p)) {
			num = getParaToInt();
		}
		int pagesize= PropKit.getInt("pagesize");
		render(PageKit.topage(request,num, pagesize, tagstr.toUpperCase(), "list", scmap)+ ".jsp");
	}

	private void editForm() {
		HttpServletRequest request = getRequest();
		ServletContext sct = getSession().getServletContext();
		//如果页面右侧参数为空，默认跳转到首页
		String res = PageKit.getparameters(sct);
		if (res == null) {
			request.setAttribute("pagetype", "list");
			request.setAttribute("tab", "newspage");
		}

		if (PageKit.ispc(request)) {
			int streammode = PropKit.getInt("streammode");
			request.setAttribute("streammode", streammode);
			request.setAttribute("ispc", "1");
		} else {
			request.setAttribute("ispc", "0");
		}
	}

}
