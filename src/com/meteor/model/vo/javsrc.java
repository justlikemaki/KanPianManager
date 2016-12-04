package com.meteor.model.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.PropKit;
import com.meteor.kit.JsonKit;
import com.meteor.kit.PageKit;

public class javsrc {

    private String id;
    private String title;
    private String times;
    private String imgsrc;
    private String tabtype;
    private String isdown;
    private String isstar;
    private String tagstr;
    private String tags, btfile, btname;
    private String sbm,sbmsb;
    private List<String> tagslist, btfilelist, btnamelist;
    private String imgsrccg;
    private List<String> btfilelistcg;

    public String getSbmsb() {
        sbmsb= PageKit.getSbmByTitle(title);
        return sbmsb;
    }

    public void setSbmsb(String sbmsb) {
        this.sbmsb = sbmsb;
    }

    public String getSbm() {
        return sbm;
    }

    public void setSbm(String sbm) {
        this.sbm = sbm;
    }

    public String getTagstr() {
        this.tagstr = this.tags.replace("[", "").replace("]", "").replace("\"","");
        return this.tagstr;
    }

    public List<String> getTagslist() {
        tagslist=JsonKit.json2List(tags);
        return tagslist;
    }

    public void setTagslist(List<String> tagslist) {
        this.tagslist = tagslist;
    }

    public List<String> getBtfilelist() {
        btfilelist=JsonKit.json2List(btfile);
        return btfilelist;
    }

    public void setBtfilelist(List<String> btfilelist) {
        this.btfilelist = btfilelist;
    }

    public List<String> getBtnamelist() {
        btnamelist=JsonKit.json2List(btname);
        return btnamelist;
    }

    public void setBtnamelist(List<String> btnamelist) {
        this.btnamelist = btnamelist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTimes() {
        if(times.contains("00:00:00")){
            times = times.replace("00:00:00","");
        }
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getImgsrc() {
        return imgsrc;
    }

    public void setImgsrc(String imgsrc) {
        this.imgsrc = imgsrc;
        initImgsrccg();
    }

    public String getTabtype() {
        return tabtype;
    }

    public void setTabtype(String tabtype) {
        this.tabtype = tabtype;
    }

    public String getIsdown() {
        return isdown;
    }

    public void setIsdown(String isdown) {
        this.isdown = isdown;
    }

    public String getIsstar() {
        return isstar;
    }

    public void setIsstar(String isstar) {
        this.isstar = isstar;
    }

    public void setTagstr(String tagstr) {
        this.tagstr = tagstr;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getBtfile() {
        return btfile;
    }

    public void setBtfile(String btfile) {
        this.btfile = btfile;
        initTorsrccg();
    }

    public String getBtname() {
        return btname;
    }

    public void setBtname(String btname) {
        this.btname = btname;
    }


    public String getClassName() {
        return "JavSrc";
    }

    public String getImgsrccg() {
        return imgsrccg;
    }

    public void setImgsrccg(String imgsrccg) {
        this.imgsrccg = imgsrccg;
    }

    public List<String> getBtfilelistcg() {
		return btfilelistcg;
	}

	public void setBtfilelistcg(List<String> btfilelistcg) {
		this.btfilelistcg = btfilelistcg;
	}

	private void initImgsrccg(){
        String rootdir= PropKit.get("rootdir");
        this.imgsrccg=this.imgsrc;
        if(StringUtils.isNotBlank(this.imgsrc) && this.imgsrccg.contains(PageKit.getimgBase64Key())){
            this.imgsrccg=rootdir+"/interface/imgbase/"+this.imgsrc;
            this.imgsrc=null;
        }
    }
	
	private void initTorsrccg(){
        String rootdir= PropKit.get("rootdir");
        this.btfilelistcg=getBtfilelist();
        if(StringUtils.isNotBlank(this.btfile) && this.btfile.contains(PageKit.gettorBase64Key())){
        	List<String> newlisttor = new ArrayList<String>();
            for (String tor:this.btfilelistcg) {
            	newlisttor.add(rootdir+"/interface/torbase/"+tor);
			}
            this.btfilelistcg=newlisttor;
        }
    }
}
