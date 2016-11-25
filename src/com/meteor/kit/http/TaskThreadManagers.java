package com.meteor.kit.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meteor.kit.DateKit;
import com.meteor.kit.SecurityEncodeKit;
import com.meteor.kit.StringKit;

public class TaskThreadManagers {
	/**
	 * 资源文件总长度（B）
	 */
	private long contentLengths = 0;
	/**
	 * 输出的格式化的资源文件大小
	 */
	private String contentsize = "";
	/**
	 * 已经下载的资源文件长度
	 */
	private long countLengths = 0;
	/**
	 * 下载进度
	 */
	private String degree = "";
	/**
	 * 平均下载速度
	 */
	private String averagerate = "";
	/**
	 * 重试次数，默认20次
	 */
	private int retrynum = 20;
	/**
	 * 重试计数
	 */
	private int retrynumcount = 0;
	/**
	 * 文件名
	 */
	private String filename = "";
	/**
	 * md5加密的文件名
	 */
	private String md5name = "";
	/**
	 * 下载链接
	 */
	private String url = "";
	/**
	 * 文件下载路径
	 */
	private String filepath = "";
	/**
	 * 下载状态
	 */
	private String status = "";
	/**
	 * 相应的下载操作
	 */
	private String dowhat = "";
	/**
	 * 相应的操作代码
	 */
	private int docode = 0;
	/**
	 * 距离下载完成还剩时间
	 */
	private String timeoff = "";
	/**
	 * 运行时间
	 */
	private String timerun = "";

	/**
	 * 是否中断
	 */
	protected boolean isinterrupt = false;
	/**
	 * 是否正在下载
	 */
	protected boolean isdownloading = true;
	/**
	 * 是否下载完成
	 */
	protected boolean isend = false;
	/**
	 * 是否删除
	 */
	protected boolean isdel = false;
	/**
	 * 下载完成线程计数
	 */
	protected int endcount = 0;
	/**
	 * 中断，被休眠的线程计数
	 */
	protected int sleepthreadcount = 0;
	/**
	 * 线程开始时间
	 */
	protected long bgtime = 0;
	/**
	 * 线程中断前的总耗时
	 */
	protected long lasttime = 0;
	/**
	 * 上一次的下载位置，用于计算平均速度
	 */
	protected long precountLengths = 0;
	/**
	 * 是否启用代理
	 */
	private static boolean openProxy = false;

	protected Map threadCountMap = new HashMap();
	protected Map threadStartPositionMap = new HashMap();
	protected Map posoneMap = new HashMap();
	protected Map countsizeMap = new HashMap();
	protected boolean acceptRanges = false;// 是否支持多线程下载
	protected Timer timer;// 用于创建定时任务，实时获取下载进度
	protected final long roundtime = 3;
	protected List<Subtasks> subs = new ArrayList();

	public static boolean getOpenProxy() {
		return openProxy;
	}

	public static void setOpenProxy(boolean openProxy) {
		TaskThreadManagers.openProxy = openProxy;
	}

	public boolean isIsdel() {
		return isdel;
	}

	public void setIsdel(boolean isdel) {
		this.isdel = isdel;
	}

	public synchronized int getEndcount() {
		return endcount;
	}

	public void setEndcount() {
		this.endcount += 1;
	}

	public synchronized int getSleepthreadcount() {
		return sleepthreadcount;
	}

	public void setSleepthreadcountDefault() {
		sleepthreadcount = 0;
	}

	public synchronized void setSleepthreadcount() {
		sleepthreadcount += 1;
	}

	public boolean isend() {
		long res = countLengths / contentLengths;
		if (res == 1)
			return true;
		else
			return false;
	}

	public void setBgtime(long bgtime) {
		this.bgtime = bgtime;
	}

	public boolean isIsend() {
		return isend;
	}

	public void setIsend(boolean isend) {
		this.isend = isend;
	}

	public List<Subtasks> getSubs() {
		return subs;
	}

	public void setSubs(List<Subtasks> subs) {
		this.subs = subs;
	}

	public boolean isIsinterrupt() {
		return isinterrupt;
	}

	public String getUrl() {
		return url;
	}

	public String getFilepath() {
		return filepath;
	}

	public synchronized void setTaskEnd() {
		for (Subtasks s : subs) {
			s.setIsend(true);
		}
	}

