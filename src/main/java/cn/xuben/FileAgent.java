package cn.xuben;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * 文件读写等工具类
 * Create Date：	2013-03-29
 * TODO 需大量优化
 */
public final class FileAgent {

	/**
	 * 全局唯一实例
	 */
	private static FileAgent agent = null;

	/**
	 * 文件BOM头的数值
	 */
	private final static int BOM_VALUE = 65279;

	/**
	 * 构造函数私有
	 */
	private FileAgent() {
	}

	/**
	 * @return 一个本类的实例
	 */
	public static synchronized FileAgent getInstance() {
		if (agent == null) {
			agent = new FileAgent();
		}
		return agent;
	}

	/**
	 * 统一字符串的行结束符为'\n'
	 * 
	 * @param text
	 *            需要进行处理的字符串
	 * @return 经过处理的字符串
	 */
	private final String unifyLineSeparator(String text) {
		if (text == null || text.trim().equals("")) {
			return null;
		}
		/*
		 * 如果字符串中没有'\r'字符，则无需处理
		 */
		if (text.indexOf('\r') < 0) {
			return text;
		}
		/*
		 * 有"\r\n"出现，则认为其行结束符为"\r\n"
		 */
		if (text.indexOf("\r\n") >= 0) {
			return text.replaceAll("\r\n", "\n");
		}
		return text.replaceAll("\r", "\n");
	}

	/**
	 * 因为在本程序内部，行分隔符统一为'\n'， 这里取到系统的行分隔符并将'\n'替换为该分隔符
	 * @param text 需要进行处理的文本内容
	 * @return 处理好的文本内容
	 */
	private final String varyLineSeparator(String text) {
		if (text == null || text.trim().equals("")) {
			return null;
		}
		String linesep = System.getProperty("line.separator");
		if (!linesep.equals("\n")) {
			text = text.replaceAll("\n", linesep);
		}
		return text;
	}

	/**
	 * 得到磁盘(文本)文件的内容，并将内容的行结束符统一为'\n'
	 * 
	 * @param filePath
	 *            给定的磁盘文件的文件路径
	 * @return 文件内容
	 */
	public final String getFileText(String filePath) {
		if (filePath == null || filePath.trim().equals("")) {
			return null;
		}
		File f = new File(filePath.trim());
		if (!f.exists()) {
			return null;
		}
		return this.autoGetFileText(f);
	}
	
