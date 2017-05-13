/**
 * 
 */
package com.meteor.model.vo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Element;

/**
 * @author justlikemaki
 *
 */
public class CompDate implements Comparable{
	private int index;
	private Element ele;
	private String date;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Element getEle() {
		return ele;
	}

	public void setEle(Element ele) {
		this.ele = ele;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}



	/**
	 * @标题: CompDls.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-16
	 * @版本：1.0
	 * @方法描述：
	 */
	@Override
	public int compareTo(Object o) {
		try {
			CompDate cp=(CompDate) o;
			SimpleDateFormat sdf=null;
			if(this.date.contains("-")){
				sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			}else{
				sdf = new SimpleDateFormat("MMM d, YYYY", Locale.US);
			}
			Date date1 = sdf.parse(this.date);
			Date date2 = sdf.parse(cp.getDate());
			long reslong=date2.getTime()-date1.getTime();
			int res=0;
			if(reslong>0){
				res=1;
			}else if(reslong<0){
				res=-1;
			}
			return res;
		} catch (ParseException e) {
			return 0;
		}
	}


}
