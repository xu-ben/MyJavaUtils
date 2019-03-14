package cn.xuben;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Create Date：	2013-03-29
 */
public final class StreamAgent {

    /**
     * 读取的时候的缓冲char数组的大小
     */
    private static final int BUFF_SIZE = 512;

    private StreamAgent() {
    }

    private static class StreamAgentHolder {
        private static final StreamAgent agent = new StreamAgent();
    }

    /**
     * @return 一个本类的全局唯一实例
     */
    public static StreamAgent getInstance() {
        return StreamAgentHolder.agent;
    }

    /**
     * 从InputStream中读取所有内容到StringBuilder中,
     * InputStream可以各种形式获取，例如从文件、从URL等
     * <b>注意控制内容的总长度，该方法不管</b>
     * <b>该方法内部不会主动close该InputStream, 捕获的异常直接抛出</b>
     *
     * @param cs 指定编码，如果为null，则使用默认编码
     */
    public StringBuilder getTextFromInputStream(InputStream is, Charset cs) throws IOException {
        InputStreamReader isr;
        if (cs != null) {
            isr = new InputStreamReader(is, cs);
        } else {
            isr = new InputStreamReader(is);
        }
        return getTextFromInputStreamReader(isr);
    }

    /**
     * 从InputStream中读取最多maxCharnum个字符内容到StringBuilder中,
     * <b>该方法内部不会主动close该InputStream, 捕获的异常直接抛出</b>
     *
     * @param maxCharNum <b>必须提供</b>
     *                   有多种方式获取此值，比如通过File的length(), 或通过InputStream的available()，
     *                   不过这两种方式获取的都是文本文件的存储字节数，经编码转换后的文本内容的实际长度，大多小于此存储字节数，
     *                   不过这没关系，不会影响程序执行结果。因此，在难以确定文本内容实际长度的情况下，
     *                   可以尝试将contentSize设得大一些。这种读取方式性能较好，尤其在读取大文件的情况下，避免反复分配内存
     * @param cs         指定编码，如果为null，则使用默认编码
     */
    public StringBuilder getTextFromInputStream(InputStream is, Charset cs, int maxCharNum) throws IOException {
        InputStreamReader isr;
        if (cs != null) {
            isr = new InputStreamReader(is, cs);
        } else {
            isr = new InputStreamReader(is);
        }
        return getTextFromInputStreamReader(isr, maxCharNum);
    }

    /**
     * 阻塞式将InputStreamReader中的内容读取到StringBuilder中
     * <b>注意控制内容的总长度，该方法不管</b>
     * <b>该方法内部不会主动close该InputStream, 捕获的异常直接抛出</b>
     */
    public StringBuilder getTextFromInputStreamReader(InputStreamReader isr) throws IOException {
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[BUFF_SIZE];
        int len;
        while ((len = br.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb;
    }

    /**
     * 阻塞式将InputStreamReader中的内容读取到StringBuilder中, 最多读取maxCharNum个字符
     * <b>该方法内部不会主动close该InputStream, 捕获的异常直接抛出</b>
     *
     * @param maxCharNum 此值最好稍大于目标内容字节数，越接近越 这样性能较佳
     */
    public StringBuilder getTextFromInputStreamReader(InputStreamReader isr, int maxCharNum) throws IOException {
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder(maxCharNum + 1);
        char[] buf;
        if (maxCharNum < BUFF_SIZE) {
            buf = new char[maxCharNum];
        } else {
            buf = new char[BUFF_SIZE];
        }
        int len;
        while ((len = br.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb;
    }

}
