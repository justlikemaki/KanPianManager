package com.meteor.kit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @ClassName FileOperate
 * @author Meteor
 * @date 2015年8月8日 下午11:29:54
 * @category 文件操作工具类
 */
public class FileOperateKit {
	private String message;

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:30:14
	 * @Title readText
	 * @param filePathAndname
	 * @param lastrow
	 * @param encoding
	 * @return String 返回类型
	 * @category 读取文件最后lastrow
	 */
	public String readText(String filePathAndname, int lastrow, String encoding) {
		RandomAccessFile rf = null;
		StringBuffer out = new StringBuffer();
		if (StringUtils.isBlank(encoding)) {
			encoding = "UTF-8";
		}
		try {
			rf = new RandomAccessFile(filePathAndname, "r");
			long len = rf.length();
			long start = rf.getFilePointer();
			long nextend = start + len - 1;
			String line;
			rf.seek(nextend);
			int c = -1;
			while (nextend > start) {
				c = rf.read();
				if (c == '\n' || c == '\r') {
					line = rf.readLine();
					nextend--;
					if (line == null) {// 处理文件末尾是空行这种情况
						rf.seek(nextend);
						continue;
					}
					lastrow--;
					line = new String(line.getBytes("8859_1"), encoding);//
					out.append(line + "\n");
				}
				nextend--;
				rf.seek(nextend);
				if (nextend == 0) {// 当文件指针退至文件开始处，输出第一行
					line = rf.readLine();
					line = new String(line.getBytes("8859_1"), encoding);//
					out.append(line + "\n");
				}
				if (lastrow <= 0) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rf != null)
					rf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return out.toString();
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:35:47
	 * @Title readTxt
	 * @param filePathAndName
	 * @return String 返回类型
	 * @category 读取文本 默认utf8
	 */
	public String readTxt(String filePathAndName) {
		return readTxt(filePathAndName, "UTF-8");
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:35:47
	 * @Title readTxt
	 * @param filePathAndName
	 * @return String 返回类型
	 * @category 读取文本 指定编码
	 */
	public String readTxt(String filePathAndName, String encoding) {
		encoding = encoding.trim();
		StringBuffer str = new StringBuffer("");
		String st = "";
		try {
			FileInputStream fs = new FileInputStream(filePathAndName);
			InputStreamReader isr;
			if (encoding.equals("")) {
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding);
			}
			BufferedReader br = new BufferedReader(isr);
			try {
				String data = "";
				while ((data = br.readLine()) != null) {
					str.append(data + "\n");
				}
			} catch (Exception e) {
				str.append(e.toString());
			} finally {
				br.close();
				isr.close();
			}
			st = str.toString();
		} catch (IOException es) {
			st = "";
		}
		return st;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:36:20
	 * @Title writeTxt
	 * @param path
	 * @param filename
	 * @param fileContent void 返回类型
	 * @category 写文本 默认utf8
	 */
	public void writeTxt(String path, String filename, String fileContent) {
		writeTxt(path, filename, fileContent, "UTF-8");

	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:36:20
	 * @Title writeTxt
	 * @param path
	 * @param filename
	 * @param fileContent void 返回类型
	 * @category 写文本 指定编码
	 */
	public void writeTxt(String path, String filename, String fileContent, String encoding) {
		PrintWriter pwrite = null;
		try {
			File file = new File(path);
			if (StringUtils.isNotBlank(filename)) {
				if (!file.exists()) {
					file.mkdirs();
				}
				file = new File(path + "/" + filename);
			}

			if (encoding != null && !"".equals(encoding)) {
				pwrite = new PrintWriter(file, encoding);
			} else {
				pwrite = new PrintWriter(file);
			}
			pwrite.println(fileContent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pwrite != null) {
				pwrite.close();
			}
		}
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:36:55
	 * @Title copyDir
	 * @param sourceDir
	 * @param targetDir
	 * @throws Exception void 返回类型
	 * @category 复制文件夹
	 */
	public void copyDir(String sourceDir, String targetDir) throws Exception {
		String url1 = sourceDir;
		String url2 = targetDir;
		if (!(new File(url2)).exists()) {
			(new File(url2)).mkdirs();
		}
		File[] file = (new File(url1)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				file[i].toString();
				FileInputStream input = new FileInputStream(file[i]);
				FileOutputStream output = new FileOutputStream(url2 + "/" + (file[i].getName()).toString());
				byte[] b = new byte[1024 * 5];
				int len;
				while ((len = input.read(b)) != -1) {
					output.write(b, 0, len);
				}
				output.flush();
				output.close();
				input.close();
			} else if (file[i].isDirectory()) {
				String url_2_dir = url2 + "/" + file[i].getName();
				copyDir(file[i].getPath(), url_2_dir);
			}
		}
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:37:23
	 * @Title createFolder
	 * @param folderPath
	 * @return String 返回类型
	 * @category 创建文件夹
	 */
	public String createFolder(String folderPath) {
		String txt = folderPath;
		try {
			File myFilePath = new File(txt);
			txt = folderPath;
			if (!myFilePath.exists()) {
				myFilePath.mkdirs();
			}
		} catch (Exception e) {
			message = " 错误 ";
		}
		return txt;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:38:33
	 * @Title delFile
	 * @param filePathAndName
	 * @return boolean 返回类型
	 * @category 删除文件
	 */
	public boolean delFile(String filePathAndName) {
		boolean bea = false;
		try {
			String filePath = filePathAndName;
			File myDelFile = new File(filePath);
			if (myDelFile.exists()) {
				myDelFile.delete();
				bea = true;
			} else {
				bea = false;
				message = (filePathAndName);
			}
		} catch (Exception e) {
			message = e.toString();
		}
		return bea;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:38:58
	 * @Title delFolder
	 * @param folderPath void 返回类型
	 * @category 删除文件夹
	 */
	public void delFolder(String folderPath) {
		try {
			delAllFile(folderPath);
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete();
		} catch (Exception e) {
			message = ("");
		}
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:39:11
	 * @Title delAllFile
	 * @param path
	 * @return boolean 返回类型
	 * @category 删除所有文件
	 */
	public boolean delAllFile(String path) {
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);
				delFolder(path + "/" + tempList[i]);
				bea = true;
			}
		}
		return bea;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:39:29
	 * @Title delAllFile
	 * @param path
	 * @param min
	 * @return boolean 返回类型
	 * @category 删除多少分钟前没有变化的文件
	 */
	public boolean delAllFile(String path, int min) {
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				if (System.currentTimeMillis() - 1000 * 60 * min > temp.lastModified()) {
					temp.delete();
				}
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i], min);
				delFolder(path + "/" + tempList[i], min);
				bea = true;
			}
		}
		return bea;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:39:29
	 * @Title delAllFile
	 * @param folderPath
	 * @param min
	 * @return boolean 返回类型
	 * @category 删除多少分钟前没有变化的文件 并删除文件夹
	 */
	public void delFolder(String folderPath, int min) {
		try {
			delAllFile(folderPath, min);
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete();
		} catch (Exception e) {
			message = ("");
		}
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:41:30
	 * @Title copyFile
	 * @param oldPathFile
	 * @param newPathFile
	 * @return String 返回类型
	 * @category 按路径复制文件
	 */
	public String copyFile(String oldPathFile, String newPathFile) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPathFile);
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
			return "";
		} catch (Exception e) {
			return ("错误") + e.getMessage();
		}
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:41:30
	 * @Title copyFile
	 * @param fis
	 * @param fos
	 * @return String 返回类型
	 * @category 按数据流复制文件
	 */
	public String copyFile(InputStream fis, FileOutputStream fos) {
		try {
			int bytesum = 0;
			int byteread = 0;
			byte[] buffer = new byte[1444];
			while ((byteread = fis.read(buffer)) != -1) {
				bytesum += byteread;
				fos.write(buffer, 0, byteread);
			}
			fis.close();
			fos.close();
			return "";
		} catch (Exception e) {
			return ("错误") + e.getMessage();
		}
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:42:22
	 * @Title moveFile
	 * @param oldPath
	 * @param newPath void 返回类型
	 * @category 移动文件
	 */
	public void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:42:34
	 * @Title moveFolder
	 * @param sourceDir
	 * @param targetDir
	 * @throws Exception void 返回类型
	 * @category 移动文件夹
	 */
	public void moveFolder(String sourceDir, String targetDir) throws Exception {
		copyDir(sourceDir, targetDir);
		delFolder(sourceDir);
	}

	public String getMessage() {
		return this.message;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:44:00
	 * @Title zip
	 * @param inputFileName
	 * @param zipFileName
	 * @throws Exception void 返回类型
	 * @category 压缩文件
	 */
	public void zip(String inputFileName, String zipFileName) throws Exception {
		zip(zipFileName, new File(inputFileName));
	}

	private void zip(String zipFileName, File inputFile) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		zip(out, inputFile, "");
		System.out.println("zip done");
		out.close();
	}

	private void zip(ZipOutputStream out, File f, String base) throws Exception {
		if (f.isDirectory()) { // 判断是否为目录
			File[] fl = f.listFiles();
			out.putNextEntry(new ZipEntry(base + "/"));
			base = base.length() == 0 ? "" : base + "/";
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + fl[i].getName());
			}
		} else { // 压缩目录中的所有文件
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(f);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			in.close();
		}
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:44:26
	 * @Title Ectract
	 * @param sZipPathFile
	 * @param sDestPath
	 * @return
	 * @throws Exception ArrayList 返回类型
	 * @category 解压zip文件
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList Ectract(String sZipPathFile, String sDestPath) throws Exception {
		ArrayList allFileName = new ArrayList();
		File f = new File(sZipPathFile);
		if (!f.exists() || !f.isFile()) {
			return null;
		}
		// 为空表示在当前目录下解压
		if (StringUtils.isBlank(sDestPath)) {
			sDestPath = f.getParent();
		}
		FileInputStream fins = new FileInputStream(sZipPathFile);
		ZipInputStream zins = new ZipInputStream(fins);
		ZipEntry ze = null;
		byte ch[] = new byte[1024];
		while ((ze = zins.getNextEntry()) != null) {
			File zfile = new File(sDestPath + "/" + ze.getName());
			File fpath = new File(zfile.getParentFile().getPath());
			if (ze.isDirectory()) {
				if (!zfile.exists())
					zfile.mkdirs();
				zins.closeEntry();
			} else {
				if (!fpath.exists())
					fpath.mkdirs();
				FileOutputStream fouts = new FileOutputStream(zfile);
				int i;
				allFileName.add(zfile.getAbsolutePath());
				while ((i = zins.read(ch)) != -1)
					fouts.write(ch, 0, i);
				zins.closeEntry();
				fouts.close();
			}
		}
		fins.close();
		zins.close();

		return allFileName;
	}

	/**
	 *
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:45:08
	 * @Title saveToFile
	 * @param destUrl
	 * @param fileName
	 * @throws java.io.IOException void 返回类型
	 * @category 将HTTP资源另存为文件
	 */
	public static void saveToFile(String destUrl, String fileName) throws IOException {
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		URL url = null;
		byte[] buf = new byte[1024];
		int size = 0;
		// 建立链接
		url = new URL(destUrl);
		httpUrl = (HttpURLConnection) url.openConnection();
		// 连接指定的资源
		httpUrl.connect();
		// 获取网络输入流
		bis = new BufferedInputStream(httpUrl.getInputStream());
		// 建立文件
		fos = new FileOutputStream(fileName);

		// 保存文件
		while ((size = bis.read(buf)) != -1)
			fos.write(buf, 0, size);
		fos.close();
		bis.close();
		httpUrl.disconnect();
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午11:45:42
	 * @Title Fixutfhead   
	 * String rootString = "D:\\workspace\\asms5000\\WebRoot"; File file = new File(rootString);
	 * Fixutfhead(file);
	 * @param file void 返回类型
	 * @category 去掉文件中utf编码乱码 -17 -69 -65
	 */
	public static void Fixutfhead(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				Fixutfhead(f);
			}
		}
		if (file.getName().endsWith("jsp")) {
			try {
				RandomAccessFile rf = new RandomAccessFile(file, "r");
				byte[] b = new byte[3];
				rf.read(b);
				if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
					System.out.println("找到" + file.getPath());
					rf.seek(3); // 利用RandomAccessFile定位到第3个字节，之后再读文件
					List<byte[]> list = new ArrayList<byte[]>();
					byte[] b2 = new byte[1024];
					int size = 0;
					while ((size = rf.read(b2)) != -1) {
						byte[] b3 = new byte[size];
						System.arraycopy(b2, 0, b3, 0, size);
						list.add(b3); // 将所读取出来的内容以byte数组为单位存放到一个ArrayList当中
						b2 = new byte[1024];
					}
					rf.close();

					FileOutputStream outFile = new FileOutputStream(file);
					for (Iterator<byte[]> i = list.iterator(); i.hasNext();) {
						outFile.write(i.next()); // 将ArrayList里的内容重新写回之前的文件
					}
					outFile.close();
					Fixutfhead(file);// 再重复一次 防止有多个乱码的情况
				} else {
					rf.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 下午1:07:27
	 * @Title fastCopyFile
	 * @param rootpath 文件夹根目录
	 * @param saveDir  是否保存目录结构 0 不保存，1保存
	 * @param newpath  新的文件根路径
	 * @param repath   需要替换的路径，保存目录结构时非空值
	 * @param containted  过滤出包含指定内容的文件，空值时不过滤
	 * @category 多线程快速复制文件
	 */
	public static void fastCopyFile(String rootpath, final int saveDir,final String newpath,final String repath,final String containted){
		if(saveDir==1 && StringUtils.isBlank(repath)){
			System.out.println("没有设置替换路径");
		}else{						
			// 能容纳100个文件
			final BlockingQueue<File> queue = new LinkedBlockingQueue<File>(100);
			// 线程池
			final ExecutorService exec = Executors.newFixedThreadPool(5);
			// 根路径
			final File root = new File(rootpath);
			// 完成标志
			final File exitFile = new File("");
			// 读个数
			final AtomicInteger rc = new AtomicInteger();
			// 写个数
			final AtomicInteger wc = new AtomicInteger();
			
			// 读线程
			Runnable read = new Runnable() {
				public void run() {
					scanFile(root);
					scanFile(exitFile);
				}
	
				public void scanFile(File file) {
					if (file.isDirectory()) {
						File[] files = file.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								if(StringUtils.isNotBlank(containted)){
									//System.out.println(pathname.isDirectory());
									//System.out.println(pathname.isDirectory() || pathname.getPath().contains(containted));
									return pathname.isDirectory() || pathname.getPath().contains(containted);
								}else{
									return pathname.isDirectory() || pathname.isFile();
								}
							}
						});
						for (File one : files)
							scanFile(one);
					} else {
						try {
							int index = rc.incrementAndGet();
							//System.out.println("Read0: " + index + " " + file.getPath());
							queue.put(file);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			exec.submit(read);
			// 四个写线程
			for (int index = 0; index < 4; index++) {
				// write thread
				final int NO = index;
				Runnable write = new Runnable() {
					String threadName = "Write" + NO;
					public void run() {
						while (true) {
							try {
								//Thread.sleep(randomTime());
								int index = wc.incrementAndGet();
								File file = queue.take();
								// 队列已经无对象
								if (file == exitFile) {
									// 再次添加"标志"，以让其他线程正常退出
									queue.put(exitFile);
									System.out.println(threadName+"---end");
									break;
								}
								String filepath=file.getPath();
								//System.out.println(threadName + ": " + index + " " + filepath);
								//执行复制文件操作
								String thenew="";
								if(saveDir==1){
									String pre=repath;
									String next=newpath;
									thenew=filepath.replace(pre, next);
								}else{
									thenew=newpath+file.getName();
								}
								if(!filepath.equals(thenew)){
									FileUtils.copyFile(new File(filepath), new File(thenew));
								}
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				exec.submit(write);
			}
			exec.shutdown();	
		}
	}
	
	public static void main(String[] args) {
		//fastCopyFile("D:\\Development\\WorkSpace\\eclipse\\babyplan_java",1,"D:\\11","D:\\Development\\WorkSpace\\eclipse\\babyplan_java","");
		//String p1="D:\\Development\\Servers\\Tomcats\\Tomcat7-JAV\\webapps\\javsrc";
		//String p2="F:\\DATA\\!!SSDBK\\Development\\Servers\\Tomcats\\Tomcat7-JAV\\webapps\\javsrc";
		//String p3="D:\\Development\\文档+架构包";
		//String p4="G:\\DATA\\!!SSDBK\\Development\\文档+架构包";
		//fastCopyFile(p1,1,p2,p1,"");
		new FileOperateKit().loopDelEmptyFolder("D:\\Development\\Servers\\Tomcats\\apache-tomcat-7.0.59\\wtpwebapps\\tt");
	}
	
	public void delFileList(List<String> filePath) {
		for (String path : filePath) {
			try {
				File myFilePath = new File(path);
				myFilePath.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public boolean loopDelEmptyFolder(String folderPath){
		try {
			File file = new File(folderPath);
			if (!file.exists()) {
				return false;
			}
			File[] tempList = file.listFiles();
			for (File f:tempList) {
				if(f.isDirectory()){
					boolean flag = loopDelEmptyFolder(f.getAbsolutePath());
					if(flag){
						delFolder(f.getAbsolutePath());
					}
					return flag;
				}else{
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
