/**
 * 
 */
package com.meteor.kit;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.meteor.model.vo.InExl;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author justlikemaki
 *
 */
public class CzExlKit {
	public static void main(String[] args) throws Exception {
		writeExl("G:/XE/new","G:/XE/new/newsrc.xls","1");
	}
	
	public static void writeExl(String readpath,String writepath,String cdlx) throws Exception{
			List<InExl> filelist=new ArrayList();
			String strPath=readpath;
			List<InExl> inlist=refreshFileList(filelist,strPath);
			wirteex(inlist,writepath,cdlx);
	}
	
	/**
	 * 
	 * @标题: CzExlUtils.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：读取exl，返回遍历的对象
	 */
	public static List<InExl> readexl(InputStream is) throws Exception{
		Workbook wb=Workbook.getWorkbook(is);
        // 获取这个excel文件的第1个表；  
        Sheet sheet = wb.getSheet(0);
        // 得到行数
        int rows=sheet.getRows();
        // 得到列数
        int columns=sheet.getColumns();
        // 通过行和列值得到确定的单元格    
        List<InExl> inelist=new ArrayList();
        for (int i = 1; i < rows; i++) { 
        	InExl ine=new InExl();
        	for (int j = 0; j < columns; j++) {
        		Cell c = sheet.getCell(j, i);
                // 可以直接读取单元格的内容。不管此单元格式什么类型的。字符串型的还是数字型的。还是标签性的。得到的内容都是字符串型的；      		
                String content = c.getContents();
                if(j == 0){
                	ine.setParentDirMc(content);
                }
                if(j == 1){
                    ine.setPparentDirMc(content);
                }
                if(j == 2){
                    ine.setFilePath(content.split(",,,"));
                }
                if(j == 3){
                    ine.setFilename(content.split(",,,"));
                }
                if(j == 4){
                    ine.setType(content);
                }
                if(j == 5){
                    ine.setTypeDirMc(content);
                }
                if(j == 6){
                    ine.setIsdown(content);
                }
			}
        	//文件路径不为空才添加
        	if(StringUtils.isNotBlank(ine.getFilename()[0])){
        		inelist.add(ine);
        	}
        }  
        wb.close();
		return inelist;
	}
	
