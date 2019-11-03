package com.utopiaxc.urpassistant.fuctions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

}
