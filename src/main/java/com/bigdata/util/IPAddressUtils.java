package com.bigdata.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP地址服务
 * gzp提供
 */
public class IPAddressUtils {

    /**
     * 纯真IP数据库名
     */
//    private String IP_FILE="qqwry.dat";  //IP数据库文件，window上跑的话，放到项目根目录即可
//    private String IP_FILE="D:\\qigaishidaima\\nginx_log\\qqwry.dat";
    //private String IP_FILE="/home/gzp/qqwry.dat"; //在linux上运行的话，要把 IP数据库文件 放到某个绝对路径上
    //private String IP_FILE="F:\\test\\qqwry.dat"; //在linux上运行的话，要把 IP数据库文件 放到某个绝对路径上
	private String IP_FILE="/root/nginx-etl/qqwry.dat"; //在linux上运行的话，要把 IP数据库文件 放到某个绝对路径上

    /**
     * 纯真IP数据库保存的文件夹
     */
    private String INSTALL_DIR="/test/";

    /**
     * 常量，比如记录长度等等
     */
    private static final int IP_RECORD_LENGTH = 7;
    /**
     * 常量，读取模式1
     */
    private static final byte REDIRECT_MODE_1 = 0x01;
    /**
     * 常量，读取模式2
     */
    private static final byte REDIRECT_MODE_2 = 0x02;

    /**
     * 缓存，查询IP时首先查询缓存，以减少不必要的重复查找
     */
    private Map<String, IPLocation> ipCache;
    /**
     * 随机文件访问类
     */
    private RandomAccessFile ipFile;
    /**
     * 内存映射文件
     */
    private MappedByteBuffer mbb;
    /**
     * 起始地区的开始和结束的绝对偏移
     */
    private long ipBegin, ipEnd;

    /**
     * 为提高效率而采用的临时变量
     */
    private IPLocation loc;
    /**
     * 为提高效率而采用的临时变量
     */
    private byte[] buf;
    /**
     * 为提高效率而采用的临时变量
     */
    private byte[] b4;
    /**
     * 为提高效率而采用的临时变量
     */
    private byte[] b3;
    /**
     * IP地址库文件错误
     */
    private static final String BAD_IP_FILE     =   "IP地址库文件错误";
    /**
     * 未知国家
     */
    private static final String UNKNOWN_COUNTRY   =   "未知国家";
    /**
     * 未知地区
     */
    private static final String UNKNOWN_AREA    =   "未知地区";


    public void init() {
        try {
            // 缓存一定要用ConcurrentHashMap， 避免多线程下获取为空
            ipCache = new ConcurrentHashMap();
            loc = new IPLocation();
            buf = new byte[100];
            b4 = new byte[4];
            b3 = new byte[3];
            try {
                ipFile = new RandomAccessFile(IP_FILE, "r");
                 System.out.println("读取ip库文件为:" + IP_FILE);
                
                
            } catch (FileNotFoundException e) {
                // 如果找不到这个文件，再尝试再当前目录下搜索，这次全部改用小写文件名
                //     因为有些系统可能区分大小写导致找不到ip地址信息文件
                String filename = new File(IP_FILE).getName().toLowerCase();
                File[] files = new File(INSTALL_DIR).listFiles();
                for(int i = 0; i < files.length; i++) {
                    if(files[i].isFile()) {
                        if(files[i].getName().toLowerCase().equals(filename)) {
                            try {
                                ipFile = new RandomAccessFile(files[i], "r");
                            } catch (FileNotFoundException e1) {
                                //log.error("IP地址信息文件没有找到，IP显示功能将无法使用:{}" + e1.getMessage(), e1);
                            	e1.printStackTrace();
                                ipFile = null;
                            }
                            break;
                        }
                    }
                }
            }
            // 如果打开文件成功，读取文件头信息
            if(ipFile != null) {
                try {
                    ipBegin = readLong4(0);
                    ipEnd = readLong4(4);
                    if(ipBegin == -1 || ipEnd == -1) {
                        ipFile.close();
                        ipFile = null;
                    }
                } catch (IOException e) {
                    //log.error("IP地址信息文件格式有错误，IP显示功能将无法使用"+ e.getMessage(), e);
                	e.printStackTrace();
                    ipFile = null;
                }
            }

        } catch (Exception e) {
//            log.error("IP地址服务初始化异常:" + e.getMessage(), e);
        	e.printStackTrace();
        }
    }