	/**
	 * 
	 * @throws java.text.ParseException
	 * @标题: CzExlUtils.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：把遍历的结果写入exl
	 */
	public static void wirteex(List<InExl> inlist,String destpath,String cdlx) throws Exception{
		WritableWorkbook wb=   Workbook.createWorkbook(new File(destpath));
	    WritableSheet ws= wb.createSheet("sheet1", 0);
	    WritableFont wf=new WritableFont( WritableFont.ARIAL,10); //设置字体大小
	    WritableCellFormat cf=new WritableCellFormat(wf);
	    
	    Label la0=new Label(0,0,"上级目录",cf); 
	    ws.addCell(la0);	   
	    ws.setColumnView(0, 40); //设置列宽//width为字符数   
	    Label la1=new Label(1,0,"上上级目录",cf); 
	    ws.addCell(la1);
	    ws.setColumnView(1, 10);  
	    Label la2=new Label(2,0,"文件列表",cf); 
	    ws.addCell(la2);
	    ws.setColumnView(2, 60); 
	    Label la3=new Label(3,0,"文件名称列表",cf); 
	    ws.addCell(la3);
	    ws.setColumnView(3, 60);
	    Label la4=new Label(4,0,"影片分类",cf); 
	    ws.addCell(la4);
	    ws.setColumnView(4, 10);
	    Label la5=new Label(5,0,"影片类型",cf); 
	    ws.addCell(la5);
	    ws.setColumnView(5, 10);
	    Label la6=new Label(6,0,"是否存档",cf); 
	    ws.addCell(la6);
	    ws.setColumnView(6, 10);  
	    //设置行高
        ws.setRowView(0, 400,false);
        String tyy=cdlx;//0未存档，1硬盘存档，2网络存档
	    for(int i = 1; i <= inlist.size(); i++) { 
	    	InExl ine=inlist.get(i-1);
	    	//设置第1个单元格
	    	Label la00=new Label(0,i,ine.getParentDirMc(),cf); 
		    ws.addCell(la00);
		    //设置第2个单元格
		    Label la01=new Label(1,i,ine.getPparentDirMc(),cf); 
		    ws.addCell(la01);
		    //设置第3个单元格
		    StringBuffer sb1=new StringBuffer();
		    for(int j=0;j<ine.getFilePath().length;j++){
		    	if(j+1==ine.getFilePath().length){		    		
		    			sb1.append(ine.getFilePath()[j]);			
		    	}else{
		    			sb1.append(ine.getFilePath()[j]+",,,");	
		    	}
		    }
		    Label la02=new Label(2,i,sb1.toString(),cf); 
		    ws.addCell(la02);
		    //设置第4个单元格
		    StringBuffer sb2=new StringBuffer();
		    for(int j=0;j<ine.getFilename().length;j++){
		    	if(j+1==ine.getFilename().length){
		    		sb2.append(ine.getFilename()[j]);
		    	}else{
		    		sb2.append(ine.getFilename()[j]+",,,");
		    	}
		    }
		    Label la03=new Label(3,i,sb2.toString(),cf); 
		    ws.addCell(la03);
		    //设置第5个单元格
		    Label la04=new Label(4,i,"classical",cf);//此处更改影片分类
		    ws.addCell(la04);
		    //设置第6个单元格
		    Label la05=new Label(5,i,ine.getTypeDirMc(),cf);
		    ws.addCell(la05);
		    //设置第7个单元格
		    Label la06=new Label(6,i,tyy,cf);
		    ws.addCell(la06);
		    
        	ws.setRowView(i, 400,false);//height为一个点的1/20
	    }
	    wb.write();  
	    wb.close();  
	    System.out.println("创建完成@！");  
	}
	
	public static boolean isTorAndImg(String path){
		if((path.indexOf(".torrent")>-1)||(path.indexOf(".jpg")>-1)||(path.indexOf(".jpeg")>-1)||(path.indexOf(".png")>-1)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 
	 * @标题: CzExlUtils.java 
	 * @版权: Copyright (c) 2014
	 * @公司: VETECH
	 * @作者：LF
	 * @时间：2014-8-9
	 * @版本：1.0
	 * @方法描述：遍历文件夹，得到exl对象
	 */
	public static List<InExl> refreshFileList(List<InExl> filelist,String strPath) {
        File dir = new File(strPath); 
        File[] files = dir.listFiles(); 
        if (files == null || files.length==0){ 
            return filelist;
        }
        String parentName=files[0].getParentFile().getName();
        String pparentName=files[0].getParentFile().getParentFile().getName();
        String typeName=files[0].getParentFile().getParentFile().getParentFile().getName();
		List<String> filePath=new ArrayList();//当前文件绝对路径
		List<String> filename=new ArrayList();//文件名称
		InExl in=new InExl();
		in.setParentDirMc(parentName);
		in.setPparentDirMc(pparentName);
		in.setTypeDirMc(typeName);
        for (int i = 0; i < files.length; i++) { 
            if (files[i].isDirectory()) { 
                refreshFileList(filelist,files[i].getAbsolutePath()); 
            } else {   
            	if(isTorAndImg(files[i].getAbsolutePath().toLowerCase())){
	            	String thisName=files[i].getName();            	
	                String strFilePath = files[i].getAbsolutePath().toLowerCase();                
	                filePath.add(strFilePath);                             
	                filename.add(thisName); 
            	}
            } 
        }
        in.setFilePath(filePath.toArray(new String[filePath.size()]));
        in.setFilename(filename.toArray(new String[filename.size()]));
        filelist.add(in);
		return filelist; 
	} 

}
