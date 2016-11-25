package com.meteor.kit.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @ClassName CyclicBarrierDownload
 * @author Meteor
 * @date 2015年8月13日 下午11:10:43
 * @category 只适用于单线程请求调用的下载方法，正在下载中来的请求一律拒绝
 * 			 默认重试20次，超出后中断下载，并还原相应的默认值，所以并不能断点续传
 */
public class CyclicBarrierMultitDownload {	
	private TaskThreadManagers ttm;
	public CyclicBarrierMultitDownload(TaskThreadManagers ttm){
		this.ttm=ttm;
	}
	/***
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:17:02
	 * @Title getDownloadFileInfo
	 * @param url
	 * @return
	 * @throws Exception long 返回类型
	 * @category 获取文件长度，并知道是否可以多线程下载
	 */
	private void getDownloadFileInfo(long contentLength,String url) throws Exception{	    
	    if(contentLength>0){
	    	HttpGet httpHead = new HttpGet(url);
	    	CloseableHttpResponse response = null;
	    	try {
	    		CloseableHttpClient other_httpClient = HttpClientHelp.getOtherHttpClient();
		    	httpHead.setHeader("Range", "bytes=0-"+(contentLength-1));
		    	httpHead.setConfig(HttpClientHelp.getRequestConfig(false,ttm.getOpenProxy()));
		    	response = other_httpClient.execute(httpHead);
		    	if(response.getStatusLine().getStatusCode() == 206){
		    		ttm.setAcceptRanges(true);
		    	}
			} finally {
				if (response != null)
					response.close();
				httpHead.abort();
				httpHead.releaseConnection();
			}
	    }
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:17:59
	 * @Title muultitDownload
	 * @param url
	 * @param threadnum
	 * @param filepath
	 * @return
	 * @throws Exception String 返回类型
	 * @category 多线程下载文件，默认重试20次
	 */
	public String multitDownload(long length,String url,int threadnum,String filepath) throws Exception{
		return multitDownload( length,url, threadnum, filepath,0 ) ;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:17:59
	 * @Title muultitDownload
	 * @param url
	 * @param threadnum
	 * @param filepath
	 * @return
	 * @throws Exception String 返回类型
	 * @category 多线程下载文件，可设定重试次数
	 */
	public String multitDownload(long length,String url,int threadnum,String filepath,int retrynum) throws Exception{
		String res="";
		getDownloadFileInfo(length,url);
		long contentLength=length;

		String filename=filepath.substring(filepath.lastIndexOf("/")+1);
		ttm.setFilename(filename);
		ttm.setUrlAndFilepath(url, filepath);
		if(retrynum>0){
			ttm.setRetrynum(retrynum);
		}
		if(contentLength>0 && ttm.getAcceptRanges()==true){
			ttm.startTimer();
			ttm.setContentLengths(contentLength);
			//每个请求的大小
			long perThreadLength = contentLength / threadnum + 1;
			long startPosition = 0;
			long endPosition = perThreadLength;
			List<Position> pos=new ArrayList<Position>();
			for (int i = 0; i < threadnum; i++) {
				if(endPosition >= contentLength){
			        endPosition = contentLength - 1;
				}
				Position posone=new Position(startPosition,endPosition);
				pos.add(posone);

				startPosition = endPosition + 1;//此处加 1,从结束位置的下一个地方开始请求
		        endPosition = startPosition+perThreadLength;
			}
			File f=new File(filepath);
			doit(url,threadnum, pos, f);
			res="多线程下载已启动！";
		}else{
			res= HttpClientHelp.getFileDownByPathFull(url, filepath,ttm.getOpenProxy());
			ttm.setIsend(true);
			ttm.getAllOut();
		}
		return res;
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:19:27
	 * @Title doit
	 * @param url
	 * @param threadnum
	 * @param pos
	 * @param filepath void 返回类型
	 * @category 执行多线程下载的任务分配与线程启动
	 */
	private void doit(String url,int threadnum,List<Position> pos,File filepath) {
		CyclicBarrier barrier = new CyclicBarrier(threadnum,new Tasks(ttm));
		ExecutorService exec = Executors.newFixedThreadPool(threadnum);	
		for (int i = 0; i < threadnum; i++) {			
			exec.submit(new Subtasks(ttm,barrier,url,pos.get(i),filepath,"thread-"+(i+1)));
		}
		exec.shutdown();
	}
	
}

class Subtasks implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(Subtasks.class);
	
	private CyclicBarrier barrier;	
	private File filepath;
	private String url;	
	private TaskThreadManagers ttm;
	private boolean isinterrupt=false;
	private boolean isend=false;
	
	public boolean isIsend() {
		return isend;
	}

	public void setIsend(boolean isend) {
		this.isend = isend;
	}

	public CyclicBarrier getBarrier() {
		return barrier;
	}

	public void setBarrier(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	public File getFilepath() {
		return filepath;
	}

	public void setFilepath(File filepath) {
		this.filepath = filepath;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public TaskThreadManagers getTtm() {
		return ttm;
	}

	public void setTtm(TaskThreadManagers ttm) {
		this.ttm = ttm;
	}

	public boolean isIsinterrupt() {
		return isinterrupt;
	}

	public void setIsinterrupt(boolean isinterrupt) {
		this.isinterrupt = isinterrupt;
	}
	
	public void barrieradd(){
		try {
			barrier.await();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public Subtasks(TaskThreadManagers ttm,CyclicBarrier barrier,String url,Position posone,File filepath,String threadnum) {
		this.ttm=ttm;
		this.barrier = barrier;
		this.filepath= filepath;
		this.url=url;
		ttm.setCountsizeMap(threadnum, posone.getEndPosition()-posone.getStartPosition()+1);
		ttm.setPosoneMap(threadnum, posone);
		ttm.getSubs().add(this);
	}
	
	public void run() {		
			Thread td=Thread.currentThread();
			String threadName=td.getName();
			threadName=threadName.split("-")[2]+"-"+threadName.split("-")[3];
			Position posone=ttm.getPosoneMap(threadName);
			//System.out.println(threadName+":"+posone.getStartPosition()+"==="+posone.getEndPosition());
			ttm.log(threadName+":"+posone.getStartPosition()+"==="+posone.getEndPosition());
			reget(posone.getStartPosition(),posone.getEndPosition(),threadName,1);	
			try {		
				ttm.setEndcount();
				barrier.await();
			} catch (Exception e) {
				logger.error("barrier+1",e);
			}
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:21:46
	 * @Title reget
	 * @param startPosition
	 * @param endPosition
	 * @param threadName
	 * @param isbegin void 返回类型
	 * @category 获取相应位置的文件流并写入相应位置
	 * 			发生异常后记录重试次数，并等待3秒后重试
	 */
	private void reget(long startPosition,long endPosition,String threadName,int isbegin){
		//创建随机读写类   
		HttpGet httpGet = new HttpGet(url);
        RandomAccessFile outputStream = null;
    	CloseableHttpResponse response = null;
		try {
			if(isend==true){
				return ;
			}
			if(isinterrupt==true){
				tosleep();
			}
			outputStream = new RandomAccessFile(filepath, "rw");
			if(isbegin==1){
				ttm.setThreadStartPosition(threadName,startPosition);
			}
			if(startPosition<endPosition){		    	
	    		CloseableHttpClient other_httpClient = HttpClientHelp.getOtherHttpClient();
	    		httpGet.setHeader("Range", "bytes="+startPosition+"-"+endPosition);
	    		httpGet.setConfig(HttpClientHelp.getRequestConfig(false,ttm.getOpenProxy()));
		    	response = other_httpClient.execute(httpGet);

		        InputStream inputStream = response.getEntity().getContent();

		        //跳到指定位置
		        outputStream.seek(startPosition);

		        int count = 0;
		        byte[] buffer=new byte[10240];
		        while((count = inputStream.read(buffer, 0, buffer.length))>0){
		        	if(isend==true){
		    			break;
		    		}
		        	if(isinterrupt==true){
		        		tosleep();
					}
		        	outputStream.write(buffer, 0, count);
		        	ttm.setThreadStartPosition(threadName,count);
		        	ttm.setThreadCountMap(threadName, count, ttm.getCountsizeMap(threadName));
		        	ttm.setAllCount(count);
		        }
			}
		} catch (Exception e) {
			logger.error("多线程下载:"+e.toString());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				logger.error("下载异常后的休眠线程",e1.toString());
			}
			isretrytrue(threadName);
		} catch(Throwable t){
			logger.error("多线程下载:"+t.toString());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				logger.error("下载异常后的休眠线程",e1.toString());
			}
			isretrytrue(threadName);
		} finally{
			try {
				if(outputStream!=null) outputStream.close();
				if(response!=null) response.close();
				HttpClientHelp.releaseMethod(httpGet);
			} catch (IOException e2) {
				logger.error("关闭文件流，释放get方法",e2);
			} catch(Throwable t){
				logger.error("关闭文件流，释放get方法",t);
			}
		}
	}
	
	private synchronized void isretrytrue(String threadName){
		int retrynum=ttm.getRetrynum();
		int retrynumcount=ttm.getRetrynumcount();
		if(retrynumcount<retrynum){
			ttm.setRetrynumcount();
			long newStartPosition=ttm.getThreadStartPosition(threadName);
			reget(newStartPosition,ttm.getPosoneMap(threadName).getEndPosition(),threadName,0);
		}else{
			ttm.setDownload(false, true);
			if(isinterrupt==true){
        		tosleep();
			}
			isretrytrue(threadName);
		}
	}
	
	private synchronized void timecancel(){
		if(ttm.getSleepthreadcount()==ttm.getSubs().size()-ttm.getEndcount()){
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				logger.error("关闭计时器休眠",e.toString());
			}
			ttm.closeTimer();
		}
	}
	
	private void tosleep(){
		ttm.setSleepthreadcount();
		timecancel();
		while (true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				logger.error("下载线程休眠",e.toString());
			}
			if(ttm.getTimer()!=null){
				timecancel();
			}
			if(isend==true){
				break ;
			}
			if(isinterrupt==false){
				if(Thread.currentThread().getName().contains("thread-1")){
					ttm.startTimer();
				}
				break;
			}
		}
	}
}

class Tasks implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(Tasks.class);
	
	private TaskThreadManagers ttm;
	public Tasks(TaskThreadManagers ttm){
		this.ttm=ttm;
	}
	
	public void run() {		
			HttpClientHelp.closeHttpClient(HttpClientHelp.getOtherHttpClient());
			ttm.closeTimer();
			if(ttm.isdel==false){
				ttm.setIsend(true);
				ttm.getAllOut();
				//System.out.println("下载总耗时:"+DateUtil.getedtimenoout()+"秒");
				//System.out.println("完成");			
				ttm.log("下载总耗时:"+ttm.getTimeRun());			
				ttm.log("完成");		
			}else{
				ttm.log("下载总耗时:"+ttm.getTimeRun());
				ttm.log("下载终止");	
			}
	}
}

class DateTasks extends TimerTask { 
	private TaskThreadManagers ttm;
	public DateTasks (TaskThreadManagers ttm){
		this.ttm=ttm;
	}
	
    public void run() {  
    	ttm.getAllOut();
    }
}

class Position {
	private long startPosition = 0;  
	private long endPosition = 0;
	
	public Position(long startPosition,long endPosition) {
		this.startPosition=startPosition;
		this.endPosition=endPosition;
	}
	
	public long getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(long startPosition) {
		this.startPosition = startPosition;
	}
	public long getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(long endPosition) {
		this.endPosition = endPosition;
	}
}

