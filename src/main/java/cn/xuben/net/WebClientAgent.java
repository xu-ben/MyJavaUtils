package cn.xuben.net;

import java.io.*;
import java.net.*;
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
    private String getEncodingNameFromConn(URLConnection conn) {
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
    @Deprecated
    public String getEncodingName(URL url) throws IOException {
        URLConnection conn = url.openConnection();
//        conn.connect(); 不用连接就可以读，因为openConnection了就是建立了tcp连接了
        /**
         * 如果能从header中解析出charset，则返回
         */
        String result = getEncodingNameFromConn(conn);
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

    @Deprecated
    public StringBuilder doPost(URL url, String postData) throws IOException {
        // TODO 方法还未完成
        URLConnection urlConn = url.openConnection();
//            Proxy p;
        if (!(urlConn instanceof HttpURLConnection)) {
            throw new ProtocolException("the url'protocol must be http or https");
        }
        HttpURLConnection conn = (HttpURLConnection) urlConn;
        conn.setDoOutput(true);
        // Post 请求不能使用缓存
        conn.setUseCaches(false);
        // 设定请求的方法为"POST"，默认是GET
        conn.setRequestMethod("POST");
        // 设定传送的内容类型是可序列化的java对象
        // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
//        conn.setRequestProperty("Content-type", "application/x-java-serialized-object");
        conn.setRequestProperty("Accept-Charset", "utf-8");
        Charset cs = Charset.forName("utf-8");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postData.length()));
        conn.connect();
        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(output);
            writer.write(postData);
            writer.flush();
            return getContentFromConn(conn, cs);
        } catch (IOException e) {
            throw e;
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * 给定一个url字符串，方法会直接去<b>阻塞式</b>访问，并返回其网页内容文本
     *
     * @param urlStr   合法url字符串
     * @param encoding 如果不指定(设为null)，方法内部会尝试从连接的状态信息中读取此值，再不行就采用系统默认编码
     */
    public StringBuilder doGetSimply(String urlStr, String encoding) throws IOException {
        URL url = new URL(urlStr);
        Charset cs = null;
        if (encoding != null) {
            cs = Charset.forName(encoding);
        }
        return doGet(url, null, null, 0, 0, cs);
    }

    /**
     * 默认的不使用代理的doGet方法，连接和读取限时分别是20s和40s
     */
    public StringBuilder doGetByDefault(String urlStr, String userAgent, String encoding) throws IOException {
        URL url = new URL(urlStr);
        Charset cs = null;
        if (encoding != null) {
            cs = Charset.forName(encoding);
        }
        return doGet(url, null, userAgent, 20 * 1000, 40 * 1000, cs);
    }

    /**
     * 默认的使用代理的doGet方法，连接和读取限时分别是30s和40s
     */
    public StringBuilder doGetByProxyDefault(String urlStr, Proxy proxy, String userAgent, String encoding) throws IOException {
        URL url = new URL(urlStr);
        Charset cs = null;
        if (encoding != null) {
            cs = Charset.forName(encoding);
        }
        return doGet(url, proxy, userAgent, 30 * 1000, 40 * 1000, cs);
    }

    /**
     * 不使用代理，直接使用Get方法，获取指定url上的网页文本内容
     *
     * @param url       必须是http或https, 否则会报错
     * @param userAgent 如果不需要，设为null即可
     * @param cs        网页文本的编码，若未指定(null)，方法内部会尝试从连接的状态信息中读取此值，再不行就采用系统默认编码
     * @throws IOException 如果方法执行超时(连接等待20s，读取等待40s)，会抛出java.net.SocketTimeoutException异常，其他情况的异常也会正常抛出
     */
    public StringBuilder doGetDirectlyInDefaultTime(URL url, String userAgent, Charset cs) throws IOException {
        return doGet(url, null, userAgent, 20 * 1000, 40 * 1000, cs);
    }

    /**
     *
     * 使用代理Get指定url上的网页文本内容
     *
     * @param url       必须是http或https, 否则会报错
     * @param proxy
     * @param userAgent 如果不需要，设为null即可
     * @param cs        网页文本的编码，若未指定(null)，方法内部会尝试从连接的状态信息中读取此值，再不行就采用系统默认编码
     * @throws IOException 如果方法执行超时(连接等待30s，读取等待40s)，会抛出java.net.SocketTimeoutException异常，其他情况的异常也会正常抛出
     */
    public StringBuilder doGetByProxyInDefaultTime(URL url, Proxy proxy, String userAgent, Charset cs) throws IOException {
        return doGet(url, proxy, userAgent, 30 * 1000, 40 * 1000, cs);
    }

    /**
     * 使用Get方法，获取指定url上的网页文本内容
     *
     * @param url         必须是http或https, 否则会报错
     * @param proxy       建立网络连接所用的代理，为null表示不使用代理
     * @param userAgent   如果不需要，设为null即可
     * @param connTimeout 建立连接的超时时间，单位毫秒
     * @param readTimeout 建立连接之后读取网页的超时时间，单位毫秒
     * @param cs          网页文本的编码，若未指定(null)，方法内部会尝试从连接的状态信息中读取此值，再不行就采用系统默认编码
     * @throws IOException 如果方法执行超时，会抛出java.net.SocketTimeoutException异常，其他情况的异常也会正常抛出
     */
    public StringBuilder doGet(URL url, Proxy proxy, String userAgent, int connTimeout, int readTimeout, Charset cs) throws IOException {
        URLConnection urlConn;
        if (proxy != null) {
            urlConn = url.openConnection(proxy);
        } else {
            urlConn = url.openConnection();
        }
        if (!(urlConn instanceof HttpURLConnection)) {
            throw new ProtocolException("the url'protocol must be http or https");
        }
        HttpURLConnection conn = (HttpURLConnection) urlConn;
        if (userAgent != null) {
            conn.setRequestProperty("User-Agent", userAgent);
        }
        if (connTimeout > 0) {
            conn.setConnectTimeout(connTimeout);
        }
        if (readTimeout > 0) {
            conn.setReadTimeout(readTimeout);
        }
        return connectAndReadFromConn(conn, cs);
    }

    /**
     * 给定一个准备好，但还未连接的对象，连接并读取得到指定编码的网页内容
     *
     * @param cs 网页文本的编码，若未指定(null)，方法内部会尝试从连接的状态信息中读取此值，再不行就采用系统默认编码
     */
    private StringBuilder connectAndReadFromConn(HttpURLConnection conn, Charset cs) throws IOException {
        InputStream is = null;
        try {
            conn.connect();
            if (cs == null) {
                String cn = getEncodingNameFromConn(conn);
                if (cn != null) {
                    cs = Charset.forName(cn);
                }
            }
            int code = conn.getResponseCode();
            if (code >= 300 || code < 200) {
                throw new IOException("the response code is " + code);
            }
            is = conn.getInputStream();
            return getTextFromInputStream(is, cs);
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
            conn.disconnect();
        }
    }

    /**
     * 给定一个连接好的连接对象，得到指定编码的网页内容
     * <b>此方法内部不会主动关闭连接</b>
     *
     * @param cs 网页文本的编码，若未指定(null)，方法内部会尝试从连接的状态信息中读取此值，再不行就采用系统默认编码
     */
    public StringBuilder getContentFromConn(HttpURLConnection conn, Charset cs) throws IOException {
        if (cs == null) {
            String cn = getEncodingNameFromConn(conn);
            if (cn != null) {
                cs = Charset.forName(cn);
            }
        }
        int code = conn.getResponseCode();
        if (code >= 300 || code < 200) {
            throw new IOException("the response code is " + code);
        }
        return getTextFromInputStream(conn.getInputStream(), cs);
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
