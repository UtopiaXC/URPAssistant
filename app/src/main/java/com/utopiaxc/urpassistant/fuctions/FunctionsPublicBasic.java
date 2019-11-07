package com.utopiaxc.urpassistant.fuctions;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.utopiaxc.urpassistant.sqlite.SQLHelperTimeTable;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionsPublicBasic {
    private String userAgent = ".userAgent(\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36\")";
    private Map<String, String> Cookies = null;
    private Document document = null;

    //爬取网页源码方法
    public String getHTML(String address) {
        URL url;
        int responsecode;
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String line;
        try {
            url = new URL(address);
            //打开URL
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
            //获取服务器响应代码
            responsecode = urlConnection.getResponseCode();
            if (responsecode == 200) {
                //得到输入流，即获得了网页的内容
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } else {
                return "error";
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return "error";
        }
    }

    //登录教务并获取cookie的方法
    private boolean getCookies(String address, String username, String password) {
        try {
            Connection.Response response = Jsoup.connect(address + "/loginAction.do")
                    .userAgent(userAgent)
                    .data("zjh", username, "mm", password)
                    .method(Connection.Method.POST)
                    .execute();
            Cookies = response.cookies();
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }

    //获取教务指定网页Doc的方法,document调用结束后请赋空
    private boolean getDocument(String address) {
        try {
            Connection.Response response = Jsoup.connect(address)
                    .userAgent(userAgent)
                    .cookies(Cookies)
                    .method(Connection.Method.POST)
                    .execute();

            document = response.parse();
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            document = null;
            return false;
        }
    }

    //测试账号密码及教务地址的方法
    public boolean testURP(String address, String username, String password) {
        try {
            if (!getCookies(address, username, password))
                return false;
            if (!getDocument(address + "/xkAction.do?actionType=17"))
                return false;
            String line = document.toString();

            String reg = "setTimeout";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                document = null;
                return false;
            }
            document = null;
            return true;

        } catch (Exception e) {
            System.out.println(e.toString());
            return false;

        }
    }

    //处理课程表的方法
    public boolean setClassTableSQL(Context context, String address, String username, String password) {
        try {
            //判断Cookie是否正确获取
            if (!getCookies(address, username, password))
                return false;

            //判断网页是否正确爬取
            if (!getDocument(address + "/xkAction.do?actionType=17"))
                return false;

            //寻找全部带有odd标签的课程
            Elements elements = document.getElementsByClass("odd");
            String messages_last[] = new String[30];

            //数据库打开
            SQLHelperTimeTable sql = new SQLHelperTimeTable(context, "URP",null,2);
            SQLiteDatabase sqliteDatabase = sql.getWritableDatabase();
            sqliteDatabase.execSQL("delete from classes");


            //遍历每一课程
            for (Element element : elements) {
                Elements elements_class = element.getElementsByTag("td");
                String messages[] = new String[30];
                int flag = 0;

                //去掉无用信息
                for (Element element_toSave : elements_class) {
                    messages[flag] = element_toSave.toString();
                    messages[flag] = messages[flag].replace(" ", "");
                    messages[flag] = messages[flag].replace("<td>&nbsp", "");
                    messages[flag] = messages[flag].replace("</td>", "");
                    messages[flag] = messages[flag].replace("<tdrowspan=\"1\">&nbsp", "");
                    messages[flag] = messages[flag].replace("<tdrowspan=\"2\">&nbsp", "");
                    messages[flag] = messages[flag].replace("<tdrowspan=\"3\">&nbsp", "");
                    messages[flag] = messages[flag].replace("<tdrowspan=\"4\">&nbsp", "");
                    flag++;
                }

                //判断是否得多行课程
                if (flag > 16) {
                    for (int i = 0; i < 11; i++)
                        messages_last[i] = messages[i];
                }
                if (flag < 8) {
                    for (int i = 0; i < 7; i++)
                        messages[i + 11] = messages[i];
                    for (int i = 0; i < 11; i++)
                        messages[i] = messages_last[i];
                }

                for (int i = 11; i < 18; i++) {
                    if (messages[i].equals(";"))
                        messages[i] = "^";
                }

                //处理上课周数信息
                messages[11] = messages[11].replace("周上", "");
                messages[11] = messages[11].replace(";", "");

                if (messages[11].contains("-")) {
                    String[] time = messages[11].split(",");
                    messages[11] = "";
                    for (String check : time) {
                        check = check.replace(",", "");
                        if (check.contains("-")) {
                            String[] result = check.split("-");
                            result[0] = result[0].replace(",", "");
                            result[1] = result[1].replace(",", "");


                            for (int i = Integer.parseInt(result[0]); i < Integer.parseInt(result[1]) + 1; i++)
                                messages[11] += i + ",";
                        } else
                            messages[11] += check + ",";
                    }
                    messages[11] = messages[11].substring(0, messages[11].length() - 1);
                }

                //数据库提交表
                ContentValues values = new ContentValues();
                values.put("ClassId", messages[1].replace(";", ""));
                values.put("ClassName", messages[2].replace(";", ""));
                values.put("Credit", messages[4].replace(";", ""));
                values.put("ClassAttribute", messages[5].replace(";", ""));
                values.put("ExamAttribute", messages[6].replace(";", ""));
                values.put("Teacher", messages[7].replace(";", ""));
                values.put("Way", messages[9].replace(";", ""));
                values.put("Week",messages[11]);
                values.put("Data",messages[12].replace(";", ""));
                values.put("Time",messages[13].replace(";", ""));
                values.put("Count",messages[14].replace(";", ""));
                values.put("School",messages[15].replace(";", ""));
                values.put("Building",messages[16].replace(";", ""));
                values.put("Room",messages[17].replace(";", ""));

                //插入数据表
                sqliteDatabase.insert("classes", null, values);


            }

            //SQLit测试
            /*
            Cursor cursor = sqliteDatabase.query("classes", new String[]{"ClassName","Credit"}, null, null, null, null, null);
            while(cursor.moveToNext())
                System.out.println(cursor.getString(cursor.getColumnIndex("ClassName"))
                +cursor.getString(cursor.getColumnIndex("Credit")));
            cursor.close();
            */



            sqliteDatabase.close();
            document = null;
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            document = null;
            return false;
        }

    }

    //处理成绩表的方法
    public boolean setGrades(Context context, String address, String username, String password){
        if(!getCookies(address,username,password))
            return false;
        if(!getDocument(address+"/gradeLnAllAction.do?type=ln&oper=sxinfo&lnsxdm=001"))
            return false;
        try{
            System.out.println(document.toString());





            return true;

        }catch (Exception e){
            System.out.println(e.toString());
            return false;
        }


    }
}
