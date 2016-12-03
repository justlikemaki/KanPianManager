package com.meteor.model.po;

import com.meteor.kit.StringKit;

public class javtor {

    private String id;
    private String srcid;
    private String torbase;

    public javtor(String srcid,String torbase){
        this.id= StringKit.getMongoId();
        this.srcid = srcid;
        this.torbase = torbase;
    }
    
    public javtor(){
    }

	public String getTorbase() {
		return torbase;
	}

	public void setTorbase(String torbase) {
		this.torbase = torbase;
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
