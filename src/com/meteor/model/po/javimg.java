package com.meteor.model.po;

import org.apache.commons.lang.RandomStringUtils;

import com.meteor.kit.StringKit;

public class javimg {

    private String id;
    private String srcid;
    private String imgbase;

    public javimg(String srcid,String imgbase){
        this.id= StringKit.getMongoId()+RandomStringUtils.randomNumeric(3);
        this.srcid = srcid;
        this.imgbase = imgbase;
    }
    
    public javimg(){
    }

	public String getImgbase() {
		return imgbase;
	}

	public void setImgbase(String imgbase) {
		this.imgbase = imgbase;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSrcid() {
		return srcid;
	}

	public void setSrcid(String srcid) {
		this.srcid = srcid;
	}

}
