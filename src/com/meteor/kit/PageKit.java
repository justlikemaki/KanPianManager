package com.meteor.kit;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.meteor.common.MainConfig;
import com.meteor.kit.email.SendEmail;
import com.meteor.kit.getpage.PageManager;
import com.meteor.kit.getpage.PageRun;
import com.meteor.kit.http.HttpClientHelp;
import com.meteor.kit.http.HttpUtilKit;
import com.meteor.model.po.javsrc;
import com.meteor.model.po.javtor;
import com.meteor.model.vo.BtList;
import com.meteor.model.vo.CompDate;
import com.meteor.model.vo.CompDls;
import com.meteor.model.vo.DateVo;
import com.meteor.model.vo.SearchQueryP;

public class PageKit {

	private final static Logger logger = LoggerFactory.getLogger(PageKit.class);
	private static String imgBase64Tip="data:image/jpg;base64,";
	private static String imgReplcKey="netcdn.space";
	private static String torBase64Key="tor64--";
	private static Map<String,String> tabtype=new HashMap<String, String>();
	static {
		tabtype.put("0","newspage");
		tabtype.put("1","censored");
		tabtype.put("2","uncensored");
		tabtype.put("3","westporn");
		tabtype.put("4","classical");
	}

	public static String getTabType(String type){
		return tabtype.get(type);
	}

	public static String getimgBase64Tip(){
		return imgBase64Tip;
	}
	
	public static String gettorBase64Key(){
		return torBase64Key;
	}