	public synchronized void setDownload(boolean isdownloading, boolean isinterrupt) {
		this.isdownloading = isdownloading;
		this.isinterrupt = isinterrupt;
		// setAllStatus();
		boolean isit = false;
		if (isinterrupt == true)
			isit = true;

		for (Subtasks s : subs) {
			s.setIsinterrupt(isit);
		}

	}

	private void setAllStatus() {
		setStatus();
		setDowhat();
		setDocode();
	}

	private void setStatus() {
		if (isend == true) {
			status = "完成";
		} else {
			if (isdownloading == true) {
				status = "下载中";
			} else if (isinterrupt == true) {
				status = "已停止";
			} else {
				status = "已暂停";
			}
		}
	}

	private void setDowhat() {
		if (isend == true) {
			dowhat = "";
		} else {
			if (isdownloading == true) {
				dowhat = "暂停";
			} else if (isinterrupt == true) {
				dowhat = "继续";
			} else {
				dowhat = "继续";
			}
		}
	}

	private void setDocode() {
		if (isend == true) {
			docode = 0;
		} else {
			if (isdownloading == true) {
				docode = 1;
			} else if (isinterrupt == true) {
				docode = 3;
			} else {
				docode = 2;
			}
		}
	}

	public void setUrlAndFilepath(String url, String filepath) {
		this.url = url;
		this.filepath = filepath;
	}

	public String getAveragerate() {
		return averagerate;
	}

	public void setAveragerate(String averagerate) {
		this.averagerate = averagerate;
	}

	public long getCountLengths() {
		return countLengths;
	}