    /**
     * 查询IP地址位置 - synchronized的作用是避免多线程时获取区域信息为空
     * @param ip
     * @return
     */
    public synchronized IPLocation getIPLocation(final String ip) {
        IPLocation location = new IPLocation();
        String area = this.getArea(ip);//运营商信息
        location.setArea(area);//运营商信息
        String country = this.getCountry(ip);//国家信息
        location.setCountry(country);//国家信息
        return location;
    }

    /**
     * 从内存映射文件的offset位置开始的3个字节读取一个int
     * @param offset
     * @return
     */
    private int readInt3(int offset) {
        mbb.position(offset);
        return mbb.getInt() & 0x00FFFFFF;
    }

    /**
     * 从内存映射文件的当前位置开始的3个字节读取一个int
     * @return
     */
    private int readInt3() {
        return mbb.getInt() & 0x00FFFFFF;
    }

    /**
     * 根据IP得到国家名
     * @param ip ip的字节数组形式
     * @return 国家名字符串
     */
    public String getCountry(byte[] ip)
    {
        // 检查ip地址文件是否正常
        if(ipFile == null)
        {
            return BAD_IP_FILE;
        }

        // 保存ip，转换ip字节数组为字符串形式
        String ipStr = CZIPUtils.getIpStringFromBytes(ip);
        // 先检查cache中是否已经包含有这个ip的结果，没有再搜索文件
        if(ipCache.containsKey(ipStr))
        {
            IPLocation ipLoc = ipCache.get(ipStr);
            String country = ipLoc.getCountry();
            return country;
        }
        else
        {
            IPLocation ipLoc = getIPLocation(ip);
            IPLocation copy = ipLoc.getCopy();
            ipCache.put(ipStr, copy);
            String country = ipLoc.getCountry();
            return country;
        }
    }

    /**
     * 根据IP得到国家名
     * @param ip IP的字符串形式
     * @return 国家名字符串
     */
    public String getCountry(String ip)
    {
        byte[] ipByteArrayFromString = CZIPUtils.getIpByteArrayFromString(ip);
        String country = getCountry(ipByteArrayFromString);
        return country;
    }

    /**
     * 根据IP得到地区名
     * @param ip ip的字节数组形式
     * @return 地区名字符串
     */
    public String getArea(final byte[] ip) {
        // 检查ip地址文件是否正常
        if(ipFile == null)
            return BAD_IP_FILE;
        // 保存ip，转换ip字节数组为字符串形式
        String ipStr = CZIPUtils.getIpStringFromBytes(ip);
        // 先检查cache中是否已经包含有这个ip的结果，没有再搜索文件
        if(ipCache.containsKey(ipStr)) {
            IPLocation ipLoc = ipCache.get(ipStr);
            return ipLoc.getArea();
        } else {
            IPLocation ipLoc = getIPLocation(ip);
            ipCache.put(ipStr, ipLoc.getCopy());
            return ipLoc.getArea();
        }
    }

    /**
     * 根据IP得到地区名
     * @param ip IP的字符串形式
     * @return 地区名字符串
     */
    public String getArea(final String ip) {
        return getArea(CZIPUtils.getIpByteArrayFromString(ip));
    }

