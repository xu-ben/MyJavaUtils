package cn.xuben.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建日期2013-12-6
 */
public final class WebClientAgent {

    /**
     * 全局唯一实例
     */
    private static WebClientAgent agent = null;

    private WebClientAgent() {
    }


    /**
     * 试图从连接的header中解析出charset name
     *
     * @return charset name或null(未解析到)
     */
    private String getEncodingName(URLConnection conn) {
        String ret = conn.getContentEncoding();
        if (ret != null) {
            return ret;
        }
        ret = conn.getContentType();
        if (ret == null) {
            return null;
        }
        int index = ret.toLowerCase().indexOf("charset=");
        if (index != -1) {
            return ret.substring(index + 8);
        }
        return null;
    }

    /**
     * 获取编码名称字符串
     *
     * @return 解析获得的编码名称字符串，若解析不到编码，则返回null
     */
    public String getEncodingName(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        /**
         * 如果能从header中解析出charset，则返回
         */
        String result = getEncodingName(conn);
        if (result != null) {
            return result;
        }
        /**
         * 如果不能从header中解析出charset，则解析网页meta从中获取charset
         * TODO 优化
         * 目前这种写法是有问题的，一是应该只解析<head></head>内部, 二是不能按行读
         */
        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        // example1:  <meta charset="UTF-8">
        // example2: <meta http-equiv="content-type" content="text/html;charset=utf-8">
        String regex = "<\\s*meta[^>]*content=\"[^\"]*charset\\s*=\\s*([a-zA-Z_0-9-]+)\\s*\"";
        Pattern pa = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Pattern pb = Pattern.compile(".*<\\s*body\\s*>.*", Pattern.CASE_INSENSITIVE);
        String line;
//        pa.matcher()
        while ((line = br.readLine()) != null) {
            Matcher m = pa.matcher(line);
            if (m.find()) {
                br.close();
                return m.group(1);
            } else {
                if (pb.matcher(line).matches()) {
                    break;
                }
            }
        }
        // 以上两种方法均解析不出，则返回null
        br.close();
        return null;
    }

    /**
     * 给定一个url，方法会去访问，并返回其网页内容文本
     *
     * @param urlStr   合法url字符串
     * @param encoding 最好指定。如果不指定(设为null)，方法内部可能会通过多访问一次并解析得到encoding
     */
    public String getContent(String urlStr, String encoding) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
//            Proxy p;
            URLConnection uc = url.openConnection();
            uc.connect();
            if (encoding == null) {
                encoding = getEncodingName(uc);
            }
            Charset cs = null;
            if (encoding != null) {
                cs = Charset.forName(encoding);
            }
            is = uc.getInputStream();
            return getTextFromInputStream(is, Charset.forName(encoding)).toString();
        } catch (IOException ioe) {
           throw ioe;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     *
     * 给定一个url，方法会去访问，并返回其网页内容
     * <b>不会多次访问, 即使用户未指定Charset，最多会尝试从连接的元信息中取</b>
     * 方法执行完毕会关闭连接
     */
    public StringBuilder getContent(URL url, String userAgent, Charset cs) throws IOException {
        InputStream is = null;
        try {
//            Proxy p;
            URLConnection uc = url.openConnection();
            if (userAgent != null) {
                uc.setRequestProperty("User-Agent", userAgent);
            }
//            uc.setReadTimeout(3000);
            uc.connect();
            is = uc.getInputStream();
            return getTextFromInputStream(is, cs);
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     *
     * 给定一个连接好的连接对象，得到指定编码的网页内容
     * <b>此方法内部不会主动关闭连接</b>
     */
    public StringBuilder getContent(URLConnection conn, Charset cs) throws IOException {
        InputStream is = conn.getInputStream();
        return getTextFromInputStream(is, cs);
    }

   /**
     * @return 一个本类的实例
     */
    public static synchronized WebClientAgent getInstance() {
        if (agent == null) {
            agent = new WebClientAgent();
        }
        return agent;
    }

}
