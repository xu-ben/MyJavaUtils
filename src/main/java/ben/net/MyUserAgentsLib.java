package ben.net;

public class MyUserAgentsLib {

    public static String chrome70 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

    /**
     * 微信5.0在我小米2手机上的User-Agent
     */
    private static String microAgentstr = "Mozilla/5.0 (Linux; U; Android 4.1.1; zh-cn; MI2 Build/JRO03L) AppleWebKit/534.30 (KHTML, like Gecko) version/4.0 Mobile Safari/534.30 MicroMessenger/5.0.3.1.355";
//	private static String microAgentstr = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_1_3 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Mobile/10B329 MicroMessenger/5.0.1";

    public static String getAgentsOfChrome() {
        return chrome70;
    }

    public static String getAgentsOfWechat() {
        return microAgentstr;
    }
}