	public void setCountLengths(int countLengths) {
		this.countLengths = countLengths;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public long getContentLengths() {
		return contentLengths;
	}

	public long getRoundtime() {
		return roundtime;
	}

	public Timer getTimer() {
		return timer;
	}

	public void startTimer() {
		bgtime = new Date().getTime();
		timer = new Timer();
		timer.schedule(new DateTasks(this), roundtime * 1000, roundtime * 1000);
	}

	public void closeTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			lasttime += new Date().getTime() - bgtime;
		}
	}

	public void setAcceptRanges(boolean acceptRang) {
		acceptRanges = acceptRang;
	}

	public boolean getAcceptRanges() {
		return acceptRanges;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		this.md5name = SecurityEncodeKit.MD5Encode(filename);
	}

	public void printRetrynum() {
		if (retrynumcount > 0) {
			if (retrynumcount < retrynum) {
				// System.out.println("已重试"+retrynumcount+"次,还剩"+(retrynum-retrynumcount)+"次");
				log("已重试" + retrynumcount + "次,还剩" + (retrynum - retrynumcount) + "次");
			} else {
				// System.out.println("已超过重试次数，结束此次下载，下载失败");
				log("已超过重试次数，结束此次下载，下载失败");
			}
		}
	}

	public int getRetrynumcount() {
		return retrynumcount;
	}

	public synchronized void setRetrynumcount() {
		this.retrynumcount += 1;
	}

	public void setRetrynumcountDefault() {
		this.retrynumcount = 0;
	}

	public int getRetrynum() {
		return retrynum;
	}

	public void setRetrynum(int retrynum) {
		this.retrynum = retrynum;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:24:42
	 * @Title averagerate void 返回类型
	 * @category 获取文件平均下载速度
	 */
	public void averagerate() {
		long KB = (countLengths - precountLengths) / getRoundtime() / 1000;
		// System.out.println("平均下载速度："+KB+"KB/s");
		String kbout = "平均下载速度：" + KB + "KB/s";
		log(kbout);
		setAveragerate(KB + "KB/s");
		setRestOfTime();

		precountLengths = countLengths;
	}

	private void setRestOfTime() {
		long B = (countLengths - precountLengths) / getRoundtime();
		long YB = contentLengths - countLengths;
		if (B != 0) {
			long BB = Math.round(YB / B);
			timeoff = DateKit.getTimeOff(BB * 1000);
		}
	}

	private void setRestOfTimeStop() {
		timeoff = "--";
	}

	public long getCountsizeMap(String tname) {
		return Long.valueOf(countsizeMap.get(tname).toString());
	}

	public void setCountsizeMap(String tname, long size) {
		countsizeMap.put(tname, size);
	}

	public Map getPosoneMapAll() {
		return posoneMap;
	}

	public Position getPosoneMap(String tname) {
		return (Position) posoneMap.get(tname);
	}

	public void setPosoneMap(String tname, Position posone) {
		posoneMap.put(tname, posone);
	}

	public void setThreadStartPosition(String tname, long startorcount) {
		Object obj = threadStartPositionMap.get(tname);
		if (obj != null) {
			String thiscount = obj.toString();
			long tcount = Long.valueOf(thiscount);
			tcount += startorcount;
			threadStartPositionMap.put(tname, tcount);
		} else {
			threadStartPositionMap.put(tname, startorcount);
		}
	}

	public long getThreadStartPosition(String tname) {
		return Long.valueOf(threadStartPositionMap.get(tname).toString());
	}

	public void setThreadCountMap(String tname, long count, long countsize) {
		Object obj = threadCountMap.get(tname);
		if (obj != null) {
			String thiscount = obj.toString();
			String scount = thiscount.split("/")[0];
			long tcount = Long.valueOf(scount);
			tcount += count;
			threadCountMap.put(tname, tcount + "/" + countsize);
		} else {
			threadCountMap.put(tname, count + "/" + countsize);
		}
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:25:14
	 * @Title getAllThreadCount void 返回类型
	 * @category 列出所有线程的下载情况
	 */
	public void getAllThreadCount() {
		try {
			if (contentLengths != 0) {
				Iterator it = threadCountMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					// System.out.println(key+":"+value);
					log(key + ":" + value);
				}
			}
		} catch (Exception e) {
			logger.error("输出所有线程的状态" + e.toString());
		}
	}

	/***
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月13日 下午11:26:55
	 * @Title getCount void 返回类型
	 * @category 获取文件总的下载情况
	 */
	public void getCount() {
		double a = countLengths;
		double b = contentLengths;
		double ab = a / b;
		ab = (ab * 100);
		DecimalFormat df = new DecimalFormat("###.0");
		if (contentLengths != 0) {
			// System.out.println();
			// System.out.println(DateUtil.getStringDate()+"
			// 已完成："+df.format(ab)+"% "+countLengths+"/"+contentLengths)
			String out = " 已完成：" + df.format(ab) + "%  ";
			log("\r\n" + DateKit.getStringDate() + out + countLengths + "/" + contentLengths);
			setDegree(df.format(ab) + "%");
		}
	}

	public synchronized void setAllCount(int count) {
		countLengths += count;
	}

	public void setContentLengths(long contentl) {
		contentLengths = contentl;
		// System.out.println("文件大小："+contentLengths+"B");
		log("文件大小：" + contentLengths + "B");
		contentsize = StringKit.toGB(contentLengths);
	}

	public void getAllOut() {
		getCount();
		getAllThreadCount();
		printRetrynum();
		averagerate();
		if (isinterrupt == true && sleepthreadcount == subs.size() - endcount) {
			setAllStatus();
		}
		if (isinterrupt == false) {
			setAllStatus();
		}
		if (docode == 2 || docode == 3 || isend == true) {
			setRestOfTimeStop();
			setAveragerate(0 + "KB/s");
		}
		getTimeRun();
	}

	public String getTimeRun() {
		long timeis = new Date().getTime() - bgtime + lasttime;
		return timerun = DateKit.getTimeOff(timeis);
	}

	protected final Logger logger = LoggerFactory.getLogger(TaskThreadManagers.class);

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午10:17:31
	 * @Title log
	 * @param content
	 * @param crawlParam
	 *            void 返回类型
	 * @category 打印执行方法后的日志
	 */
	public void log(String outstr) {
		try {
			if (true) {
				String ml = System.getProperty("catalina.home") + "/temp/log/";
				// String ml = "d:/" + "logs/download/";
				File f0 = new File(ml);
				if (!f0.exists()) {
					f0.mkdir();
				}
				ml = ml + filename + "_" + DateKit.getStringDateShort() + ".txt";
				File f = new File(ml);
				if (!f.exists()) {
					f.createNewFile();
				}

				OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(ml, true), "UTF-8");
				out.write(outstr);
				out.write("\r\n");
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			logger.error("打印多线程下载文件log", e);
		}
	}
}
