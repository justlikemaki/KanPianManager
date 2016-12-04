package com.meteor.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.monogodb.MongoKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.meteor.common.MainConfig;
import com.meteor.interceptor.LoginCheck;
import com.meteor.kit.ClassKit;
import com.meteor.kit.CzExlKit;
import com.meteor.kit.DateKit;
import com.meteor.kit.FileOperateKit;
import com.meteor.kit.JsonKit;
import com.meteor.kit.PageKit;
import com.meteor.kit.PgsqlKit;
import com.meteor.kit.SecurityEncodeKit;
import com.meteor.kit.StringKit;
import com.meteor.kit.WorkPool;
import com.meteor.kit.getpage.PageManager;
import com.meteor.kit.getpage.PageRun;
import com.meteor.kit.http.HttpClientHelp;
import com.meteor.kit.http.MultitDownload;
import com.meteor.kit.http.TaskThreadManagers;
import com.meteor.model.po.errpage;
import com.meteor.model.po.javsrc;
import com.meteor.model.po.javtor;
import com.meteor.model.vo.InExl;
import com.meteor.model.vo.SearchQueryP;
import com.mongodb.BasicDBList;

@Before(LoginCheck.class)
public class BaseAction extends Controller {
	
	private final Logger logger = LoggerFactory.getLogger(BaseAction.class);

	/**
	 * @author Meteor
	 * @Title
	 * @category 删除本地数据库的数据
	 */
	public void delData() throws Exception {
		PgsqlKit.deleteCollectionAllData("companys");
		PgsqlKit.deleteCollectionAllData("javsrc");
		renderText("ok");
	}
	
	/**
	 * 
	 * @标题: BaseAction.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：到jav网站获取数据的入口
	 */
	public void newsrc(){
		String type=getPara("type");
		String fhkey=getPara("fhkey");
		String jsonlist=getPara("jsonlist");
		String timesession=getPara("timesession");
		double threadnum=Double.valueOf(getPara("threadnum"));
		try{
			PageKit.testHaveNewHost();
			PageManager pm=new PageManager();
			PageRun pr=new PageRun(pm);
			pr.doit(threadnum,jsonlist,type,fhkey);
			getRequest().getSession().setAttribute("PageManager"+timesession,pm);
			String res="ok";
			renderText(res);
		} catch (Exception e) {
			logger.error("newsrc: "+e.toString());
			renderText("<br>"+e.toString());
		}
	}

