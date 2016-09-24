package com.bj.hk.util;

import org.apache.commons.lang.xwork.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * pdm中的表结构转entity工具
 * 使用：
 *  1、新建一个txt文件
 *  2、将pdm中的Preview建表语句全选-复制-粘贴到txt文件
 *  3、用此程序读取该文件，控制台会输出相应的结果
 *  4、复制结果粘贴到新建的entity中
 * @Author hukai
 * @Email 614811431@qq.com
 * @Date 2016/6/15 8:48
 */
public class CreateEntityFromTxt {

    public CreateEntityFromTxt(String filePath) {
        try {

            File file=new File(filePath);

            String encoding="UTF-8";

            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                int index = 0;
                int startFieldNum = 0;//记录开始处理行数
                int endFieldNum = 0;//记录结束处理的行数
                List<String> fieldAllStrList = new ArrayList<String>();//该行语句数组，如：private String aa;//
                List<String> fieldStrList = new ArrayList<String>();//该行字段数组，如：aa
                int jj = 0;//记录该注释是哪一行的注释
                boolean nextIsNotes = false;//下一行是否是注释

                while((lineTxt = bufferedReader.readLine()) != null){
                    index++;
                    //查找开始创建表的位置
                    if(lineTxt.indexOf("create table")!=-1){
                        startFieldNum = index;
                    }
                    if(lineTxt.indexOf("primary key")!=-1){
                        endFieldNum = index;
                    }

                    if(startFieldNum!=0&&index>startFieldNum+1
                            &&(endFieldNum==0||index<endFieldNum)){

                        int endIndexOf = 0;//记录这一行该字段名的结束位置
                        String fieldType = "String";//记录改行字段的类型

                        if(lineTxt.indexOf("VARCHAR2")>0) {
                            endIndexOf = lineTxt.indexOf("VARCHAR2");
                            fieldType = "String";
                        }else if(lineTxt.indexOf("DATE")>0) {
                            endIndexOf = lineTxt.indexOf("DATE");
                            fieldType = "Date";
                        }else if(lineTxt.indexOf("CHAR")>0) {
                            endIndexOf = lineTxt.indexOf("CHAR");
                            fieldType = "String";
                        }else if(lineTxt.indexOf("NUMBER")>0) {
                            endIndexOf = lineTxt.indexOf("NUMBER");
                            fieldType = "Integer";
                        }

                        fieldStrList.add(lineTxt.substring(0,endIndexOf).replace(" ",""));
                        fieldAllStrList.add("private "
                                +fieldType+" "
                                +dataBaseFieldToStr(lineTxt.substring(0,endIndexOf).replace(" ","")).replaceAll("_","")
                                +";//");
                    }
                    //拼接每个字段的注释
                    if(nextIsNotes){
                        nextIsNotes = false;

                        String fieldAllStr = fieldAllStrList.get(jj);
                        //获取该字段的注释
                        fieldAllStr += lineTxt.substring(1,lineTxt.indexOf("';"));
                        //修改某一行的值，加入注释
                        fieldAllStrList.set(jj,fieldAllStr);
                    }

                    //查找每个字段的注释
                    for(int ii=0;ii<fieldStrList.size();ii++){

                        if(endFieldNum!=0&&lineTxt.indexOf(fieldStrList.get(ii)+" is")>0){
                            nextIsNotes = true;
                            jj = ii;
                        }
                    }

                }
                //输出最后结果
                if(null!=fieldAllStrList){
                    for(String abc:fieldAllStrList){
                        System.out.println(abc);
                    }
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

    /**
     * entity表名转驼峰变量名
     * @param str
     * @return
     */
    public static String dataBaseFieldToStr(String str){
        String returnStr = "";
        if(StringUtils.isNotEmpty(str)){
            str = str.toLowerCase();
            int j = 0;
            for(int i = 0; i < str.length(); i++){
                char c = str.charAt(i);
                //若该字符为“_”
                if (c=='_'){
                    j=i;
                }
                //当循环到“_”后一个字符时,将后面一个字母转大写
                if(j!=0&&i==j+1){
                    returnStr += (c+"").toUpperCase();
                }else{
                    returnStr += c;
                }
            }
        }
        return returnStr;
    }

}
