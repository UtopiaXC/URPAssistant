package com.utopiaxc.urpassistant.fuctions;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionsPublicBasic {
    //爬取网页源码方法
    public String getHTML(String address) {
        System.out.println(address);
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
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String result = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
                return result;
            } else {
                return "error";
            }
        } catch (Exception e) {
            return "error";
        }
    }

    public boolean testURP(String address,String username,String password){
        try {

            Connection conn = Jsoup.connect(address);
            conn.method(Connection.Method.GET);
            conn.followRedirects(false);
            Connection.Response response = conn.execute();
            System.out.println(response.cookies());

            Connection.Response res = Jsoup.connect(address)
                    .data("zjh", username, "mm", password)
                    .method(Connection.Method.POST)
                    .execute();

            Map<String, String> loginCookies = res.cookies();

            Document doc = Jsoup.connect(address+"/xkAction.do?actionType=17")
                    .cookies(loginCookies)
                    .get();
            System.out.println(doc);

            String line=doc.toString();

            System.out.println(line);

            String reg = "setTimeout";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return false;
            }
            return true;

        }catch (Exception e){
            System.out.println(e);
            return false;

        }
    }
}