    /**
     * 根据ip搜索ip信息文件，得到IPLocation结构，所搜索的ip参数从类成员ip中得到
     * @param ip 要查询的IP
     * @return IPLocation结构
     */
    private IPLocation getIPLocation(final byte[] ip) {
        IPLocation info = null;
        long offset = locateIP(ip);
        if(offset != -1)
            info = getIPLocation(offset);
        if(info == null) {
            info = new IPLocation();
            info.setCountry (  UNKNOWN_COUNTRY);
            info.setArea(UNKNOWN_AREA);
        }
        return info;
    }

    /**
     * 从offset位置读取4个字节为一个long，因为java为big-endian格式，所以没办法
     * 用了这么一个函数来做转换
     * @param offset
     * @return 读取的long值，返回-1表示读取文件失败
     */
    private long readLong4(long offset) {
        long ret = 0;
        try {
            ipFile.seek(offset);
            ret |= (ipFile.readByte() & 0xFF);
            ret |= ((ipFile.readByte() << 8) & 0xFF00);
            ret |= ((ipFile.readByte() << 16) & 0xFF0000);
            ret |= ((ipFile.readByte() << 24) & 0xFF000000);
            return ret;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * 从offset位置读取3个字节为一个long，因为java为big-endian格式，所以没办法
     * 用了这么一个函数来做转换
     * @param offset 整数的起始偏移
     * @return 读取的long值，返回-1表示读取文件失败
     */
    private long readLong3(long offset) {
        long ret = 0;
        try {
            ipFile.seek(offset);
            ipFile.readFully(b3);
            ret |= (b3[0] & 0xFF);
            ret |= ((b3[1] << 8) & 0xFF00);
            ret |= ((b3[2] << 16) & 0xFF0000);
            return ret;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * 从当前位置读取3个字节转换成long
     * @return 读取的long值，返回-1表示读取文件失败
     */
    private long readLong3() {
        long ret = 0;
        try {
            ipFile.readFully(b3);
            ret |= (b3[0] & 0xFF);
            ret |= ((b3[1] << 8) & 0xFF00);
            ret |= ((b3[2] << 16) & 0xFF0000);
            return ret;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * 从offset位置读取四个字节的ip地址放入ip数组中，读取后的ip为big-endian格式，但是
     * 文件中是little-endian形式，将会进行转换
     * @param offset
     * @param ip
     */
    private void readIP(long offset, byte[] ip) {
        try {
            ipFile.seek(offset);
            ipFile.readFully(ip);
            byte temp = ip[0];
            ip[0] = ip[3];
            ip[3] = temp;
            temp = ip[1];
            ip[1] = ip[2];
            ip[2] = temp;
        } catch (IOException e) {
//            log.error("readIP函数实现(从offset位置读取四个字节的ip地址放入ip数组中)"+e.getMessage(), e);
        	e.printStackTrace();
        }
    }

    /**
     * 从offset位置读取四个字节的ip地址放入ip数组中，读取后的ip为big-endian格式，但是
     * 文件中是little-endian形式，将会进行转换
     * @param offset
     * @param ip
     */
    private void readIP(int offset, byte[] ip) {
        mbb.position(offset);
        mbb.get(ip);
        byte temp = ip[0];
        ip[0] = ip[3];
        ip[3] = temp;
        temp = ip[1];
        ip[1] = ip[2];
        ip[2] = temp;
    }

    /**
     * 把类成员ip和beginIp比较，注意这个beginIp是big-endian的
     * @param ip 要查询的IP
     * @param beginIp 和被查询IP相比较的IP
     * @return 相等返回0，ip大于beginIp则返回1，小于返回-1。
     */
    private int compareIP(byte[] ip, byte[] beginIp) {
        for(int i = 0; i < 4; i++) {
            int r = compareByte(ip[i], beginIp[i]);
            if(r != 0)
                return r;
        }
        return 0;
    }

    /**
     * 把两个byte当作无符号数进行比较
     * @param b1
     * @param b2
     * @return 若b1大于b2则返回1，相等返回0，小于返回-1
     */
    private int compareByte(byte b1, byte b2) {
        if((b1 & 0xFF) > (b2 & 0xFF)) // 比较是否大于
            return 1;
        else if((b1 ^ b2) == 0)// 判断是否相等
            return 0;
        else
            return -1;
    }

    /**
     * 这个方法将根据ip的内容，定位到包含这个ip国家地区的记录处，返回一个绝对偏移
     * 方法使用二分法查找。
     * @param ip 要查询的IP
     * @return 如果找到了，返回结束IP的偏移，如果没有找到，返回-1
     */
    private long locateIP(byte[] ip) {
        long m = 0;
        int r;
        // 比较第一个ip项
        readIP(ipBegin, b4);
        r = compareIP(ip, b4);
        if(r == 0) return ipBegin;
        else if(r < 0) return -1;
        // 开始二分搜索
        for(long i = ipBegin, j = ipEnd; i < j; ) {
            m = getMiddleOffset(i, j);
            readIP(m, b4);
            r = compareIP(ip, b4);
            // log.debug(Utils.getIpStringFromBytes(b));
            if(r > 0)
                i = m;
            else if(r < 0) {
                if(m == j) {
                    j -= IP_RECORD_LENGTH;
                    m = j;
                } else
                    j = m;
            } else
                return readLong3(m + 4);
        }
        // 如果循环结束了，那么i和j必定是相等的，这个记录为最可能的记录，但是并非
        //     肯定就是，还要检查一下，如果是，就返回结束地址区的绝对偏移
        m = readLong3(m + 4);
        readIP(m, b4);
        r = compareIP(ip, b4);
        if(r <= 0) return m;
        else return -1;
    }

    /**
     * 得到begin偏移和end偏移中间位置记录的偏移
     * @param begin
     * @param end
     * @return
     */
    private long getMiddleOffset(long begin, long end) {
        long records = (end - begin) / IP_RECORD_LENGTH;
        records >>= 1;
        if(records == 0) records = 1;
        return begin + records * IP_RECORD_LENGTH;
    }

    /**
     * 给定一个ip国家地区记录的偏移，返回一个IPLocation结构
     * @param offset 国家记录的起始偏移
     * @return IPLocation对象
     */
    private IPLocation getIPLocation(long offset) {
        try {
            // 跳过4字节ip
            ipFile.seek(offset + 4);
            // 读取第一个字节判断是否标志字节
            byte b = ipFile.readByte();
            if(b == REDIRECT_MODE_1) {
                // 读取国家偏移
                long countryOffset = readLong3();
                // 跳转至偏移处
                ipFile.seek(countryOffset);
                // 再检查一次标志字节，因为这个时候这个地方仍然可能是个重定向
                b = ipFile.readByte();
                if(b == REDIRECT_MODE_2) {
                    loc.setCountry (  readString(readLong3()));
                    ipFile.seek(countryOffset + 4);
                } else
                    loc.setCountry ( readString(countryOffset));
                // 读取地区标志
                loc.setArea( readArea(ipFile.getFilePointer()));
            } else if(b == REDIRECT_MODE_2) {
                loc.setCountry ( readString(readLong3()));
                loc.setArea( readArea(offset + 8));
            } else {
                loc.setCountry (  readString(ipFile.getFilePointer() - 1));
                loc.setArea( readArea(ipFile.getFilePointer()));
            }
            return loc;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 给定一个ip国家地区记录的偏移，返回一个IPLocation结构，此方法应用与内存映射文件方式
     * @param offset 国家记录的起始偏移
     * @return IPLocation对象
     */
    private IPLocation getIPLocation(int offset) {
        // 跳过4字节ip
        mbb.position(offset + 4);
        // 读取第一个字节判断是否标志字节
        byte b = mbb.get();
        if(b == REDIRECT_MODE_1) {
            // 读取国家偏移
            int countryOffset = readInt3();
            // 跳转至偏移处
            mbb.position(countryOffset);
            // 再检查一次标志字节，因为这个时候这个地方仍然可能是个重定向
            b = mbb.get();
            if(b == REDIRECT_MODE_2) {
                loc.setCountry (  readString(readInt3()));
                mbb.position(countryOffset + 4);
            } else
                loc.setCountry (  readString(countryOffset));
            // 读取地区标志
            loc.setArea(readArea(mbb.position()));
        } else if(b == REDIRECT_MODE_2) {
            loc.setCountry ( readString(readInt3()));
            loc.setArea(readArea(offset + 8));
        } else {
            loc.setCountry (  readString(mbb.position() - 1));
            loc.setArea(readArea(mbb.position()));
        }
        return loc;
    }

    /**
     * 从offset偏移开始解析后面的字节，读出一个地区名
     * @param offset 地区记录的起始偏移
     * @return 地区名字符串
     * @throws IOException
     */
    private String readArea(long offset) throws IOException {
        ipFile.seek(offset);
        byte b = ipFile.readByte();
        if(b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
            long areaOffset = readLong3(offset + 1);
            if(areaOffset == 0)
                return UNKNOWN_AREA;
            else
                return readString(areaOffset);
        } else
            return readString(offset);
    }

    /**
     * @param offset 地区记录的起始偏移
     * @return 地区名字符串
     */
    private String readArea(int offset) {
        mbb.position(offset);
        byte b = mbb.get();
        if(b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
            int areaOffset = readInt3();
            if(areaOffset == 0)
                return UNKNOWN_AREA;
            else
                return readString(areaOffset);
        } else
            return readString(offset);
    }

    /**
     * 从offset偏移处读取一个以0结束的字符串
     * @param offset 字符串起始偏移
     * @return 读取的字符串，出错返回空字符串
     */
    private String readString(long offset) {
        try {
            ipFile.seek(offset);
            int i;
            for(i = 0, buf[i] = ipFile.readByte(); buf[i] != 0; buf[++i] = ipFile.readByte());
            if(i != 0)
                return CZIPUtils.getString(buf, 0, i, "GBK");
        } catch (IOException e) {
//            log.error("readString函数实现(从offset偏移处读取一个以0结束的字符串)"+e.getMessage(), e);
        	e.printStackTrace();
        }
        return "";
    }

    /**
     * 从内存映射文件的offset位置得到一个0结尾字符串
     * @param offset 字符串起始偏移
     * @return 读取的字符串，出错返回空字符串
     */
    private String readString(int offset) {
        try {
            mbb.position(offset);
            int i;
            for(i = 0, buf[i] = mbb.get(); buf[i] != 0; buf[++i] = mbb.get());
            if(i != 0)
                return CZIPUtils.getString(buf, 0, i, "GBK");
        } catch (IllegalArgumentException e) {
//            log.error("readString函数实现(从内存映射文件的offset位置得到一个0结尾字符串)"+e.getMessage(), e);
        	e.printStackTrace();
        }
        return "";
    }

    public String getCity(final String ipAddress){
        try {
            if(ipAddress.startsWith("192.168.")){
//                log.error("此IP["+ipAddress+"]段不进行处理！");
            	System.out.println("此IP["+ipAddress+"]段不进行处理！");
                return null;
            }
            return getIPLocation(ipAddress).getCity();
        }catch (Exception e){
//            log.error("根据IP["+ipAddress+"]获取省份失败:"+ e.getMessage());
        	e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){
        IPAddressUtils ip = new IPAddressUtils();
        ip.init();

        /*
        运行时 记得修改 IP数据库文件的 所在路径
            String IP_FILE="qqwry.dat";  //IP数据库文件，window上跑的话，放到项目根目录即可
            String IP_FILE="/home/gzp/qqwry.dat"; //在linux上运行的话，要把 IP数据库文件 放到某个绝对路径上
        */
        String address = "61.140.182.181";

        /*
        外国网址测试
                169.235.24.133    美国 加利福尼亚大学 06-04 17:39 0.767 whois
                153.19.50.62      波兰 ProxyCN 06-04 17:31 0.992 whois
                203.69.66.102     台湾省 中华电信 06-04 17:46 0.996 whois
                59.39.145.178     广东省惠州市 电信 06-04 17:36 0.998 whois
                115.68.28.11      欧洲 ProxyCN 06-04 17:33 0.998 whois
                169.235.24.133    美国 06-04 17:50 1.000 whois
                210.51.23.244     上海市 漕河泾网通IDC机房 06-04 17:48 1.000 whois
                220.194.58.240    北京市 联通
        */
//        String address = "195.59.199.64";

        System.out.println("IP地址["+address + "]");
        System.out.println("获取到的区域信息:" + ip.getIPLocation(address).getCountry());
        String country = ip.getIPLocation(address).getCountry();
        System.out.println(country.indexOf("省"));
        System.out.println(country.indexOf("市"));
        System.out.println(country.substring(0,3));
        System.out.println(country.substring(3,6));

        System.out.println("获取到的城市:" + ip.getIPLocation(address).getCity());
        String city = ip.getIPLocation(address).getCity();
        System.out.println(city.indexOf("市"));
        System.out.println(city.substring(0,3));

        System.out.println("运营商:"+ip.getIPLocation(address).getArea());

        System.out.println("广州市".indexOf("省")); //找不到返回-1
        System.out.println("xxx自治区".indexOf("自治"));
        System.out.println("xxx行政区".indexOf("行政"));
    }

}

/*
public static final String place[][] = {
		{"北京市","市辖区","市辖县"},
		{"天津市","市辖区","市辖县"},
		{"安徽省","安庆市","蚌埠市","亳州市","巢湖市","池州市","滁州市","阜阳市","合肥市","淮北市","淮南市","黄山市","六安市","马鞍山市","宿州市","铜陵市","芜湖市","宣城市"},
		{"澳门特别行政区","澳门"},
		{"香港特别行政区","香港"},
		{"福建省","福州市","龙岩市","南平市","宁德市","莆田市","泉州市","厦门市","漳州市"},
		{"甘肃省","白银市","定西市","甘南藏族自治州","嘉峪关市","金昌市","酒泉市","兰州市","临夏回族自治州","陇南市","平凉市","庆阳市","天水市","武威市","张掖市"},
		{"广东省","潮州市","东莞市","佛山市","广州市","河源市","惠州市","江门市","揭阳市","茂名市","梅州市","清远市","汕头市","汕尾市","韶关市","深圳市","阳江市","云浮市","湛江市","肇庆市","中山市","珠海市"},
		{"广西壮族自治区","百色市","北海市","崇左市","防城港市","贵港市","桂林市","河池市","贺州市","来宾市","柳州市","南宁市","钦州市","梧州市","玉林市"},
		{"贵州省","安顺市","毕节地区","贵阳市","六盘水市","黔东南苗族侗族自治州","黔南布依族苗族自治州","黔西南布依族苗族自治州","铜仁地区","遵义市"},
		{"海南省","海口市","三亚市","省直辖县级行政区划"},
		{"河北省","保定市","沧州市","承德市","邯郸市","衡水市","廊坊市","秦皇岛市","石家庄市","唐山市","邢台市","张家口市"},
		{"河南省","安阳市","鹤壁市","焦作市","开封市","洛阳市","漯河市","南阳市","平顶山市","濮阳市","三门峡市","商丘市","新乡市","信阳市","许昌市","郑州市","周口市","驻马店市"},
		{"黑龙江省","大庆市","大兴安岭地区","哈尔滨市","鹤岗市","黑河市","鸡西市","佳木斯市","牡丹江市","七台河市","齐齐哈尔市","双鸭山市","绥化市","伊春市"},
		{"湖北省","鄂州市","恩施土家族苗族自治州","黄冈市","黄石市","荆门市","荆州市","十堰市","随州市","武汉市","咸宁市","襄樊市","孝感市","宜昌市"},
		{"湖南省","长沙市","常德市","郴州市","衡阳市","怀化市","娄底市","邵阳市","湘潭市","湘西土家族苗族自治州","益阳市","永州市","岳阳市","张家界市","株洲市"},
		{"吉林省","白城市","白山市","长春市","吉林市","辽源市","四平市","松原市","通化市","延边朝鲜族自治州"},
		{"江苏省","常州市","淮安市","连云港市","南京市","南通市","苏州市","宿迁市","泰州市","无锡市","徐州市","盐城市","扬州市","镇江市"},
		{"江西省","抚州市","赣州市","吉安市","景德镇市","九江市","南昌市","萍乡市","上饶市","新余市","宜春市","鹰潭市"},
		{"辽宁省","鞍山市","本溪市","朝阳市","大连市","丹东市","抚顺市","阜新市","葫芦岛市","锦州市","辽阳市","盘锦市","沈阳市","铁岭市","营口市"},
		{"内蒙古自治区","阿拉善盟","巴彦淖尔市","包头市","赤峰市","鄂尔多斯市","呼和浩特市","呼伦贝尔市","通辽市","乌海市","乌兰察布市","锡林郭勒盟","兴安盟"},
		{"宁夏回族自治区","固原市","石嘴山市","吴忠市","银川市","中卫市"},
		{"青海省","果洛藏族自治州","海北藏族自治州","海东地区","海南藏族自治州","海西蒙古族藏族自治州","黄南藏族自治州","西宁市","玉树藏族自治州"},
		{"山东省","滨州市","德州市","东营市","菏泽市","济南市","济宁市","莱芜市","聊城市","临沂市","青岛市","日照市","泰安市","威海市","潍坊市","烟台市","枣庄市","淄博市"},
		{"山西省","长治市","大同市","晋城市","晋中市","临汾市","吕梁市","朔州市","太原市","忻州市","阳泉市","运城市"},
		{"陕西省","安康市","宝鸡市","汉中市","商洛市","铜川市","渭南市","西安市","咸阳市","延安市","榆林市"},
		{"四川省","阿坝藏族羌族自治州","巴中市","成都市","达州市","德阳市","甘孜藏族自治州","广安市","广元市","乐山市","凉山彝族自治州","泸州市","眉山市","绵阳市","内江市","南充市","攀枝花市","遂宁市","雅安市","宜宾市","资阳市","自贡市"},
		{"西藏自治区","阿里地区","昌都地区","拉萨市","林芝地区","那曲地区","日喀则地区","山南地区"},
		{"新疆维吾尔自治区","阿克苏地区","阿勒泰地区","巴音郭楞蒙古自治州","博尔塔拉蒙古自治州","昌吉回族自治州","哈密地区","和田地区","喀什地区","克拉玛依市","克孜勒苏柯尔克孜自治州","塔城地区","吐鲁番地区","乌鲁木齐市","伊犁哈萨克自治州","自治区直辖县级行政区划"},
		{"云南省","保山市","楚雄彝族自治州","大理白族自治州","德宏傣族景颇族自治州","迪庆藏族自治州","红河哈尼族彝族自治州","昆明市","丽江市","临沧市","怒江僳僳族自治州","普洱市","曲靖市","文山壮族苗族自治州","西双版纳傣族自治州","玉溪市","昭通市"},
		{"浙江省","杭州市","湖州市","嘉兴市","金华市","丽水市","宁波市","衢州市","绍兴市","台州市","温州市","舟山市"},
		{"重庆市","市辖区","市辖县","县级市"},
		{"台湾省","台北市","高雄市","基隆市","台中市","台南市","新竹市","嘉义市"},
	};
*/