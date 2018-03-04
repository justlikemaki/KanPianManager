package com.meteor.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meteor.kit.ClassKit;
import com.meteor.kit.PageKit;
import com.meteor.kit.PgsqlKit;
import com.meteor.kit.getpage.PageManager;
import com.meteor.kit.getpage.PageRun;
import com.meteor.model.po.errpage;

public class HandleErrTask implements Job{
	private final Logger logger = LoggerFactory.getLogger(HandleErrTask.class);

	/**
	 * @author Meteor
	 * @Title
	 * @category 重新拉取发生错误了的页面
	 */
//	private void reFechPage(){
//		try {
//			boolean flag=HandleErrCircularQueueService.isEmpty();
//			if(flag) {
//				List<errpage> errpages = PgsqlKit.findall(ClassKit.errClass, null);
//				HandleErrCircularQueueService.add(errpages);
//				while (!HandleErrCircularQueueService.isEmpty()) {
//					errpage onepage=HandleErrCircularQueueService.getList().poll();
//					PgsqlKit.deleteById(ClassKit.errTableName, onepage.getId());
//					PageManager pm = new PageManager();
//					PageRun pr = new PageRun(pm);
//					pr.doit(1, onepage.getNum(), onepage.getType(), onepage.getSearchkey());
//					Thread.sleep(100000);
//				}
//			}
//		} catch (Exception e) {
//			logger.error("重新获取错误页面异常: " + e.toString());
//		}
//	}

	/**
	 * @author Meteor
	 * @Title
	 * @category 重新拉取发生错误了的页面(new)
	 * 按tabtype分组拉取错误url
	 */
	private void reFechPage(){
		try {
			PageKit.delrepeatedErrpage();//清除重复项
			Map<String,String> pageMap = new HashMap<String,String>();
			List<errpage> errpages = PgsqlKit.findall(ClassKit.errClass, null);
			for (errpage page:errpages){
				groupPage(pageMap,page);
			}
			for (Map.Entry<String, String> entry : pageMap.entrySet()) {
				String nums = entry.getValue();
				PageManager pm = new PageManager();
				PageRun pr = new PageRun(pm);
				if(entry.getKey().equals("westpronImg")){
					pr.doit(1, nums, "westpronImgList", null);
				}else if(entry.getKey().contains("---")) {
					nums = nums.replace("p2p", ",");
					String search = entry.getKey().split("---")[1];
					pr.doit(1, nums, entry.getKey(), search);
				}else{
					nums = nums.replace("p2p", ",");
					pr.doit(1, nums, entry.getKey(), null);
				}
			}
		} catch (Exception e) {
			logger.error("重新获取错误页面异常: " + e.toString(),e);
		}
	}

	private void groupPage(Map<String,String> pageMap,errpage page){
		String search = page.getSearchkey();
		String tabtype = page.getType();
		if(StringUtils.isNotBlank(search)){
			tabtype = tabtype+"---"+search;
		}
		String mapnum = pageMap.get(tabtype);
		String thenum = "";
		if(page.getNum().contains("--")){
			String[] numarr = page.getNum().split("--");
            int bg=Integer.valueOf(numarr[0]);
            int ed=Integer.valueOf(numarr[1]);
            List<String> sb=new ArrayList<String>();
            for(int i=bg;i<=ed;i++){
            	if(i==ed){
            		thenum += i+"errid"+page.getId();
            	}else{
            		thenum += i+"errid"+page.getId()+"p2p";
            	}
            }
		}else{
			thenum = page.getNum()+"errid"+page.getId();
		}
		if(mapnum==null){
			pageMap.put(tabtype,thenum);
		}else{
			pageMap.put(tabtype,thenum+"p2p"+mapnum);
		}
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		reFechPage();
		PageKit.updateJavNullimage();
	}
}
