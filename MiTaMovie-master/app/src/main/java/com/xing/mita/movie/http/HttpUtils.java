package com.xing.mita.movie.http;

import android.text.TextUtils;
import android.util.Log;

import com.xing.mita.movie.app.SysApplication;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Mita
 * @date 2018/10/11
 * @Description 网络请求
 */
public class HttpUtils {

    /**
     * @param aUrl 网址
     * @return 返回的HTML代码
     */
    public static String getHTML(String aUrl, boolean instanceFollowRedirects) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(aUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            if (instanceFollowRedirects) {
                // 必须设置false，否则会自动redirect到Location的地址
                conn.setInstanceFollowRedirects(false);

                //获取真正请求地址
                String location = conn.getHeaderField("Location");
                Log.i("HttpUtils", "真正请求地址：" + location);
                if (TextUtils.isEmpty(location)) {
                    location = aUrl;
                } else if (!TextUtils.isEmpty(location) && location.contains("m")) {
                    location = location.substring(0, location.lastIndexOf("m") + 1);
                }
                String spilt = "://";
                if (location.contains(spilt)) {
                    location = location.replace("://", "://m.");
                }
                SysApplication.BASE_URL = location;

                url = new URL(location);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
            }

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                String htmlStr = new String(outStream.toByteArray(), StandardCharsets.UTF_8);
                inputStream.close();
                outStream.close();
                return htmlStr;
            }
        } catch (Exception e) {
            Log.e("HttpUtils", "请求失败：" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * @param aUrl 网址
     * @return 返回的HTML代码
     */
    public static String getKk3HTML(String aUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(aUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                String htmlStr = new String(outStream.toByteArray(), "gb2312");
                inputStream.close();
                outStream.close();
                return htmlStr;
            }
        } catch (Exception e) {
            Log.e("HttpUtils", "请求失败：" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * 对url中的中文进行编码
     *
     * @param url String
     * @return String
     */
    public static String encodeUrl(String url) {
        //被转码后的url
        String result = "";
        int index = url.indexOf("?");
        result = url.substring(0, index + 1);
        String temp = url.substring(index + 1);
        try {
            //URLEncode转码会将& ： / = 等一些特殊字符转码,(但是这个字符  只有在作为参数值  时需要转码;
            // 例如url中的&具有参数连接的作用，此时就不能被转码)
            String encode = URLEncoder.encode(temp, "gb2312");
            System.out.println(encode);
            encode = encode.replace("%3D", "=");
            encode = encode.replace("%2F", "/");
            encode = encode.replace("+", "%20");
            encode = encode.replace("%26", "&");
            result += encode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
