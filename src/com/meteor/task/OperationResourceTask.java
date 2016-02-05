package com.meteor.task;

import com.meteor.common.MainConfig;
import com.meteor.kit.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OperationResourceTask implements Job{
	private final Logger logger = LoggerFactory.getLogger(OperationResourceTask.class);

	private void delrepeated(){
		PageKit.delrepeated();
	}


	private void reTobase64(){
		try {
			PageKit.tobase64();
		} catch (Exception e) {
			logger.error("图片转换异常: " + e.toString());
		}
		//清空临时文件夹
		String tmpdir= MainConfig.tmpsavedir;//PropKit.get("tmpsavedir");
		File[] files=new File(tmpdir).listFiles();
		for(File file:files){
			try {
				file.delete();
			}catch (Exception e) {
				//忽略异常
			}
		}
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		delrepeated();
		reTobase64();
	}
}