	public static boolean hasTabType(String value){
		for(Map.Entry<String, String> entry:tabtype.entrySet()){
			if(entry.getValue().equals(value.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	/**
	 * @标题: BaseAction.java
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：每个月份有多少条数据的计算
	 */
	public static List datelist(String bg, String ed) throws Exception {
		int bgyear, bgmonth, edyear, edmonth;
		bgyear = Integer.parseInt(bg.substring(0, 4));
		bgmonth = Integer.parseInt(bg.substring(5, 7));
		edyear = Integer.parseInt(ed.substring(0, 4));
		edmonth = Integer.parseInt(ed.substring(5, 7));

		Calendar nowdate = Calendar.getInstance();
		nowdate.set(bgyear, bgmonth - 1, 1);
		Calendar olddate = Calendar.getInstance();
		olddate.set(edyear, edmonth - 1, 1);

		List<DateVo> dates = new ArrayList();
		String months, years;
		while (nowdate.compareTo(olddate) > -1) {
			//System.out.println(olddate.get(Calendar.YEAR)+"-"+olddate.get(Calendar.MONTH));
			DateVo d = new DateVo();
			int nowmonths = nowdate.get(Calendar.MONTH) + 1;
			if (nowmonths < 10) {
				months = "0" + (nowmonths);
			} else {
				months = (nowmonths) + "";
			}
			years = nowdate.get(Calendar.YEAR) + "";
			d.setDatecn(years + "年" + months + "月");
			d.setDateen(years + "-" + months);
			Map mp = new HashMap();
			mp.put("times", years + "-" + months);
			long count = PgsqlKit.getCollectionCount(ClassKit.javTableName, mp);//查询当月有多少条数据
			d.setCount(count + "");
			if (count > 0) {//只显示有数据的月份
				dates.add(d);
			}
			nowdate.set(nowdate.get(Calendar.YEAR), nowdate.get(Calendar.MONTH) - 1, nowdate.get(Calendar.DATE));
		}
		return dates;
	}

	private static List<DateVo> getTimelistInterface() throws Exception {
		String sql=null;
		int istoday=PropKit.getInt("showintoday");
		if(istoday==1){
			String overtime=DateKit.getStringDate();
			sql = "select to_char(to_date(times,'YYYY-MM'), 'YYYY-MM')dateen,to_char(to_date(times,'YYYY-MM'), 'YYYY年MM月')datecn ,count(1)" +
					"from javsrc where times <= '"+overtime+"' group BY to_date(times,'YYYY-MM') ORDER BY to_date(times,'YYYY-MM') desc";
		}else {
			sql = "select to_char(to_date(times,'YYYY-MM'), 'YYYY-MM')dateen,to_char(to_date(times,'YYYY-MM'), 'YYYY年MM月')datecn ,count(1)" +
					"from javsrc group BY to_date(times,'YYYY-MM') ORDER BY to_date(times,'YYYY-MM') desc";
		}
		List<Record> list = Db.find(sql);
		List<DateVo> datelist = BeanKit.copyRec(list,DateVo.class);
		return datelist;
	}

	private static List<Map.Entry> getHotlistInterface() throws Exception {
		String sql=null;
		int istoday=PropKit.getInt("showintoday");
		if(istoday==1) {
			String overtime = DateKit.getStringDate();
			sql="SELECT * from (select replace(replace(replace(regexp_split_to_table(tags,','),'\"',''),']',''),'[','') tags,count(1) from javsrc where times <= '"+overtime+"'" +
					"GROUP BY replace(replace(replace(regexp_split_to_table(tags,','),'\"',''),']',''),'[','') ORDER BY count(1) desc) t1 "+
					"where t1.tags not LIKE '%WESTPORN%' and t1.tags not LIKE '%CENSORED%' and t1.tags not LIKE '%CLASSICAL%' and t1.tags not LIKE '%单片%' and  t1.tags not LIKE '%高画质%' " +
					"LIMIT 90 OFFSET 10";
		}else {
			sql="SELECT * from (select replace(replace(replace(regexp_split_to_table(tags,','),'\"',''),']',''),'[','') tags,count(1) from javsrc " +
					"GROUP BY replace(replace(replace(regexp_split_to_table(tags,','),'\"',''),']',''),'[','') ORDER BY count(1) desc) t1 "+
					"where t1.tags not LIKE '%WESTPORN%' and t1.tags not LIKE '%CENSORED%' and t1.tags not LIKE '%CLASSICAL%' and t1.tags not LIKE '%单片%' and  t1.tags not LIKE '%高画质%' " +
					"LIMIT 90 OFFSET 10";
		}
		List<Record> list= Db.find(sql);
		List<Map.Entry> mappingList = new ArrayList<Map.Entry>();
		for (int i=0 ; i < list.size() ; i++){
			Map<String, String> mpt= new HashMap<String, String>();
			Map rc=list.get(i).getColumns();
			mpt.put(rc.get("tags").toString(),rc.get("count").toString());
			Iterator<Map.Entry<String, String>> it=mpt.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				mappingList.add(entry);
			}
		}
		return mappingList;
	}

	private static List<DateVo> getTimelist() throws Exception {
		int istoday=PropKit.getInt("showintoday");
		String sql = "select to_char(to_date(times,'YYYY-MM'), 'YYYY-MM')dateen,to_char(to_date(times,'YYYY-MM'), 'YYYY年MM月')datecn ,count(1)" +
					"from javsrc group BY to_date(times,'YYYY-MM') ORDER BY to_date(times,'YYYY-MM') desc";
		List<Record> list = Db.find(sql);
		List<DateVo> datelist = BeanKit.copyRec(list,DateVo.class);
		return datelist;
	}

	private static List<Map.Entry> getHotlist() throws Exception {
		String sql="SELECT * from (select replace(replace(replace(regexp_split_to_table(tags,','),'\"',''),']',''),'[','') tags,count(1) from javsrc " +
				"GROUP BY replace(replace(replace(regexp_split_to_table(tags,','),'\"',''),']',''),'[','') ORDER BY count(1) desc) t1 "+
				"where t1.tags not LIKE '%WESTPORN%' and t1.tags not LIKE '%CENSORED%' and t1.tags not LIKE '%CLASSICAL%' and t1.tags not LIKE '%单片%' and  t1.tags not LIKE '%高画质%' " +
				"LIMIT 90 OFFSET 10";
		List<Record> list= Db.find(sql);
		List<Map.Entry> mappingList = new ArrayList<Map.Entry>();
		for (int i=0 ; i < list.size() ; i++){
			Map<String, String> mpt= new HashMap<String, String>();
			Map rc=list.get(i).getColumns();
			mpt.put(rc.get("tags").toString(),rc.get("count").toString());
			Iterator<Map.Entry<String, String>> it=mpt.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				mappingList.add(entry);
			}
		}
		return mappingList;
	}

	public static void getparametersThread(ServletContext sct) {
		try {
			List<DateVo> datelist = getTimelist();
			sct.setAttribute("datelist", datelist);
			List<Map.Entry> hotlist = getHotlist();
			sct.setAttribute("hotlist", hotlist);
		} catch (Exception e) {
			logger.error("getparametersThread: " + e.toString());
		}
	}

	public static Map getparametersInterface(ServletContext sct, int withdate) {
		Map p = new HashMap();
		try {
			List<DateVo> datelist = (List<DateVo>) sct.getAttribute("datelist_interface");
			if (datelist == null) {
				if (withdate == 1) {
					datelist = getTimelistInterface();
					p.put("datelist", datelist);
				}
			}else{
				if (withdate == 1) {
					p.put("datelist", datelist);
				}
			}

			List<Map.Entry> hotlist = (List<Map.Entry>) sct.getAttribute("hotlist_interface");
			if (hotlist == null) {
				hotlist = getHotlistInterface();
			}
			p.put("hotlist", hotlist);
			p.put("status", 0);
		} catch (Exception e) {
			logger.error("getparametersInterface: " + e.toString());
			p.put("errmsg", e.toString());
			p.put("status", -1);
		}
		return p;
	}

	public static void getparametersInterfaceUpdate(ServletContext sct) {
		try {
			List<DateVo> datelist = getTimelistInterface();
			sct.setAttribute("datelist_interface", datelist);
			List<Map.Entry> hotlist = getHotlistInterface();
			sct.setAttribute("hotlist_interface", hotlist);
		} catch (Exception e) {
			logger.error("getparametersInterfaceUpdate: " + e.toString());
		}
	}

	/**
	 * @标题: BaseAction.java
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：得到网页右侧列表数据
	 */
	public static String getparameters(ServletContext sct) {

		try {
			if (sct.getAttribute("datelist") == null) {
				List<DateVo> datelist = getTimelist();
				sct.setAttribute("datelist", datelist);
			}
			if (sct.getAttribute("hotlist") == null) {
				List<Map.Entry> hotlist = getHotlist();
				sct.setAttribute("hotlist", hotlist);
			}
		} catch (Exception e) {
			logger.error("getparameters: " + e.toString());
			return null;
		}
		return "ok";
	}

	public static SearchQueryP istodayRoot(SearchQueryP p){
		int istoday=PropKit.getInt("dataintoday");
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

	/**
	 * @标题: BaseAction.java
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：页面跳转
	 */
	public static String topage(HttpServletRequest request, int nowpage, int count, String pagename, String pagetype, Map searchzd) {
		if (count != 0) {
			try {
				SearchQueryP p = new SearchQueryP();
				p.setCount(count);
				p.setNowpage(nowpage);
				Map mp = new HashMap();
				//如果搜索条件不为空，加入搜索条件
				if (searchzd!=null) {
//					mp.put(searchzd, pagename);
					mp=searchzd;
				}
				p.setParameters(mp);
				p=istodayRoot(p);
				Map res = PgsqlKit.findByCondition(ClassKit.javClientClass, p);
				List<javsrc> srcs = (List<javsrc>) res.get("list");
				long pagecount = Long.valueOf(res.get("count").toString());
				request.setAttribute("srcs", srcs);
				request.setAttribute("pagecount", pagecount);//总共有多少条数据
				request.setAttribute("countsize", count);//总共显示多少条数据
				request.setAttribute("pagenum", nowpage);//当前页面

//				if (searchzd.equals("tabtype") || pagename.equals("newspage")) {
				if(hasTabType(pagename)){
					request.setAttribute("actionUrl", pagename);//要跳转的页面名称
				} else {
					request.setAttribute("actionUrl", "search");//要跳转的页面名称
				}
			} catch (Exception e) {
				logger.error("topage: " + e.toString());
				return "error";
			}
		}
		request.setAttribute("pagetype", pagetype);//页面类型
		request.setAttribute("tabtitle", pagename.toUpperCase());//页面标题
		request.setAttribute("tab", pagename);
		return "default";
	}

	public static String getConfigPath(HttpServletRequest request) {
		ServletContext sc = request.getSession().getServletContext();
		String realpath = sc.getRealPath("");
		realpath = realpath + "/WEB-INF/classes/config.txt";
		return realpath;
	}

	public static String getConfigUUIDPath(HttpServletRequest request) {
		ServletContext sc = request.getSession().getServletContext();
		String realpath = sc.getRealPath("");
		realpath = realpath + "/WEB-INF/classes/accessuuid.txt";
		return realpath;
	}

	public static String getDownBasePath(HttpServletRequest request) {
		String rootsavedir = PropKit.get("rootsavedir");
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/";
		basePath = basePath + rootsavedir;
		return basePath;
	}
	
	public static String formatLocalpath(String filepath){
		filepath = filepath.replace("//", "/");
		filepath = filepath.replace("\\\\", "\\");
		filepath = filepath.replace("/\\", "/");
		filepath = filepath.replace("\\/", "/");
		return filepath;
	}

	public static String getfilePath(HttpServletRequest request) {
		String rootsavedir = PropKit.get("rootsavedir");
		ServletContext sc = request.getSession().getServletContext();
		String realpath = sc.getRealPath("");
		String contentPath = sc.getContextPath().replace("/", "");
		String filepath = null;
		if (StringUtils.isNotBlank(contentPath)) {
			filepath = realpath.replace(contentPath, "");
		} 
		filepath = filepath + "/" + rootsavedir;
		filepath = formatLocalpath(filepath);
		return filepath;
	}

	/**
	 * @标题: BaseAction.java
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：得到实际路径
	 */
	public static String copypath(HttpServletRequest request, String onedir, String twodir, String filename) {
		String filepath = getfilePath(request);
		String nowdate = DateKit.getStringDateShort();
		filepath = filepath +"/"+nowdate + "/" + onedir + "/" + twodir + "/" + filename;
		filepath = formatLocalpath(filepath);
		return filepath;
	}

	public static String copypathold(HttpServletRequest request, String oldpath) {
		String rootsavedir = PropKit.get("rootsavedir");
		String filepath = getfilePath(request);
		filepath = filepath + "/"+oldpath.replace("/" + rootsavedir, "");
		filepath = formatLocalpath(filepath);
		return filepath;
	}

	public static String getwebfilePath(HttpServletRequest request) {
		String rootsavedir = PropKit.get("rootsavedir");
		ServletContext sc = request.getSession().getServletContext();
		String contentPath = sc.getContextPath().replace("/", "");
		String path = request.getContextPath();
		String filepath = null;
		if (StringUtils.isNotBlank(contentPath)) {
			filepath = path.replace(contentPath, rootsavedir);
		}
		filepath = filepath + "/" + rootsavedir;
		filepath = formatLocalpath(filepath);
		return filepath;
	}

	/**
	 * @标题: BaseAction.java
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：得到相对路径
	 */
	public static String webpath(HttpServletRequest request, String onedir, String twodir, String filename) {
		String nowdate = DateKit.getStringDateShort();
		String path = getwebfilePath(request);
		String webpath = path +"/"+nowdate + "/" + onedir + "/" + twodir + "/" + filename;
		webpath = formatLocalpath(webpath);
		return webpath;
	}

	public static String webpathold(HttpServletRequest request, String oldpath) {
		String rootsavedir = PropKit.get("rootsavedir");
		String nowdate = DateKit.getStringDateShort();
		String path = getwebfilePath(request);
		String webpath = path + "/"+oldpath.replace("/" + rootsavedir, "");
		webpath = formatLocalpath(webpath);
		return webpath;
	}

	public static String selectbt(String idtype,String sv,String id,javsrc jav,boolean flag){
		String res = null;
		if(idtype.equals("all")){
			res = getBtLinksAll(sv, id, jav,flag);
		}else {
			String[] typearray = idtype.split("--");
			res = getBtLinksByType(sv, typearray, id, jav,flag);
		}
		return res;
	}

	private static List filterSuccess(List<BtList> list) {
		if(list!=null&&list.size()>0){
			BtList bt = list.get(0);
			if (bt.getBtlink().equals("###")) {
				return new ArrayList<BtList>();
			} else {
				return list;
			}
		}else{
			return new ArrayList<BtList>();
		}


	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt
	 */
	private static String getBtLinksAll(String sv, String id,javsrc jav,boolean flag) {
		String types="t1--t2--t3";
		String[] typearray=types.split("--");
		String rejson=getBtLinksByType(sv,typearray,id,jav,flag);
		return rejson;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt
	 */
	private static String getBtLinksByType(String sv,String[] typearray ,String id,javsrc jav,boolean flag) {
		List<BtList> btlist=new ArrayList();
		try {
			for (int i=0;i<typearray.length;i++){
				String type=typearray[i];
				if(type.equals("t1")){
					List one = getBtNyaa(sv, id,flag);
					List bl1=filterSuccess(one);
					btlist=ListUtils.union(btlist, bl1);
				}
				if(type.equals("t2")){
					List two = getBtKitty(sv,id,flag);
					List bl2=filterSuccess(two);
					btlist=ListUtils.union(btlist, bl2);
				}
				if(type.equals("t3")){
					List three = getBtSow(sv,id,flag);
					List bl3=filterSuccess(three);
					btlist=ListUtils.union(btlist, bl3);
				}
			}

			if (btlist.size()==0) {
				btlist.add(errlistOne());
			}else{
				savebtlist(jav, id, btlist);
			}

		} catch (Exception e) {
			logger.error("getBtLinks: " + e.toString());
			btlist=new ArrayList();
			btlist.add(errlistOne(e.toString()));
		}

		String rejson=JsonKit.bean2JSON(btlist);
		return rejson;
	}

	private static List getlistpath(List<BtList> list) {
		List files = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			files.add(list.get(i).getBtlink());
		}
		return files;
	}

	private static List getlistname(List<BtList> list) {
		List files = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			files.add(list.get(i).getBtname());
		}
		return files;
	}

	private static BtList errlistOne() {
		BtList bl = new BtList();
		bl.setBtlink("###");
		bl.setBtname("暂无种子可以下载");
		return bl;
	}

	private static BtList errlistOne(String errmsg) {
		BtList bl = new BtList();
		bl.setBtlink("###");
		bl.setBtname(errmsg);
		return bl;
	}

	private static void savebtlist(javsrc jav,String id,List list) throws Exception {
		if(StringUtils.isNotBlank(id)) {
			List btfiles = getlistpath(list);
			List btnames = getlistname(list);
			jav.setBtfile(JsonKit.bean2JSON(btfiles));
			jav.setBtname(JsonKit.bean2JSON(btnames));
			jav.setId(id);
			String jsonbean = JsonKit.bean2JSON(jav);
			Map p = JsonKit.json2Bean(jsonbean, HashMap.class);
			PgsqlKit.updateById(ClassKit.javTableName, p);
		}
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt
	 */
	public static List getBtSow(String sv,String id,boolean  likeflag){
		List<BtList> btlist=new ArrayList();
		try {
			String bthost=PropKit.get("bthost3");
			String url=bthost+java.net.URLEncoder.encode(sv,"UTF-8");
			Map headers = HttpClientHelp.getDefaultHeader();
			String html=HttpClientHelp.doGet(url, null, headers, true);

			Document doc = Jsoup.parse(html);
			Elements news = doc.select(".data-list .row");
			if(news == null || news.isEmpty() || news.size()==0){
				SendEmail.sendWebChangeWarn(url);
			}
			if(news.size()>0){
				List<CompDate> elelist=new ArrayList();
				for (int i = 1; i < news.size(); i++) {
					Element one= (Element)news.get(i);
					String date=one.child(2).text();
					String btname=one.child(0).attr("title");
					btname=btname.toLowerCase();
					sv=sv.toLowerCase();
					if( likeflag||(btname.contains(sv)||btname.replace("-","").contains(sv.replace("-","")))) {
						CompDate cd=new CompDate();
						cd.setIndex(i);
						cd.setDate(date);
						cd.setEle(one);
						elelist.add(cd);
					}
				}
				Collections.sort(elelist);

				if(StringUtils.isNotBlank(id)) {
					int ed = elelist.size() > 3 ? 3 : elelist.size();
					for (int i = 0; i < ed; i++) {
						BtList bl = getbtlist3(elelist.get(i).getEle());
						btlist.add(bl);
					}
				}else{
					if(elelist.size()>=1) {
						BtList bl0 = new BtList();
						bl0.setBtlink("#");
						bl0.setBtname("BtSow");
						btlist.add(bl0);
					}else{
						btlist.add(errlistOne());
					}
					for (int i = 0; i < elelist.size(); i++) {
						BtList bl = getbtlist3(elelist.get(i).getEle());
						btlist.add(bl);
					}
				}
			}else{
				btlist.add(errlistOne());
			}
		}catch (Exception e) {
			logger.error("getBtSow: " + e.toString());
			btlist=new ArrayList();
			btlist.add(errlistOne(e.toString()));
		}
		return btlist;
	}

	private static BtList getbtlist3(Element one) throws Exception{
		String btname=one.child(0).attr("title");
		String baseurl=one.child(0).attr("href");
		Map headers = HttpClientHelp.getDefaultHeader();
		String basehtml=HttpClientHelp.doGet(baseurl, null, headers, true);
		Document basedoc = Jsoup.parse(basehtml);
		Elements basenews = basedoc.select(".magnet-link");
		String btlink=basenews.get(0).text();
		BtList bl=new BtList();
		bl.setBtlink(btlink);
		bl.setBtname("magnet:?xt="+btname);
		return bl;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt
	 */
	public static List getBtKitty(String sv,String id,boolean  likeflag){
		List<BtList> btlist=new ArrayList();
		try {
			String bthost=PropKit.get("bthost2");
			String url=bthost+java.net.URLEncoder.encode(sv,"UTF-8")+"/";
			Map headers = HttpClientHelp.getDefaultHeader();
			String html=HttpClientHelp.doGet(url, null, headers, true);

			Document doc = Jsoup.parse(html);
			Elements news = doc.select("#archiveResult tr");
			if(news == null || news.isEmpty() || news.size()==0){
				SendEmail.sendWebChangeWarn(url);
			}
			if(news.size()>0){
				if(StringUtils.isNotBlank(news.get(1).child(1).text())){
					List<CompDate> elelist=new ArrayList();
					for (int i = 1; i < news.size(); i++) {
						Element one= (Element)news.get(i);
						String date=one.child(2).text();
						String btname=one.child(0).text();
						btname=btname.toLowerCase();
						sv=sv.toLowerCase();
						if( likeflag||(btname.contains(sv)||btname.replace("-","").contains(sv.replace("-","")))) {
							CompDate cd=new CompDate();
							cd.setIndex(i);
							cd.setDate(date);
							cd.setEle(one);
							elelist.add(cd);
						}
					}
					Collections.sort(elelist);

					if(StringUtils.isNotBlank(id)) {
						int ed = elelist.size() > 3 ? 3 : elelist.size();
						for (int i = 0; i < ed; i++) {
							BtList bl = getbtlist2(elelist.get(i).getEle());
							btlist.add(bl);
						}
					}else{
						if(elelist.size()>=1) {
							BtList bl0 = new BtList();
							bl0.setBtlink("#");
							bl0.setBtname("TorrentKitty");
							btlist.add(bl0);
						}else{
							btlist.add(errlistOne());
						}
						for (int i = 0; i < elelist.size(); i++) {
							BtList bl = getbtlist2(elelist.get(i).getEle());
							btlist.add(bl);
						}
					}
				}else{
					btlist.add(errlistOne());
				}
			}else{
				btlist.add(errlistOne());
			}
		}catch (Exception e) {
			logger.error("getBtKitty: " + e.toString());
			btlist=new ArrayList();
			btlist.add(errlistOne(e.toString()));
		}
		return btlist;
	}

	private static BtList getbtlist2(Element one) throws Exception{
		String btname=one.child(0).text();
		String btlink=one.child(3).child(1).attr("href");
		BtList bl=new BtList();
		bl.setBtlink(btlink);
		bl.setBtname("magnet:?xt="+btname);
		return bl;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt
	 */
	public static List getBtNyaa(String sv,String id,boolean  likeflag){
		List<BtList> btlist=new ArrayList();
		try {
			String bthost=PropKit.get("bthost");
			String url=bthost+"?page=search&cats=0_0&filter=0&term="+java.net.URLEncoder.encode(sv,"UTF-8");
			Map headers = HttpClientHelp.getDefaultHeader();
			String html=HttpClientHelp.doGet(url, null, headers, true);

			Document doc = Jsoup.parse(html);
			Elements news = doc.select(".tlistrow");
			if(news == null || news.isEmpty() || news.size()==0){
				SendEmail.sendWebChangeWarn(url);
			}
			if(news.size()>0){
				List<CompDls> elelist=new ArrayList();
				for (int i = 0; i < news.size(); i++) {
					Element one= (Element)news.get(i);
					Elements dlse=one.getElementsByClass("tlistdn");
					int dls=Integer.parseInt(dlse.get(0).html());
					Elements tlistname=one.getElementsByClass("tlistname");
					String btname=tlistname.get(0).getElementsByTag("a").text();
					btname=btname.toLowerCase();
					sv=sv.toLowerCase();
					if( likeflag||(btname.contains(sv)||btname.replace("-","").contains(sv.replace("-","")))) {
						CompDls cd=new CompDls();
						cd.setIndex(i);
						cd.setDls(dls);
						cd.setEle(one);
						elelist.add(cd);
					}
				}
				Collections.sort(elelist);

				if(StringUtils.isNotBlank(id)){
					if(elelist.size()>=3){
						int bg=elelist.size()-1;
						int ed=elelist.size()-3;
						for (int i =bg ; i >=ed; i--) {
							BtList bl=getbtlist(elelist.get(i).getEle());
							btlist.add(bl);
						}
					}else{
						for (int i =0 ; i<elelist.size(); i++) {
							BtList bl=getbtlist(elelist.get(i).getEle());
							btlist.add(bl);
						}
					}
				}else{
					if(elelist.size()>=1) {
						BtList bl0 = new BtList();
						bl0.setBtlink("#");
						bl0.setBtname("Nyaa");
						btlist.add(bl0);
					}else{
						btlist.add(errlistOne());
					}
					for (int i =0 ; i<elelist.size(); i++) {
						BtList bl=getbtlist(elelist.get(i).getEle());
						btlist.add(bl);
					}
				}
			}else{
				Elements basenews = doc.select(".content");
				Elements torlinks=basenews.get(0).getElementsByClass("viewdownloadbutton");
				if(torlinks.size()>0){
					String btlink=torlinks.get(0).getElementsByTag("a").attr("href");
					String btname=basenews.get(0).getElementsByClass("viewtorrentname").get(0).text();
					BtList bl=new BtList();
					bl.setBtlink(btlink);
					bl.setBtname(btname);
					btlist.add(bl);
				}else{
					btlist.add(errlistOne());
				}
			}

		} catch (Exception e) {
			logger.error("getBtNyaa: " + e.toString());
			btlist=new ArrayList();
			btlist.add(errlistOne(e.toString()));
		}
		return btlist;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt列表
	 */
	private static BtList getbtlist(Element one) throws Exception{
		String protocol="http:";
		Elements tlistname=one.getElementsByClass("tlistname");
		String baseurl=tlistname.get(0).getElementsByTag("a").attr("href");
		baseurl=protocol+baseurl;
		String btname=tlistname.get(0).getElementsByTag("a").text();
		Map headers = HttpClientHelp.getDefaultHeader();
		String basehtml=HttpClientHelp.doGet(baseurl, null, headers, true);
		Document basedoc = Jsoup.parse(basehtml);
		Elements basenews = basedoc.select(".viewdownloadbutton");
		String btlink=basenews.get(0).getElementsByTag("a").attr("href");
		btlink=protocol+btlink;
		BtList bl=new BtList();
		bl.setBtlink(btlink);
		bl.setBtname(btname+".torrent");
		return bl;		
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 通过标题截取识别码
	 */
	public static String getSbmByTitle(String title){
		String rex="\\b(\\w+\\s)*\\w{2,}(-|_|\\s)([a-z]{2,}|[0-9]{3,})(\\s\\w+)*";
		Pattern pattern = Pattern.compile(rex,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(title);
		if(matcher.find()){
			title=matcher.group(0);
		}
		return title;
	}

	public static String replace20(String url){
		String bg="";
		String ed="";
		if(url.indexOf("?")>-1){
			bg=url.substring(0,url.indexOf("?")+1);
			ed=url.substring(url.indexOf("?")+1);
		}else{
			bg=url.substring(0,url.lastIndexOf("/")+1);
			ed=url.substring(url.lastIndexOf("/")+1);
		}
		Pattern p = Pattern.compile("\\s+");
		Matcher m = p.matcher(ed);
		ed=m.replaceAll("%20");
		return bg+ed;
	}

	public static String replace20All(String url){
		Pattern p = Pattern.compile("\\s+");
		Matcher m = p.matcher(url);
		url=m.replaceAll("%20");
		return url;
	}


	/**
	 * @author Meteor
	 * @Title
	 * @category 获取有码资源
	 */
	public static int getJavmoo(String oldtitles,String searchval,String num) throws Exception {
		String censoredhost=PropKit.get("censoredhost");
		String typename="censored";
		String url="";
		//根据搜索条件选择页面地址
		if(StringUtils.isBlank(searchval)){
			url=censoredhost+"released/page/"+num;
		}else{
			if(searchval.contains("#")){
				searchval=searchval.split("#")[1];
				url = censoredhost + searchval +"/"+ num;
			}else {
				searchval = java.net.URLEncoder.encode(searchval.toLowerCase(), "UTF-8");
				url = censoredhost + "search/" + searchval + "/page/" + num;
			}
		}
		//拉取页面得到doc对象
		Map head = HttpClientHelp.getDefaultHeader();
		String ref=censoredhost.replace("/cn/","");
		head.put("Referer", censoredhost);
		String html=HttpClientHelp.doGet(url,null,head,true);
		Document doc = Jsoup.parse(html);
		Elements news = doc.select(".item");
		if(news == null || news.isEmpty() || news.size()==0){
			SendEmail.sendWebChangeWarn(url);
		}
		for (int i = 0; i <news.size(); i++) {
			javsrc bean = new javsrc();
			Element one = news.get(i);

			/**得到识别码**/
			Elements dates = one.getElementsByTag("date");
			String sbm=dates.get(0).text().trim();
			if(sbm.contains("BD-")){
				continue;
			}
			bean.setSbm(sbm);

			Elements img = one.getElementsByTag("img");
			String title = img.get(0).attr("title");
			title=sbm+" "+title;
			/**得到标题**/
			//如果在标题列表中能检索到，
			title=title.replaceAll("'","''");
			boolean flag=getSrcTitle(searchval,title);
			boolean flag2=checkBlockKey(title,typename);
			if (flag || flag2) {
				continue;
			}
			bean.setTitle(title);

			/**得到时间**/
			String date = dates.get(1).text().trim();
			date=DateKit.KsrqString(date);
			bean.setTimes(date);

			/**得到子链接**/
			Elements a = one.getElementsByTag("a");
			String blink=a.get(0).attr("href");

			Thread.sleep(3000);
			flag = getJavsChild(blink, bean, typename);
			if (!flag) {
				continue;
			}

			bean.setTabtype(typename);
			bean.setIsdown("0");
			bean.setId(StringKit.getMongoId());
			PgsqlKit.save(ClassKit.javTableName, bean);
		}
		return 0;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取无码资源
	 */
	public static int getJavlog(String oldtitles,String searchval,String num) throws Exception {
		String uncensoredhost=PropKit.get("uncensoredhost");
		String typename="uncensored";
		String url="";
		//根据搜索条件选择页面地址
		if(StringUtils.isBlank(searchval)){
			url=uncensoredhost+"page/"+num;
		}else{
			if(searchval.contains("#")){
				searchval=searchval.split("#")[1];
				url = uncensoredhost + searchval +"/"+ num;
			}else {
				searchval = java.net.URLEncoder.encode(searchval.toLowerCase(), "UTF-8");
				url = uncensoredhost + "search/" + searchval + "/page/" + num;
			}
		}
		//拉取页面得到doc对象
		Map head = HttpClientHelp.getDefaultHeader();
		String ref=uncensoredhost.replace("/cn/","");
		head.put("Referer", ref);
		String html=HttpClientHelp.doGet(url,null,head,true);
		Document doc = Jsoup.parse(html);
		Elements news = doc.select(".item");
		if(news == null || news.isEmpty() || news.size()==0){
			SendEmail.sendWebChangeWarn(url);
		}
		for (int i = 0; i <news.size(); i++) {
			javsrc bean = new javsrc();
			Element one = news.get(i);

			/**得到识别码**/
			Elements dates = one.getElementsByTag("date");
			String sbm=dates.get(0).text().trim();
			if(sbm.contains("BD-")){
				continue;
			}
			bean.setSbm(sbm);

			Elements img = one.getElementsByTag("img");
			String title = img.get(0).attr("title");
			title=sbm+" "+title;
			/**得到标题**/
			//如果在标题列表中能检索到，
			title=title.replaceAll("'","''");
			boolean flag=getSrcTitle(searchval,title);
			boolean flag2=checkBlockKey(title,typename);
			if (flag || flag2) {
				continue;
			}
			bean.setTitle(title);

			/**得到时间**/
			String date = dates.get(1).text().trim();
			date=DateKit.KsrqString(date);
			bean.setTimes(date);

			/**得到子链接**/
			Elements a = one.getElementsByTag("a");
			String blink=a.get(0).attr("href");
			flag = getJavsChild(blink, bean, typename);
			if (!flag) {
				continue;
			}

			bean.setTabtype(typename);
			bean.setIsdown("0");
			bean.setId(StringKit.getMongoId());
			PgsqlKit.save(ClassKit.javTableName, bean);
		}
		return 0;
	}

	private static boolean getJavsChild(String blink,javsrc bean,String typename) throws Exception {
		if(!blink.startsWith("http")){
			blink="http:"+blink;
		}
		String ref=null;
		if(typename.equals("uncensored")){
			 ref=PropKit.get("uncensoredhost");
		}else{
			 ref=PropKit.get("censoredhost");
		}
		Map head = HttpClientHelp.getDefaultHeader();
		ref=ref.replace("/cn/","");
		head.put("Referer", ref);
		String html=HttpClientHelp.doGet(blink,null,head,true);

//		String html=MultitHttpClient.get(blink);
		Document doc = Jsoup.parse(html);
		/**循环得到标签**/
		List tags = new ArrayList();
		tags.add(typename.toUpperCase());
		Elements info=doc.getElementsByClass("info");
		Elements infop=info.get(0).getElementsByTag("p");
		for(Element p:infop){
			if(!p.html().contains("class=\"header\"")&&!p.className().contains("header")&&StringUtils.isNotBlank(p.text())){
				if(infop.indexOf(p)==(infop.size()-1)){
					Elements plist=p.getElementsByTag("a");
					for(Element pl:plist){
						boolean flag = checkBlockKey(pl.text(), typename);
						if(flag){
							return false;
						}
						tags.add(pl.text().toUpperCase());
					}
				}else {
					boolean flag = checkBlockKey(p.text(), typename);
					if(flag){
						return false;
					}
					tags.add(p.text().toUpperCase());
				}
			}
		}
		Elements avas=doc.getElementsByClass("avatar-box");
		for(Element p:avas){
			tags.add(p.text());
		}
		bean.setTags(JsonKit.bean2JSON(tags));
		/**得到图片地址**/
		Elements imgs=doc.getElementsByClass("bigImage");
		String img=imgs.get(0).attr("href");
		if(!img.startsWith("http")){
			img="http:"+img;
		}
		if (img.contains(imgReplcKey)) {
			String newimg = getBase64Img(img);
			if (StringUtils.isNotBlank(newimg)) {
				img = newimg;
				bean.setIsstar("1");
			}
		}
		bean.setImgsrc(img);
		return true;
	}


	/**
	 * @author Meteor
	 * @Title
	 * @category 得到欧美资源，并转换一部分到无码
	 */
	public static int getPornleech(String oldtitles,String searchval,String num) throws Exception {
			String westporn = PropKit.get("westporn");
			String typename = "westporn";
			String url = "";
			//根据搜索条件选择页面地址
			int newnum = Integer.valueOf(num);
			newnum=newnum-1;
			if (StringUtils.isBlank(searchval)) {
				url = westporn + "index.php?page=torrents&search=&options=0&active=0&category=64;65;66;77&pages=" + newnum;
			} else {
				if(searchval.contains("#")){
//					String[] sps=searchval.split("#");
					/** 形如 0#0#0#0 ,不要的用0来填充**/
					String[] sps=StringUtils.split(searchval,"#");
					String chooseParam=sps[0];//0就是用category,1就是用genre,这是两个互斥参数
					String category=sps[2].equals("0")?"":sps[2];
					String genre=sps[3].equals("0")?"":sps[3];
					searchval=sps[1].equals("0")?"":sps[1];
					if(chooseParam.equals("0")){
						url = westporn + "index.php?page=torrents&search=" + searchval + "&options=0&active=0&category="+category+"&pages=" + newnum;
					}else if(chooseParam.equals("1")){
						url = westporn + "index.php?page=torrents&search=" + searchval + "&options=0&active=0&genre="+genre+"&pages=" + newnum;
					}else{
						url = westporn + "index.php?page=torrents&search=" + searchval + "&options=0&active=0&pages=" + newnum;
					}
				}else {
					searchval = java.net.URLEncoder.encode(searchval.toLowerCase(), "UTF-8");
					url = westporn + "index.php?page=torrents&search=" + searchval + "&options=0&active=0&category=64;65;66;77&pages=" + newnum;
				}
			}
			//拉取页面得到doc对象
//			String html = MultitHttpClient.post(url);
			String html = HttpUtilKit.get503Page(url);
			Document doc = Jsoup.parse(html);
			Elements tabs = doc.select("table[class=lista][width=100%]");
			if(tabs == null || tabs.isEmpty() || tabs.size()==0){
				SendEmail.sendWebChangeWarn(url);
			}
			if(!tabs.isEmpty() && tabs.size()>0) {
				Elements trs = tabs.get(tabs.size()-1).getElementsByTag("tr");
				String host = westporn;
				for (int i = 1; i < trs.size(); i++) {
					javsrc bean = new javsrc();
					Element one = trs.get(i);
					Elements as = one.select("a[onmouseover]");
					Element a = as.get(0);
					String title = a.text();
					if (title.toUpperCase().contains("CENSORED") || title.toUpperCase().contains("UNCENSORED")) {
						continue;
					}
					/**得到标题**/
					//如果在标题列表中能检索到，标记为已存在的元素
					title = title.replaceAll("'", "''");
					boolean flag = getSrcTitle(searchval, title);
					boolean flag2 = checkBlockKey(title, typename);
					if (flag || flag2) {
						continue;
					}
					bean.setTitle(title);
					/**得到影片类型**/
					Elements img = one.select("img");
					String movieType = img.get(0).attr("title");
					movieType="W_"+movieType;

					String blink = host + a.attr("href");
					flag = getPornleechChild(blink, bean, typename, host, title,movieType);
					if (!flag) {
						continue;
					}
					bean.setIsdown("0");
					bean.setId(StringKit.getMongoId());
					PgsqlKit.save(ClassKit.javTableName, bean);
				}
			}
		return 0;
	}

	private static boolean getPornleechChild(String blink,javsrc bean,String typename,String host,String title,String mvType) throws Exception {
//		String html=MultitHttpClient.post(blink);
		String html = HttpUtilKit.get503Page(blink);
		Document doc = Jsoup.parse(html);
		Elements tabs = doc.select("table[class=lista]");
		if(tabs.isEmpty()){
			return false;
		}
		Elements trs=tabs.get(0).getElementsByTag("tr");
		List tags = new ArrayList();
		tags.add(mvType);
		for (int i=0;i<trs.size();i++){
			Element tr=trs.get(i);
			Elements tds=tr.getElementsByTag("td");
			if(tds!=null&&tds.size()>1) {
				String head = tds.get(0).text();
				head = head.toLowerCase();
				Element thistd = tds.get(1);
				if (head.equals("genre")) {
					Elements as=thistd.getElementsByTag("a");
					for (Element a:as){
						boolean flag = checkBlockKey(a.text(), typename);
						if(flag){
							return false;
						}
						tags.add(a.text());
					}
					break;
				}
			}
		}
		for (int i=0;i<trs.size();i++){
			Element tr=trs.get(i);
			Elements tds=tr.getElementsByTag("td");
			if(tds!=null&&tds.size()>1) {
				String head = tds.get(0).text();
				head = head.toLowerCase();
				Element thistd = tds.get(1);
				if (head.equals("name")) {
					List torrentnames = new ArrayList();
					torrentnames.add(title);
					bean.setBtname(JsonKit.bean2JSON(torrentnames));
				}
				if (head.equals("torrent")) {
					String torrent = host + thistd.getElementsByTag("a").get(0).attr("href");
					List torrents = new ArrayList();
					torrents.add(torrent);
					bean.setBtfile(JsonKit.bean2JSON(torrents));
				}
				if (head.equals("image")) {
					String img = host + thistd.getElementsByTag("img").attr("src");
					String newimg = get503Base64Img(img);
//					String newimg = getBase64Img(img);
					if (StringUtils.isNotBlank(newimg)) {
						img = newimg;
						bean.setIsstar("1");
					}
					bean.setImgsrc(img);
				}
				if (head.equals("description")) {
					String description = thistd.text();
					if (StringUtils.isBlank(description)) {
						return false;
					} else {
						bean.setSbm(description.toUpperCase());
					}
					tags.add(typename.toUpperCase());
					bean.setTabtype(typename);
				}
				if (head.equals("adddate")) {
					String datetime = thistd.text();
					datetime=DateKit.fmtDateString(datetime);
					bean.setTimes(datetime);
				}
			}
		}
		if(tags.size()>0) {
			bean.setTags(JsonKit.bean2JSON(tags));
		}
		return true;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 查找是否有相同的标题
	 */
	public static boolean getSrcTitle(String searchval,String title) throws Exception{
		SearchQueryP sp=new SearchQueryP();
		sp.setCount(0);
		sp.setNowpage(0);
		Map mp=new HashMap();
		mp.put("tags", searchval.toUpperCase());
		mp.put("title", title);
		sp.setParameters(mp);
		List<javsrc> searchlist = new ArrayList<javsrc>();
		Map res=PgsqlKit.findByCondition(ClassKit.javClass, sp);
		searchlist = (List<javsrc>) res.get("list");
		if(searchlist!=null&&searchlist.size()>0){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 校验屏蔽关键字
	 */
	public static boolean checkBlockKey(String title,String tabtype) throws Exception{
		Prop pp=PropKit.getProp("blockkey.txt");
		Iterator it= pp.getProperties().entrySet().iterator();
		boolean flag=false;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String value = (String) entry.getValue();
			String lowtitle = title.toLowerCase();
			if(lowtitle.contains(value.toLowerCase())){
				flag=true;
				break;
			}
		}
		return flag;
	}

	private static String get503Base64Img(String imgurl){
//		if(true){
//			return getBase64Img(imgurl);
//		}
		String img = "";
		imgurl = PageKit.replace20All(imgurl);
		String res =HttpUtilKit.get503Resource(imgurl);
		Map<String, String> p = JsonKit.json2Map(res);
		if (p.get("status").equals("0")) {
			img = SecurityEncodeKit.GetImageStr(p.get("filepath"));
			if (StringUtils.isNotBlank(img)) {
				img = PageKit.getimgBase64Tip() + img;
			}
		}
		return img;
	}

	private static String getBase64Img(String imgurl){
		String tmpdir= MainConfig.tmpsavedir;//PropKit.get("tmpsavedir");
		String img = "";
		Map head = HttpClientHelp.getDefaultHeader();
//		String ref=PropKit.get("uncensoredhost");
//		head.put("Referer", ref);
		imgurl = PageKit.replace20All(imgurl);
		String res = HttpClientHelp.getFileDownByPath(imgurl, tmpdir, 1, head,true);
		Map<String, String> p = JsonKit.json2Map(res);
		if (p.get("status").equals("0")) {
			img = SecurityEncodeKit.GetImageStr(p.get("filepath"));
			if (StringUtils.isNotBlank(img)) {
				img = PageKit.getimgBase64Tip() + img;
			}
		}else{
			if(p.get("errmsg").contains("404")){
				img=imgurl.replace(imgReplcKey,"");
			}
		}
		return img;
	}

	public static void getAndUpdate503(javsrc one,String img) throws Exception {
		String newimg = get503Base64Img(img);
		if (StringUtils.isNotBlank(newimg)) {
			logger.error("503图片转换成功：" + img);
			img = newimg;
			one.setIsstar("1");
		}
		one.setImgsrc(img);
		Map pp = JsonKit.json2Map(JsonKit.bean2JSON(one));
		PgsqlKit.updateById(ClassKit.javTableName, pp);
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 下载图片并转换为base64编码(转换欧美的503图片）
	 */
	public static void tobase64By503() throws Exception {
		SearchQueryP sp=new SearchQueryP();
		Map p=new LinkedHashMap();
		p.put("tabtype","westporn");
		p.put("ISNULL_isstar","000");//有去空值得判断，所以要填充000
		p.put("LIKE_imgsrc","/torrentimg/");
		sp.setParameters(p);
		sp.setNowpage(1);
		sp.setCount(1000);
		List<javsrc> js = PgsqlKit.findByConditionAll(ClassKit.javClass, sp);
		if(js!=null && js.size()>20) {
			PageManager pm=new PageManager();
			PageRun pr=new PageRun(pm);
			pr.doitImg(20,js,"westpronImg",null);
		}else{
			for (javsrc one : js) {
				String img = one.getImgsrc();
				getAndUpdate503(one,img);
			}
			logger.error("转换图片为Base64成功");
		}
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 下载图片并转换为base64编码（针对来自uncensored的图片报403的应对措施）
	 */
	public static void tobase64() throws Exception {
		tobase64Https("censored");
		tobase64Https("uncensored");
		logger.error("转换图片为Base64成功");
	}

	private static void tobase64Https(String tabtype) throws Exception {
		SearchQueryP sp=new SearchQueryP();
		Map p=new LinkedHashMap();
		p.put("tabtype",tabtype);
		p.put("ISNULL_isstar","000");
		p.put("LIKE_imgsrc",imgReplcKey);
		sp.setParameters(p);
		List<javsrc> js = new ArrayList<javsrc>();
		Map res=PgsqlKit.findByCondition(ClassKit.javClass, sp);
		js = (List<javsrc>) res.get("list");
		for (javsrc one:js){
			String img = one.getImgsrc();
			String newimg=getBase64Img(img);
			if(StringUtils.isNotBlank(newimg)){
				img=newimg;
				one.setIsstar("1");
			}
			one.setImgsrc(img);
			Map pp=JsonKit.json2Map(JsonKit.bean2JSON(one));
			PgsqlKit.updateById(ClassKit.javTableName, pp);
		}
	}

	public static void updateCache(ServletContext sct){
		updateProp();
		getparametersThread(sct);
		getparametersInterfaceUpdate(sct);
	}

	public static void updateProp(){
		PropKit.clear();
		PropKit.use("config.txt");
		PropKit.use("accessuuid.txt");
		PropKit.use("blockkey.txt");
		PropKit.use("contenttype.properties");
	}

	public static void delrepeated(){
		String sql="delete from javsrc j1 where j1.id in (select a.id from javsrc a where a.title in  (select title from javsrc group by title having count(*) > 1)) " +
				"and j1.id not in (select max(id) from javsrc group by title having count(*) > 1)";
		PgsqlKit.excuteSql(sql);
		logger.error("清除javsrc重复项完毕");
	}

	public static void delrepeatedErrpage(){
		String sql="delete from errpage where id not in " +
				"(select max(id) from errpage GROUP BY TYPE,num,searchkey)";
		PgsqlKit.excuteSql(sql);
		logger.error("清除errpage重复项完毕");
	}

	public static void setpc(HttpServletRequest request){
		boolean flag=ispc(request);
		if(flag) {
			request.setAttribute("ispc", "1");
		}else{
			request.setAttribute("ispc", "0");
		}
	}

	public static boolean ispc(HttpServletRequest request){
		String userAgentInfo = request.getHeader("User-Agent");
		String[] Agents = new String[]{"Android", "iPhone","SymbianOS", "Windows Phone","iPad", "iPod"};
		boolean flag = true;
		for (int v = 0; v < Agents.length; v++) {
			if (userAgentInfo.contains(Agents[v])) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public static String magnetToXunleiLink(String url){
		try {
			url = java.net.URLDecoder.decode(url,"UTF-8");
			url = url.replace("magnet:","http://xxx");
			URL u=new URL(url);
			String query=u.getQuery();
			String[] querys=query.split("&");
			String hash=null;
			String bg1=null;
			String bg2=null;
			for (int i=0;i<querys.length;i++){
				String q=querys[i];
				if(q.startsWith("xt=")){
					hash=q.replace("xt=urn:btih:","").toUpperCase();
					bg1=hash.substring(0,2);
					int hashlength=hash.length();
					bg2=hash.substring(hashlength-2,hashlength);
					break;
				}
			}
			String XunleiLink=null;
			if(StringUtils.isNotBlank(hash)){
				XunleiLink="http://bt.box.n0808.com"+"/"+bg1+"/"+bg2+"/"+hash+".torrent";
			}
			return XunleiLink;
		} catch (Exception e) {
			return null;
		}
	}

	public static String magnetToTorcacheLink(String url){
		try {
			url = java.net.URLDecoder.decode(url,"UTF-8");
			url = url.replace("magnet:","http://xxx");
			URL u=new URL(url);
			String query=u.getQuery();
			String[] querys=query.split("&");
			String hash=null;
			for (int i=0;i<querys.length;i++){
				String q=querys[i];
				if(q.startsWith("xt=")){
					hash=q.replace("xt=urn:btih:","").toUpperCase();
					break;
				}
			}
			String TorcacheLink=null;
			if(StringUtils.isNotBlank(hash)){
				TorcacheLink="http://torcache.net/torrent/"+hash+".torrent";
			}
			return TorcacheLink;
		} catch (Exception e) {
			return null;
		}
	}


	public static String  downloadWithStatus(String url,String filedest,String errcode){
		File f = new File(filedest);
		if (!f.exists()) {
			String res = HttpClientHelp.getFileDownByPathFull(url, filedest,false);
			Map resp = JsonKit.json2Map(res);
			Object errmsg=resp.get("errmsg");
			if(errmsg!=null && errmsg.toString().contains("404")){
				return "404";
			}
			if (resp.get("status").equals("-1")) {
				return errcode;
			} else if (resp.get("status").equals("-2")) {
				return errcode;
			}
		}
		return  "0";
	}

	public static void testHaveNewHost(){
		String serverhost= PropKit.get("serverhost");
		String path=serverhost+"checkhost";
		try {
			HttpClientHelp.doGet(path,false);
		} catch (Exception e) {
			logger.error("替换url任务失败:"+e.toString());
		}
	}
	
	public static String getCaoLiu(){
		Map head=new HashMap();
		head.put("Content-Type","application/x-www-form-urlencoded");
		Map params=new HashMap();
		params.put("a", "g");
		params.put("v", "0");
		String url=PropKit.get("clweb");
		String res= null;
		try {
			res = HttpClientHelp.doPost(url, params, head);
		} catch (Exception e) {
			res = "获取草榴网址异常";
		}
		return res;
	}
	
	public static boolean classicalTobase64(HttpServletRequest request){ 
		try {
			String rootsavedir = PropKit.get("rootsavedir");
			SearchQueryP p = new SearchQueryP();
//			p.setCount(500);
//			p.setNowpage(1);
			Map mp = new HashMap();
			mp.put("tabtype","classical");
			mp.put("isstar","1");
			p.setParameters(mp);
			Map res = PgsqlKit.findByCondition(ClassKit.javClass, p);
			List<javsrc> srcs = (List<javsrc>) res.get("list");
			logger.error("待转换数据数量："+res.get("select"));
			String localpath = PageKit.getfilePath(request).replace(rootsavedir, "");
			for (Iterator iterator = srcs.iterator(); iterator.hasNext();) {
				javsrc javsrc = (javsrc) iterator.next();		
				boolean isTo64img = false;
				boolean isTo64tor = false;
				//存在的记录直接跳过
				Map ps =new HashMap();
				ps.put("srcid", javsrc.getId());
				List l= PgsqlKit.findall(ClassKit.javtorClass, ps);
				if(l.size()!=0){
					isTo64tor = true;
				}
				//转换图片
				String imgpath = localpath + javsrc.getImgsrc();
				imgpath = PageKit.formatLocalpath(imgpath);
				if(javsrc.getImgsrc()!=null && javsrc.getImgsrc().startsWith("/"+rootsavedir)){
					String img = SecurityEncodeKit.GetImageStr(imgpath);
					if (StringUtils.isNotBlank(img)) {
						img = PageKit.getimgBase64Tip() + img;
						javsrc.setImgsrc(img);
						isTo64img = true;
					}
				}
				//转换种子
				String torpath = null;
				if(!isTo64tor){
					List<String> listtor = JsonKit.json2List(javsrc.getBtfile());
					if(listtor!=null && javsrc.getBtfile().contains("/"+rootsavedir)){
						List<String> newlisttor = new ArrayList<String>();
						for (String tor:listtor) {
							torpath = localpath + tor;
							torpath = PageKit.formatLocalpath(torpath);
							String torstr = SecurityEncodeKit.GetImageStr(torpath);
							if (StringUtils.isNotBlank(torstr)) {
								javtor javtor = new javtor(javsrc.getId(),torstr);
								PgsqlKit.save(ClassKit.javtorTableName,javtor);
								newlisttor.add(PageKit.gettorBase64Key()+javtor.getId());
								isTo64tor = true;
							}
						}
						javsrc.setBtfile(JsonKit.bean2JSON(newlisttor));
					}
				}
				if(!isTo64img && !isTo64tor){
					logger.error(javsrc.getId()+"转换失败,等待下一次转换。");
				}else{
//					if(isTo64img && isTo64tor){
//						javsrc.setIsstar("2");
//					}
					javsrc.setIsstar("2");
					Map pp=JsonKit.json2Map(JsonKit.bean2JSON(javsrc));
					PgsqlKit.updateById(ClassKit.javTableName, pp);
					if(isTo64img){
						new FileOperateKit().delNotEmptyFolder(imgpath);
					}else{
						new FileOperateKit().delNotEmptyFolder(torpath);
					}
				}
			}

			String rootpath = PageKit.getfilePath(request);
			new FileOperateKit().loopDelEmptyFolder(rootpath);
			logger.error("转换成功");
			return true;
		} catch (Exception e) {
			logger.error("转换资源出错",e);
			return false;
		}
	}
}