	public void addToErrList(){
		try {
			String type=getPara("type");
			String fhkey=getPara("fhkey");
			String jsonlist=getPara("jsonlist");
			errpage err= new errpage(type,jsonlist,"numlist",fhkey);
			PgsqlKit.save(ClassKit.errTableName, err);
			PageKit.testHaveNewHost();
			String res="ok";
			renderText(res);
		} catch (Exception e) {
			logger.error("addToErrList: "+e.toString());
			renderText("<br>" + e.toString());
		}
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category ajax得到获取页面的进度
	 */
	public void getPageManager(){
		String timesession=getPara("timesession");
		PageManager pm = (PageManager) getRequest().getSession().getAttribute("PageManager"+timesession);
		if(pm!=null){
			renderText(pm.getSb());
		}else{
			renderText("have no change");
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
	 * @方法描述： 删除数据，返回成功状态。
	 */
	public void deleteData() {
		try {
			String id = getPara("id");
			int res = PgsqlKit.deleteById(ClassKit.javTableName, id);
			renderText(res + "");
		}catch (Exception e){
			logger.error("deleteData: "+e.toString());
			renderText("0");
		}
	}

	public void findthis() {
		try {
			String id = getPara("id");
			String type = getPara("type");
			Long count = getParaToLong("count");
			//获得当前对象插入时间
			javsrc jav= (javsrc) PgsqlKit.findById(ClassKit.javClass,id);
			String javtime=jav.getTimes();
			javtime=DateKit.KsrqString(javtime);
			//获得对象时间之前的元素
			SearchQueryP p = new SearchQueryP();
			Map mp = new HashMap();
			p.setParameters(mp);
			p=PageKit.istodayRoot(p);
			mp=p.getParameters();
			mp.put("tabtype",type);
			mp.put("GT_times",javtime);
			long precount=PgsqlKit.getCollectionCount(ClassKit.javTableName,mp);
			//获得当前时间内元素的位置
			mp.remove("GT_times");
			mp.remove("LTE_times");
			mp.put("IS_times",javtime);
			Map res = PgsqlKit.findByCondition(ClassKit.javClientClass, p);
			List<com.meteor.model.vo.javsrc> srcs = (List<com.meteor.model.vo.javsrc>) res.get("list");
			int index=0;
			for(;index<srcs.size();index++){
				com.meteor.model.vo.javsrc j=srcs.get(index);
				if(j.getId().equals(id)){
					break;
				}
			}
			long totalIndex=precount+index;
			long page=totalIndex/count+1;
			long yu=totalIndex%count+1;

			String resp=type+"中第"+page+"页,第"+yu+"个元素";
			renderText(resp);
		}catch (Exception e){
			logger.error("findthis: "+e.toString());
			renderText("err");
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
	 * @方法描述： 删除数据，返回成功状态。
	 */
	public void deletePageData() {
		try {
			HttpServletResponse response = getResponse();
			String idstr = getPara("idstr");
			String[] idlist = idstr.split("--");
			int result = PgsqlKit.deleteByIdlist(ClassKit.javTableName, idlist);
			renderText(result + "");
		}catch (Exception e){
			logger.error("deletePageData: "+e.toString());
			renderText("0");
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
	 * @方法描述： 上传图片和种子
	 */
	public void postimgtor(){
		try {
			HttpServletRequest request=getRequest();
			String oldpath=getPara("oldpath");
			UploadFile uf=getFile("imgtorFile");//先上传到upload文件夹
	        String imgFileName=uf.getOriginalFileName();
			String fliename=imgFileName.substring(0,imgFileName.indexOf("."));
			String tmpdir= MainConfig.tmpsavedir;//PropKit.get("tmpsavedir");
			String tmppath=tmpdir+"/"+imgFileName;

			if(StringUtils.isNotBlank(oldpath)){
				String filepath= PageKit.copypathold(request, oldpath);
				FileUtils.copyFile(new File(tmppath), new File(filepath));
			}else{
				String typedir=getPara("typedir");
		        String filepath= PageKit.copypath(request, typedir, fliename, imgFileName);
				oldpath= PageKit.webpath(request, typedir, fliename, imgFileName);
				FileUtils.copyFile(new File(tmppath), new File(filepath));
			}
			renderText(oldpath);
		} catch (Exception e) {
			logger.error("postimgtor: " + e.toString());
			renderText("传输文件异常");
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
	 * @方法描述：提交新增的表单数据
	 */
	public void uploadform() {
		try {
			String torsrc = getPara("torsrc");
			String tagslist = getPara("tagslist");
			String[] tags = getParaValues("tags");

			javsrc src = getModel(javsrc.class);
			src.setTimes(DateKit.getStringDate());
			src.setTabtype("classical");
			src.setId(StringKit.getMongoId());

			List<String> torpath = new ArrayList();
			List<String> torname = new ArrayList();
			torpath.add(torsrc);
			src.setBtfile(JsonKit.bean2JSON(torpath));
			torname.add(torsrc.substring(torsrc.lastIndexOf("/") + 1));
			src.setBtname(JsonKit.bean2JSON(torname));

			List<String> taglist = new ArrayList();
			for (String tag : tags) {
				taglist.add(tag.toUpperCase());
			}
			if (StringUtils.isNotBlank(tagslist)) {
				String tagArray[] = tagslist.split(",");
				for (int i = 0; i < tagArray.length; i++) {
					taglist.add(tagArray[i].toUpperCase());
				}
			}
			taglist.add("classical".toUpperCase());
			src.setTags(JsonKit.bean2JSON(taglist));
			PgsqlKit.save(ClassKit.javTableName, src);
			renderText("ok");
		}catch (Exception e){
			logger.error("uploadform: " + e.toString());
			renderText("0");
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
	 * @方法描述： 上传exl数据，解析并保存到资源库，记录数据到表中。
	 */
	public void uploadExl(){
		try {
			final HttpServletRequest request=getRequest();
			UploadFile uf=getFile("theExlFile");
			String imgFileName=uf.getOriginalFileName();
			String tmpdir=MainConfig.tmpsavedir;//PropKit.get("tmpsavedir");
			String tmppath=tmpdir+"/"+imgFileName;
			FileInputStream is=FileUtils.openInputStream(new File(tmppath));
			List<InExl> inelist= CzExlKit.readexl(is);//exl转对象

			int threadnum=4;
			List<List<InExl>> parentArray=new ArrayList();
			for (int i = 0; i < threadnum; i++) {
				List<InExl> childArray=new ArrayList();
				parentArray.add(childArray);
			}
			float lengths=inelist.size();
			int intlength=(int) Math.ceil(lengths/threadnum);
			for(int i = 0; i <threadnum; i++) {
				int ed=(i+1)*intlength;
				int bg=i*intlength;
				for (int j = bg; j <ed; j++) {
					if((j+1)<=lengths){
						parentArray.get(i).add(inelist.get(j));
					}else{
						break;
					}
				}
			}

			WorkPool pool = new WorkPool(threadnum);
			List<Future> fetures = new ArrayList<Future>();
			for(int i = 0; i <threadnum; i++) {
				final List<InExl> onelist=parentArray.get(i);
				@SuppressWarnings("unused")
				Future future = pool.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						logger.error("正在导入数据");
						return Thread.currentThread().getName()+":"+writeDisk(onelist, request,null);
					}
				});
				fetures.add(future);
			}
			StringBuffer sb=new StringBuffer();
			for(Future f : fetures){
				try {
					sb.append(f.get()+"<br>");
				} catch (Exception e) {
					logger.error("uploadExl getfetures: " + e.toString());
				}
			}
			pool.shutdown();
			//本地资源转数据库
			classcalTo64();
			renderText(sb.toString());
		} catch (Exception e) {
			logger.error("uploadExl: " + e.toString());
			renderText("导入失败");
		}
	}
	

	private String writeDisk(List<InExl> inelist,HttpServletRequest request,String oldclass) throws Exception {
		if(inelist.size()>0){
			for(int i=0;i<inelist.size();i++){
				InExl ine=inelist.get(i);
				javsrc js=new javsrc();
				js.setIsstar("1");
				js.setId(StringKit.getMongoId());
				js.setTimes(DateKit.getStringDate());
				js.setTitle(ine.getParentDirMc());
				js.setSbm(PageKit.getSbmByTitle(ine.getParentDirMc()));
				js.setTabtype(ine.getType());
				js.setIsdown(ine.getIsdown());
				List tags=new ArrayList();
				tags.add(ine.getType().toUpperCase());
				tags.add(ine.getParentDirMc().toUpperCase());
				tags.add(ine.getPparentDirMc().toUpperCase());
				tags.add(ine.getTypeDirMc().toUpperCase());
				js.setTags(JsonKit.bean2JSON(tags));
				//这里用title去查一遍比较好，但是会慢
				SearchQueryP p=new SearchQueryP();
				Map mp=new HashMap();
				mp.put("tabtype", "classical");
				mp.put("LIKE_sbm", js.getSbm());
				p.setParameters(mp);
				Map res=PgsqlKit.findByCondition(ClassKit.javClass, p);
				List<javsrc> srcs = (List<javsrc>) res.get("list");
				boolean goflag = false;
				if(srcs!=null && srcs.size()>0){
					goflag = true;
				}
				if(!goflag){
					List btfile=new ArrayList();
					List btname=new ArrayList();
					for(int j=0;j<ine.getFilePath().length;j++){
						String filenames=ine.getFilename()[j];
						String filepaths=ine.getFilePath()[j];
						String filenm=filenames.substring(0,filenames.indexOf("."));
						
						String realtime=DateKit.getRealtime(filepaths);
						js.setTimes(realtime);
						
				        String filepath="";
				        if(filenames.indexOf(".torrent")>=0){
				        	filepath= PageKit.copypath(request, ine.getTypeDirMc(), ine.getParentDirMc(), filenames);
				        }else{
				        	filepath= PageKit.copypath(request, ine.getTypeDirMc(), ine.getParentDirMc(), filenames.replace(filenm, ine.getParentDirMc()));
				        }
				        File f=new File(filepath);
				        File src=new File(filepaths);
				        FileUtils.copyFile(src,f);
				        filepaths="/"+filepath.substring(filepath.indexOf("javsrc"));
				        filepaths=PageKit.formatLocalpath(filepaths);
						if(filenames.indexOf(".torrent")>=0){					        
							btfile.add(filepaths);
							btname.add(filenames);
						}else{
							js.setImgsrc(filepaths);
						}
					}
					js.setBtfile(JsonKit.bean2JSON(btfile));
					js.setBtname(JsonKit.bean2JSON(btname));
					PgsqlKit.save(ClassKit.javTableName, js);
				}			
			}
			return "导入成功";
		}else{
			return "导入失败";
		}
	}
	

	
	/**
	 * 
	 * @标题: BaseAction.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-16
	 * @版本：1.0
	 * @方法描述：跳转到编辑页
	 */
	public  void toedit()  {
		try {
			HttpServletRequest request = getRequest();
			PageKit.setpc(request);
			String id = getPara("mgid");
			javsrc jav = (javsrc) PgsqlKit.findById(ClassKit.javClass, id);
			request.setAttribute("javobj", jav);
			render(PageKit.topage(request,0, 0, "editsrc", "addedit", null)+ ".jsp");
		}catch (Exception e){
			logger.error("toedit: " + e.toString());
			renderError(500);
		}
	}
	
	/**
	 * 
	 * @标题: BaseAction.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-16
	 * @版本：1.0
	 * @方法描述：编辑更新数据
	 */
	public void updatefrom() {
		try {
			String id=getPara("mgid");
			String tagslist=getPara("tagslist");
			javsrc jav=getModel(javsrc.class);
			jav.setId(id);
			List<String> taglist=new ArrayList();
			if(StringUtils.isNotBlank(tagslist)){
				String tagArray[]=tagslist.split(",");
				for(int i=0;i<tagArray.length;i++){
					taglist.add(tagArray[i].toUpperCase().trim());
				}
			}
			jav.setTags(JsonKit.bean2JSON(taglist));
			String jsonbean=JsonKit.bean2JSON(jav);
			Map p= JsonKit.json2Bean(jsonbean, HashMap.class);
			int renum=PgsqlKit.updateById(ClassKit.javTableName, p);
			if(renum>0){
				renderText("1");
			}else{
				renderText("0");
			}
		} catch (Exception e) {
			logger.error("updatefrom: " + e.toString());
			renderText("0");
		} 
		
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt数据
	 */
	public  void getBt(){
		try {
			javsrc jav = getModel(javsrc.class);
			String sv = getPara("searchval");
			String id = getPara("mgid");
			String idtype=PropKit.get("selectbt");
			String res=PageKit.selectbt(idtype, sv, id, jav,false);
			renderText(res);
		}catch (Exception e){
			logger.error("getBt: " + e.toString());
			renderText("");
		}
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 获取bt数据
	 */
	public  void pageGetBt(){
		try {
			String sv = getPara("searchval");
			String idtype = getPara("idtype");
			String flag = getPara("islike");
			boolean likeflag=true;
			if(flag.equalsIgnoreCase("false")){
				likeflag=false;
			}
			String res = PageKit.selectbt(idtype, sv, null, null,likeflag);
			renderText(res);
		}catch (Exception e){
			logger.error("getBt: " + e.toString());
			renderText("");
		}
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 读目录，并导出exl表格，包括根目录有4层目录结构
	 */
	public void writeExl(){
		try {
			String inpath=getPara("inpath");
			String outpath=getPara("outpath");
			String cdlx=getPara("cdlx");
			CzExlKit.writeExl(inpath, outpath, cdlx);
			renderText("1");
		} catch (Exception e) {
			logger.error("writeExl: " + e.toString());
			renderText("0");
		}		
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 导出目录下的文件到另一个目录，快速复制
	 */
	public void expFiles(){
		try {
			String inpath=getPara("inpath");
			String outpath=getPara("outpath");
			String containted=getPara("containted");
			FileOperateKit.fastCopyFile(inpath,1,outpath,inpath,containted);
			//FileIOKit.exportFiles(inpath, outpath);
			renderText("1");
		} catch (Exception e) {
			logger.error("expFiles: " + e.toString());
			renderText("0");
		}		
	}


	/**
	 * @author Meteor
	 * @Title
	 * @category 一键下载图片和bt到指定目录,或者打包下载
	 */
	public void createPackage(){
		String iszip=getPara("iszip");
		HttpServletRequest request=getRequest();
		String basedir=getPara("basedir");
		String filepath= PageKit.getfilePath(request);
		String fileorigpath=filepath;
        filepath=filepath+"/onekeytmp/"+basedir+"/";
		if(StringUtils.isNotBlank(iszip)&&iszip.equals("1")) {
			File zipf=new File(filepath+"/"+basedir+".zip");
			if(!zipf.exists()){
				String code=downloadSrc(fileorigpath);
				if(!code.equals("0") && !code.contains("isdown")){
					renderText(code);
					return;
				}
				SecurityEncodeKit.compressZip(filepath,basedir);
			}
			String downpath=PageKit.getDownBasePath(request)+"onekeytmp/"+basedir+"/"+basedir+".zip";
			renderText(downpath);
		}else{
			String code=downloadSrc(fileorigpath);
			if(!code.equals("0") && !code.contains("isdown")){
				renderText(code);
				return;
			}
			renderText("is_ok");
		}
	}

	private String downloadSrc(String fileorigpath) {
		String rootsavedir=PropKit.get("rootsavedir");
		String btlist=getPara("btlist");
		String oneid=getPara("oneid");
		String basedir=getPara("basedir");
		String filepath=fileorigpath;
		filepath=filepath+"\\onekeytmp\\"+basedir+"\\";
		File f0 = new File(filepath);
		if (!f0.exists()) {
			f0.mkdir();
		}
		try {
			javsrc js=(javsrc)PgsqlKit.findById(ClassKit.javClass,oneid);
			String img=js.getImgsrc();
			if (img.contains("data:image/")) {
				img = img.replace(PageKit.getimgBase64Tip(), "");
				String filename = oneid + ".jpg";
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
				String returncode=PageKit.downloadWithStatus(url,filedest,"3");
				if(!returncode.equals("0")){
					return returncode;
				}
			}
		} catch (Exception e) {
			logger.error("createPackage: " + e.toString());
			//renderText("3");//下载图片出错。
			return "3";
		}

		boolean isDownloadOne=false;
		String reCode="0";
		try {
			String[] bts = btlist.split("-9-");
			for (int i = 0; i < bts.length; i++) {
				if (bts[i].contains(PageKit.gettorBase64Key())) {
					String torid=bts[i].split(PageKit.gettorBase64Key())[1];
					javtor js = (javtor) PgsqlKit.findById(ClassKit.javtorClass, torid);
					String tor=js.getTorbase();
					String filename = torid + ".torrent";
					String filedest = filepath + filename;
					SecurityEncodeKit.GenerateImage(tor, filedest);
				} else if (bts[i].contains(rootsavedir)) {
					String bt = bts[i].replace(rootsavedir, "");
					String fileorig = fileorigpath + bt;
					String filename = bts[i].substring(bts[i].lastIndexOf("/") + 1, bts[i].length());
					String filedest = filepath + filename;
					FileUtils.copyFile(new File(fileorig), new File(filedest));
					isDownloadOne=true;
				} else {
					String url = null;
					String filedest = null;
					if(bts[i].contains("magnet:?xt=")){
						url =PageKit.magnetToXunleiLink(bts[i]) ;
						if(StringUtils.isBlank(url)) {
							reCode="4";
						}
						String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
						filedest = filepath + filename;
						String returncode=PageKit.downloadWithStatus(url, filedest, "5");
						if(returncode.equals("404")){
							url =PageKit.magnetToTorcacheLink(bts[i]) ;
							if(StringUtils.isBlank(url)) {
								reCode="4";
							}
							filename = url.substring(url.lastIndexOf("/") + 1, url.length());
							filedest = filepath + filename;
							returncode=PageKit.downloadWithStatus(url, filedest, "5");
							if (!returncode.equals("0")) {
								reCode = returncode;
							}else{
								isDownloadOne=true;
							}
						}else {
							if (!returncode.equals("0")) {
								reCode = returncode;
							}else{
								isDownloadOne=true;
							}
						}
					}else {
						url = PageKit.replace20(bts[i]);
						String filename = null;
						if (bts[i].indexOf(".torrent") > -1) {
							filename = bts[i].substring(bts[i].lastIndexOf("/") + 1, bts[i].length());
						} else {
							filename = bts[i].substring(bts[i].lastIndexOf("=") + 1, bts[i].length()) + ".torrent";
						}
						filename = java.net.URLEncoder.encode(filename.toLowerCase(), "UTF-8");
						filedest = filepath + filename;
						String returncode=PageKit.downloadWithStatus(url, filedest, "2");
						if(!returncode.equals("0")){
							reCode=returncode;
						}else{
							isDownloadOne=true;
						}
					}

				}
			}
		} catch (Exception e) {
			logger.error("createPackage: " + e.toString());
			//renderText("2");//下载种子出错。
			//return "2";
			reCode="2";
		}
		if(!reCode.equals("0") && isDownloadOne==true){
			reCode=reCode+"_isdown";
		}
		return reCode;
	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 转换不可url访问的图片
	 */
	public void tobase64(){
		try {
			PageKit.tobase64();
			PageKit.tobase64By503();
			renderText("成功");
		} catch (Exception e) {
			logger.error("图片转换异常: " + e.toString());
			renderText("图片转换异常");
		}
	}

	public void updateCache(){
		try {
			ServletContext sct=getSession().getServletContext();
			PageKit.updateCache(sct);
			renderText("成功");
		} catch (Exception e) {
			logger.error("刷新缓存异常: " + e.toString());
			renderText("刷新缓存异常");
		}
	}

	public void readconfig(){
		try {
			HttpServletRequest request=getRequest();
			String cofpath=PageKit.getConfigPath(request);
			String cof=FileUtils.readFileToString(new File(cofpath),"UTF-8");
			renderText(cof);
		} catch (IOException e) {
			logger.error("读取配置文件异常: " + e.toString());
			renderText("-1");
		}
	}

	public void writeconfig(){
		try {
			String text= getPara("text");
			HttpServletRequest request=getRequest();
			String cofpath=PageKit.getConfigPath(request);
			FileUtils.write(new File(cofpath),text,"UTF-8");
			PageKit.updateProp();
			renderText("修改配置成功");
		} catch (IOException e) {
			logger.error("修改配置文件异常: " + e.toString());
			renderText("修改配置文件异常");
		}
	}

	@Clear
	public void checkhost(){
		try {
			HttpServletRequest request=getRequest();
			String cofpath=PageKit.getConfigPath(request);
			String cof= FileUtils.readFileToString(new File(cofpath), "UTF-8");
			String newcof=null;

			newcof=testHaveNewHostByType("censoredhost",cof);
			newcof=testHaveNewHostByType("uncensoredhost",newcof);
			newcof=testHaveNewHostByType("westporn",newcof);

			if(!cof.equals(newcof)) {
				FileUtils.write(new File(cofpath), newcof, "UTF-8");
				PageKit.updateProp();
			}
			renderText("0");
		} catch (IOException e) {
			logger.error("替换url任务失败:"+e.toString());
			renderText("-1");
		}
	}

	private String testHaveNewHostByType(String type,String cof){
		try {
			String host= PropKit.get(type);
			String newhost= HttpClientHelp.getReHost(host, null,true);
			URL url=new URL(host);
			String oldhost=url.getHost();
			if(!oldhost.equals(newhost)){
				String hostrep=host.replace(oldhost,newhost);
				if(StringUtils.isNotBlank(host)){
					cof=cof.replace(host,hostrep);
					logger.error("替换" + type + "的url完成，新url为：" +hostrep);
					return cof;
				}else{
					return  cof;
				}
			}else{
				return  cof;
			}
		} catch (Exception e) {
			logger.error("替换" + type +"失败:"+e.toString());
			return  cof;
		}
	}

	public void readuuid(){
		try {
			HttpServletRequest request=getRequest();
			String cofpath=PageKit.getConfigUUIDPath(request);
			String cof=FileUtils.readFileToString(new File(cofpath),"UTF-8");
			renderText(cof);
		} catch (IOException e) {
			logger.error("读取配置文件异常: " + e.toString());
			renderText("-1");
		}
	}

	public void writeuuidconfig(){
		try {
			String text= getPara("text");
			HttpServletRequest request=getRequest();
			String cofpath=PageKit.getConfigUUIDPath(request);
			FileUtils.write(new File(cofpath),text,"UTF-8");
			PageKit.updateProp();
			renderText("修改配置成功");
		} catch (IOException e) {
			logger.error("修改配置文件异常: " + e.toString());
			renderText("修改配置文件异常");
		}
	}
	
	public void classcalTo64(){
		try {
			PageKit.classicalTobase64(getRequest());
			renderText("转换成功");
		} catch (Exception e) {
			logger.error("转换文件异常: " + e.toString());
			renderText("转换文件异常");
		} catch (Throwable t) {
			logger.error("转换文件异常: " + t.toString());
			classcalTo64();
			renderText("转换文件异常");
		}
	}

	//is work
	public void testdownload(){
		HttpServletRequest request=getRequest();
		if(request.getServletContext().getAttribute("download")==null){
			Map p=new HashMap();
			request.getServletContext().setAttribute("download",p);
		}
		render("testdownload.jsp");
	}

	//is work
	private String restart(TaskThreadManagers ttm){
		try {		
			ttm.setSleepthreadcountDefault();
			ttm.setRetrynumcountDefault();
			ttm.setDownload(true, false);
			return "继续下载成功";
		} catch (Exception e) {
			logger.error("继续下载: "+e.toString());
			return "继续下载异常";
		}
	}

	//is work
	public  void dataop(){
		try {
			HttpServletRequest request = getRequest();
			String opcode = request.getParameter("opcode");
			String filecode = request.getParameter("filecode");
			Map<String, Object> p = (Map<String, Object>) request.getServletContext().getAttribute("download");
			TaskThreadManagers ttm = (TaskThreadManagers) p.get(filecode);

			if (opcode.equals("1")) {//暂停下载
				ttm.setDownload(false, true);
			} else if (opcode.equals("2")) {//继续下载
				renderText(restart(ttm));
			} else if (opcode.equals("3")) {//从停止中恢复下载
				renderText(restart(ttm));
			} else {//删除当前下载
				ttm.setRetrynumcountDefault();
				ttm.setIsdel(true);
				ttm.setTaskEnd();
				p.remove(filecode);
			}
			renderText("操作成功");
		}catch (Exception e){
			logger.error("dataop: " + e.toString());
			renderText("操作异常");
		}
	}

	//is work
	public void getdatalist(){
		HttpServletRequest request =getRequest();
		Map<String,Object> p=(Map<String,Object>) request.getServletContext().getAttribute("download");
		if(p==null||p.size()==0){
			renderText("");
		}else {
			renderJson(JsonKit.bean2JSON(p));
		}
	}

	//is work
	public void getbig(){
		HttpServletRequest request =getRequest();
		//String filename=request.getParameter("filename");
		String url=getPara("url");
		int threadnum=getParaToInt("threadnum");
		int trynum=getParaToInt("trynum");
		String fileroot=HttpClientHelp.getFileroot();
		File f0 = new File(fileroot);
		if (!f0.exists()) {
			f0.mkdir();
		}
		fileroot=fileroot+url.substring(url.lastIndexOf("/")+1);
		renderText(dosomthing(url, threadnum, trynum, fileroot, request));
	}

	//is work
	private String dosomthing(String url,int threadnum,int trynum,String filename,HttpServletRequest request){
		try {
			String name=filename.substring(filename.lastIndexOf("/")+1);
			Map mp=HttpClientHelp.getLengthAngName(url,false);	
			String ctype=(String) mp.get("ctype");
			if(!ctype.contains("text/html")){
				String filenamebf=(String) mp.get("filename");
				if(filenamebf!=null){
					String hz=filenamebf.substring(filenamebf.lastIndexOf("."));
					if(!name.contains(hz)&&!hz.equals(".*")){
						filename=filename.substring(0,filename.lastIndexOf("/")+1)+filenamebf;
						name=filenamebf;
					}		
				}
				name= SecurityEncodeKit.MD5Encode(name);
				
				long contentLength=Long.valueOf(mp.get("contentLength")+"");
				
				Map p=(Map) request.getServletContext().getAttribute("download");
				if(p==null){
				    p=new HashMap();
				}
				if(p.get(name)==null){
					TaskThreadManagers ttm= MultitDownload.instanceMultitDownloadSetAll(contentLength, url, filename, threadnum, trynum);
					p.put(name, ttm);
					request.getServletContext().setAttribute("download",p);
					return "添加下载成功";
				}else{
					return "已存在相同的下载任务";
				}		
			}else{
				return "不可下载的文件类型";
			}
		} catch (Exception e) {
			logger.error("添加下载任务: "+e.toString());
			return "添加下载任务异常";	
		}
	}

}
