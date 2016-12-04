package com.meteor.kit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.meteor.model.po.javimg;
import com.meteor.model.vo.SearchQueryP;

/**
 * Created by Meteor
 * @category (这里用一句话描述这个类的作用)
 */
public class PgsqlKit {
    private static String operator=" GT, LT, GTE, LTE, NOT,NOTNULL,ISNULL,LIKE";
    private static Map opmap=new HashMap();
    static{
        opmap.put("GT"," > ");
        opmap.put("LT"," < ");
        opmap.put("GTE"," >= ");
        opmap.put("LTE"," <= ");
        opmap.put("NOT"," != ");
        opmap.put("IS"," = ");
        opmap.put("NOTNULL"," NOTNULL ");
        opmap.put("ISNULL"," ISNULL ");
        opmap.put("LIKE"," LIKE ");
    }

    private  static String getClazzName(Class<?> clazz){
        String objName=clazz.getName();
        objName=objName.substring(objName.lastIndexOf(".")+1);
        return objName;
    }

    private  static String addQueryParams(Map p){
        if(p!=null && p.size()>0){
            StringBuffer sb=new StringBuffer();
            sb.append(" where 1=1 ");
            Iterator it = p.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pairs = (Map.Entry)it.next();
                if(pairs.getValue()!=null&& StringUtils.isNotBlank(pairs.getValue() + "")){
                    String opkey=pairs.getKey().toString().split("_")[0];
                    if(operator.contains(opkey)){
                        if(opkey.contains("NULL")){
                            sb.append(" and " + pairs.getKey().toString().split("_")[1] +" "+ opmap.get(opkey));
                        }else if(opkey.contains("LIKE")){
                            sb.append(" and " + pairs.getKey().toString().split("_")[1] + opmap.get(opkey) + "'%" + pairs.getValue().toString() + "%'");
                        }else{
                            sb.append(" and " + pairs.getKey().toString().split("_")[1] + opmap.get(opkey) + " '" + pairs.getValue().toString() + "'");
                        }
                    }else {
                        if (pairs.getKey().equals("id") || pairs.getKey().equals("tabtype") || pairs.getKey().equals("type")) {
                            sb.append(" and " + pairs.getKey().toString() + " = '" + pairs.getValue().toString() + "'");
                        } else if (pairs.getKey().equals("tags")) {
                            String sekey=pairs.getValue().toString();
                            sekey=sekey.toUpperCase();
                            sb.append(" and ( UPPER(" + pairs.getKey().toString() + ") like '%\"" + sekey + "%'");
                            sb.append(" or UPPER(sbm) like '%" + sekey + "%'");
                            sb.append(" or UPPER(title) like '%" + sekey + "%' ) ");
                        } else {
                            sb.append(" and " + pairs.getKey().toString() + " like '%" + pairs.getValue().toString() + "%'");
                        }
                    }
                }
            }
            return sb.toString();
        }else{
            return "";
        }
    }
    
