package com.meteor.common;

import org.apache.commons.lang.StringUtils;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.plugin.quartz.QuartzPlugin;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.meteor.controller.BaseAction;
import com.meteor.controller.HttpInterfaceAction;
import com.meteor.controller.WebAction;
import com.meteor.kit.PageKit;

/**
 * API引导式配置
 */
public class MainConfig extends JFinalConfig {
	public static String tmpsavedir;
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		PageKit.updateProp();
		me.setDevMode(PropKit.getBoolean("devMode", false));
		me.setViewType(ViewType.JSP);// 设置视图类型为Jsp，否则默认为FreeMarker

		tmpsavedir=PropKit.get("tmpsavedir");
		tmpsavedir=System.getProperty("catalina.home")+tmpsavedir;

		me.setUploadedFileSaveDirectory(tmpsavedir);
		me.setError404View("/WEB-INF/manager/error.jsp");
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", WebAction.class, "/WEB-INF/manager");
		me.add("/manager", BaseAction.class,"/WEB-INF/manager");
		me.add("/interface", HttpInterfaceAction.class);
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		// 配置DruidPlugin数据库连接池插件
		DruidPlugin  druidPlugin = new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim(),PropKit.get("jdbcdriver"));
		me.add(druidPlugin);
		
		// 配置ActiveRecord插件
		ActiveRecordPlugin arppgsql = new ActiveRecordPlugin("pgsql",druidPlugin);
		me.add(arppgsql);
		//arppgsql.setShowSql(true);
		arppgsql.setDialect(new PostgreSqlDialect());

		//配置定时任务插件
		QuartzPlugin quartzPlugin =  new QuartzPlugin("job.properties");
		me.add(quartzPlugin);
		
//		// 配置DruidPlugin数据库连接池插件
//		DruidPlugin  druidPlugin1 = new DruidPlugin(PropKit.get("jdbcUrl1"), PropKit.get("user1"), PropKit.get("password1").trim(),PropKit.get("jdbcdriver1"));
//		me.add(druidPlugin1);
//		
//		// 配置ActiveRecord插件
//		ActiveRecordPlugin arppgsql1 = new ActiveRecordPlugin("pgsqlbk",druidPlugin1);
//		me.add(arppgsql1);
//		//arppgsql.setShowSql(true);
//		arppgsql1.setDialect(new PostgreSqlDialect());
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		String rootdir=PropKit.get("rootdir");
		if(StringUtils.isNotBlank(rootdir)) {
			me.add(new ContextPathHandler(rootdir));
		}
	}
	
	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("WebRoot", 81, "/kan", 5);
	}
}
