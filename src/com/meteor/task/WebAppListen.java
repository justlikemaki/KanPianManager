package com.meteor.task;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.meteor.kit.PageKit;

/**
 * 加载数据到application
 * @author zhanglei
 *
 */
public class WebAppListen implements ServletContextListener{
	/**
	 *   释放
	 * @param contextEvent 
	 */
	public void contextDestroyed(ServletContextEvent contextEvent) {
		
	}

	/**
	 *   初始化
	 * @param contextEvent 
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {
		ServletContext sct = contextEvent.getServletContext();
		
		//读取配置文件属性示例
		//updateCache(sct);
		new AutoThread(sct).start();
	}
	

	/**
	 * 定时更新缓存
	 * @author 章磊
	 *
	 */
	class AutoThread extends Thread {
		/**
		 * application
		 */
		ServletContext sct;
		/**
		 * 构造
		 * @param sct application
		 */
		public AutoThread(ServletContext sct){
			this.sct = sct;
		}
		/**
		 * 运行
		 */
		public void run() {
			int minu = 30;
			while (true) {
				try {
					sleep(1000 * 60 * minu);
					//System.out.println("每隔" + minu + "分钟自动刷新缓存" + DateKit.getStringDate());
					PageKit.updateCache(sct);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