//    public static void savebk(String collectionName,Object obj) throws Exception{
//        Record rec=new Record();
//        Map p= JsonKit.json2Bean(JsonKit.bean2JSON(obj),HashMap.class);
//        rec.setColumns(p);
//        Db.use("pgsqlbk").save(collectionName,rec);
//    }
//    
//    public static void saveImg(Record rd){
//    	String img = rd.get("imgsrc");
//    	String oneid = rd.get("id");
//    	if(img != null && img.startsWith(PageKit.getimgBase64Tip())){		
//    		try {
//    			img = img.replace(PageKit.getimgBase64Tip(), "");
//    			javimg ji=new javimg(oneid,img);
//				PgsqlKit.savebk(ClassKit.javimgTableName,ji);
//				rd.set("imgsrc",PageKit.getimgBase64Key()+ji.getId());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
//    	return;
//    }
//    
//    public static void tobk(int nowpage,int count,StringBuffer sb){
//    	int offset=(nowpage-1)*count;
//    	String sql = "select * from javsrc limit "+count+" offset "+offset;
//    	try {
//    		List<Record> list = Db.use("pgsql").find(sql);
//    		for (Record rd : list) {
//    			String newid =rd.get("id")+RandomStringUtils.randomNumeric(6);
//    			rd.set("id",newid);
//    			saveImg(rd);
//    			Db.use("pgsqlbk").save("javsrc", rd);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println(sql);
//			sb.append(e.getMessage()).append("\r\n");
//			sb.append(sql).append("\r\n");
//		}
//    }

    public static List findall(Class<?> clazz,Map p) throws Exception {
        String objName=getClazzName(clazz);
        String queryParams=addQueryParams(p);
        List<Record> list=Db.find("select * from " + objName+queryParams +" order by id desc");
        return BeanKit.copyRec(list,clazz);
    }

    public static Object findById(Class<?> clazz, String id) throws Exception {
        String objName=getClazzName(clazz);
        Record rec=Db.findById(objName,id);
        Object obj=clazz.newInstance();
        BeanUtils.copyProperties(obj,rec.getColumns());
        return obj;
    }

    public static Map findByCondition(Class<?> clazz,SearchQueryP p) throws Exception {
        String objName=getClazzName(clazz);
        String queryParams=addQueryParams(p.getParameters());
        String sortstr="";
        if(objName.equals("companys")){
            sortstr=" order by id desc";
        }else {
            if (p.getSorttype() == null || p.getSortname() == null) {
                sortstr = " order by times desc , id desc ";
            } else {
                sortstr = " order by " + p.getSortname() + " " + p.getSorttype()+", id desc ";
            }
        }
        String sql="from " + objName + " " + queryParams + sortstr;
        Map res=new HashMap();
        if(p.getNowpage()!=0) {
            Page<Record> pr = Db.paginate(p.getNowpage(), p.getCount(), "select * ",sql );
            res.put("list",BeanKit.copyRec( pr.getList(),clazz));
            res.put("count",pr.getTotalRow());
            res.put("select", p.getCount());
            return res;
        }else{
        	List l = findByConditionAll(clazz,sql);
            res.put("list",l);
            res.put("count",0);
            res.put("select", l.size());
            return res;
        }
    }

    private static List findByConditionAll(Class<?> clazz,String sql) throws Exception {
        List<Record> list= Db.find("select * "+sql);
        return BeanKit.copyRec(list,clazz);
    }

    public static List findByConditionAll(Class<?> clazz,SearchQueryP p) throws Exception {
        String objName=getClazzName(clazz);
        String queryParams=addQueryParams(p.getParameters());
        String sortstr="";
        if(objName.equals("companys")){
            sortstr=" order by id desc";
        }else {
            if (p.getSorttype() == null || p.getSortname() == null) {
                sortstr = " order by times desc , id desc ";
            } else {
                sortstr = " order by " + p.getSortname() + " " + p.getSorttype()+", id desc ";
            }
        }
        String sql="from " + objName + " " + queryParams + sortstr;
        if(p.getNowpage()!=0) {
            int offset=(p.getNowpage()-1)*p.getCount();
            String limit="LIMIT "+p.getCount()+" OFFSET "+offset;
            sql=sql+limit;
        }
        return findByConditionAll(clazz,sql);
    }

    public static int updateById(String collectionName,Map p) throws Exception {
        Record rec=new Record();
        rec.setColumns(p);
        boolean flag= Db.update(collectionName, rec);
        if(true){
            return 1;
        }else{
            return 0;
        }
    }

    public static void save(String collectionName,Object obj) throws Exception{
        Record rec=new Record();
        Map p= JsonKit.json2Bean(JsonKit.bean2JSON(obj),HashMap.class);
        rec.setColumns(p);
        Db.save(collectionName,rec);
    }

    public static int deleteById(String collectionName,String id) throws Exception{
        boolean flag=Db.deleteById(collectionName,id);
        if(true){
            return 1;
        }else{
            return 0;
        }
    }

    public static int deleteByIdlist(String collectionName, String[] ids) throws Exception{
        int count=0;
        for(String id:ids){
            boolean flag=Db.deleteById(collectionName,id);
            if(true){
                count+=1;
            }
        }
       return count;
    }

    public static int deleteByCondition(String collectionName,Map p) throws Exception{
        Record rec=new Record();
        rec.setColumns(p);
        boolean flag=Db.delete(collectionName, rec);
        if(true){
            return 1;
        }else{
            return 0;
        }
    }

    public static void deleteCollectionAllData(String collectionName) throws Exception{
       try {
           Db.query("delete from " + collectionName);
       }catch (Exception e){
           //e.printStackTrace();
       }
    }

    public static Object getMax(Class<?> clazz, String fieldName,Map p) throws Exception{
        String objName=getClazzName(clazz);
        String queryParams=addQueryParams(p);
        String sortstr=" order by "+fieldName+" desc";
        List<Record> recs= Db.paginate(1, 1, "select * ", "from " + objName + " " + queryParams+sortstr).getList();
        Record res=null;
        if(recs.size()>0) {
            res=recs.get(0);
        }else{
            return res;
        }
        Object obj=clazz.newInstance();
        BeanUtils.copyProperties(obj,res.getColumns());
        return obj;
    }

    public static Object getMin(Class<?> clazz, String fieldName,Map p) throws Exception{
        String objName=getClazzName(clazz);
        String queryParams=addQueryParams(p);
        String sortstr=" order by "+fieldName+" asc";
        List<Record> recs= Db.paginate(1, 1, "select * ", "from " + objName + " " + queryParams + sortstr).getList();
        Record res=null;
        if(recs.size()>0) {
            res=recs.get(0);
        }else{
            return res;
        }
        Object obj=clazz.newInstance();
        BeanUtils.copyProperties(obj,res.getColumns());
        return obj;
    }

    public static long getCollectionCount(String collectionName, Map p)  throws Exception{
        String sql="select count(id) from "+collectionName+" ";
        String queryParams=addQueryParams(p);
        sql=sql+queryParams;
        return  Db.queryLong(sql);
    }

    public static void excuteSql(String sql){
        try {
            Db.query(sql);
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    public static <T> List<T> excuteSqlWithData(String sql){
        try {
            return Db.query(sql);
        }catch (Exception e){
            //e.printStackTrace();
            return null;
        }
    }
}