	/**
	 * 从一个InputStreamReader里读取文本内容
	 * @param isr
	 * @param contentSize 可读取的文本内容的size，必须提供。
	 * 有多种方式获取此值，比如通过File的length(), 或通过InputStream的available()，
	 * 不过这两种方式获取的都是文本文件的存储字节数，经编码转换后的文本内容的实际长度，
	 * 大多小于此存储字节数，不过这没关系，不会影响程序执行结果。因此，在难以确定文本
	 * 内容实际长度的情况下，可以尝试将contentSize设得大一些
	 * @return
	 */
	public String getTextFromStreamReader(InputStreamReader isr, int contentSize) {
		if (isr == null) {
			System.err.println("null argument");
			return null;
		}
		if (contentSize <= 0) {
			return null;
		}
		try {
			BufferedReader br = new BufferedReader(isr);
			char[] content = new char[contentSize];
			int textLen = br.read(content);
			br.close();
			int offset = 0;
			/*
			 * 去掉BOM头无效字符
			 */
			if (BOM_VALUE == (int) content[0]) {
				offset = 1;
			}
			String ret = String.valueOf(content, offset, textLen - offset);
			return unifyLineSeparator(ret);// TODO 优化成直接处理字符数组，少用一次String
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param f 文件
	 * @param type 该文件的编码格式字符串，如果为null, 则系统会自动进行判断文件编码(<b>有一定机率会判断错误</b>)<br>
	 * 可接受的字符串如"us-ascii"、"iso-8859-1"、"utf8"、"utf-8"、"gbk"、"gb2312"、"gb18030"、
	 * "unicode"(不区分endian)、"UTF-16BE"、"UTF-16LE"<br>
	 * 这些字符串均不区分大小写<br>
	 * 除此之外，Charset中已实现的其它编码格式字符串也可以，不一一列举了
	 * @return
	 */
	public String getFileText(File f, String type) {
		if (f == null || !f.exists() || !f.isFile() || !f.canRead()) {
			return null;
		}
		int len = (int) f.length();
		if (len <= 0) {
			return null;
		}
		if (type == null) {
			type = getCodeType(f);
		}
		try {
			FileInputStream fis = new FileInputStream(f);
			/*
			 * 指定读取文件时以type的编码格式读取
			 */
			InputStreamReader isr = new InputStreamReader(fis, type);
			return this.getTextFromStreamReader(isr, len);
		} catch (IOException ioe) {
			return null;
		}
	}
	
	/**
	 * 自动检测文件的编码并得到磁盘(文本)文件的内容，并将内容的行结束符统一为'\n'
	 * 
	 * @param f
	 *            给定的磁盘文件
	 * @return 文件内容
	 */
	public String autoGetFileText(File f) {
		if (f == null || !f.exists() || !f.isFile() || !f.canRead()) {
			return null;
		}
		if (f.length() <= 0) {
			return null;
		}
		/*
		 * 得到该文件的编码格式字符串
		 */
		String type = getCodeType(f);
		return getFileText(f, type);
	}

	/**
	 * 判断文件f的字符编码
	 * 
	 * @param f
	 *            需要进行分析的文件
	 * @return 文件f的字符编码名称
	 */
	private final String getCodeType(File f) {
		final byte _ef = (byte) 0xef;
		final byte _bb = (byte) 0xbb;
		final byte _bf = (byte) 0xbf;
		final byte _fe = (byte) 0xfe;
		final byte _ff = (byte) 0xff;
		byte[] bom = new byte[10];
		int cn = -1;
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
			cn = is.read(bom);
			is.close();
		} catch (IOException ex) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (cn >= 3 && bom[0] == _ef && bom[1] == _bb && bom[2] == _bf) {
			return "UTF-8";
		} else if (cn >= 2 && bom[0] == _ff && bom[1] == _fe) {
			return "UTF-16LE";
		} else if (cn >= 2 && bom[0] == _fe && bom[1] == _ff) {
			// Unicode big endian
			return "UTF-16BE";
		} else {
			// 初步认为是文件无BOM头，返回当前操作系统的默认文件编码
			return System.getProperty("file.encoding");
		}
		// String os = System.getProperty("os.name").toLowerCase();
		// if (os.indexOf("win") >= 0) {// windows
		// } else if (os.indexOf("mac") >= 0) {// mac
		// } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {//
		// linux或unix
		// }else {
		// }
	}
	
	/**
	 * 从Jar包中读取图片的函数
	 * 
	 * @param path
	 *            文件路径
	 * @param cls
	 *            调用类
	 * @return Image对象
	 */
	public Image getImageFromJar(String path, Class<?> cls) {
		InputStream is = cls.getResourceAsStream(path);
		if (is == null) {
			return null;
		}
		Image image = null;
		BufferedInputStream bis = new BufferedInputStream(is);

		// 存储读到的数据
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// 临时缓冲区
			byte buf[] = new byte[1024];
			int len = 0;
			while ((len = bis.read(buf)) > 0) {
				baos.write(buf, 0, len);
			}
			bis.close();
			// 生成图片对象
			image = Toolkit.getDefaultToolkit().createImage(baos.toByteArray());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return image;
	}

	/**
	 * 从Jar包中读取文本文件
	 * 这些文本文件的编码应该为UTF-16LE(因为中文多，用此编码比UTF-8省空间)
	 * @param path
	 *            文件的相对路径
	 * @param cls
	 *            调用类
	 * @return 读出的文本
	 */
	public String getTextFromJar(String path, Class<?> cls) {
		try {
			InputStream is = cls.getResourceAsStream(path);
			if (is == null) { // 一般是因为资源文件不存在
				return null;
			}
			int size = is.available();
			InputStreamReader isr = new InputStreamReader(is, "UTF-16LE");
			return getTextFromStreamReader(isr, (int) size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * 写磁盘文件方法
	 * 
	 * @param f
	 *            要向其中写入内容的文件，此文件必须已经存在，并且具有相应的写入权限
	 * @param text
	 *            要写入的文本内容
	 * @param addbom
	 *            是否写入BOM文件头
	 * @param charset
	 *            文件采用的字符编码
	 * @return 操作是否成功
	 */
	public boolean setFileText(File f, String text, boolean addbom,
			String charset) {
		if (f == null || !f.exists() || !f.isFile() || !f.canWrite()) {
			return false;
		}
		if (text == null || text.trim().equals("")) {
			return false;
		}
		try {
			FileOutputStream fos = new FileOutputStream(f, addbom);
			OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			if (addbom) {
				// 写入文件BOM头
				bw.write(BOM_VALUE);
			}
			bw.write(text);
			bw.flush();
			bw.close();
		} catch (IOException ioe) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * 写磁盘文件方法
	 * 
	 * @param filePath
	 *            要向其中写入内容的文件的文件路径
	 * @param text
	 *            要写入的文本内容
	 * @param addbom
	 *            是否写入BOM文件头
	 * @param charset
	 *            文件采用的字符编码
	 * @return 操作是否成功
	 */
	public boolean setFileText(String filePath, String text, boolean addbom,
                               String charset) {
		if (filePath == null || filePath.trim().equals("")) {
			return false;
		}
		File f = new File(filePath.trim());
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return setFileText(f, text, addbom, charset);
	}

	/**
	 * 
	 * 采用默认方式(采用系统默认字符编码和行结束符，不写入BOM文件头，替换原有文件)写磁盘文件的方法
	 * 
	 * @param filePath
	 *            要向其中写入内容的文件的文件路径
	 * @param text
	 *            要写入的文本内容
	 * @return 操作是否成功
	 */
	public boolean setFileText(String filePath, String text) {
		String charset = System.getProperty("file.encoding");
		text = varyLineSeparator(text);
		return this.setFileText(filePath, text, false, charset);
	}

	/**
	 * 把一个相对路径转换成简化的绝对路径
	 * 例如将"C:\a\c\d\..\..\b"简换成"C:\a\b"
	 * @param path
	 * @return path简化成的绝对路径
     *
	 */
	@Deprecated
	public String getCanonicalPath(String path) {
		try { // todo 将exception抛出
			File f = new File(path);
			if (!f.exists()) {
				return null;
			}
			return f.getCanonicalPath();
		} catch (Exception e) {
			return null;
		}
	}

}
