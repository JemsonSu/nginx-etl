package com.bigdata;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPUtils
{
    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";
    public static final String GZIP_ENCODE_ISO_8859_1 = "ISO-8859-1";


    public static byte[] compress(String str, String encoding)
//    public static byte[] compress(String str, String encoding)
    {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(str.getBytes().length);
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);

            gzip.write(str.getBytes(encoding));
//            byte[] bytes = out.toByteArray();

            gzip.close();
        } catch ( Exception e) {
            e.printStackTrace();
        }

        byte[] bytes = out.toByteArray();
        try
        {
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
//        return new String(Base64Coder.encode(bytes));//Base64Coder 对 压缩数据 进行 编码
     }

    public static byte[] compress(String str) throws IOException
    {
        return compress(str, GZIP_ENCODE_UTF_8);
    }

    public static byte[] uncompress(byte[] bytes)
     {
        int bufferSize = 4096;
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in,bufferSize);
            byte[] buffer = new byte[bufferSize];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            ungzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes1 = out.toByteArray();

        try
        {
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return bytes1;
    }

    public static String uncompressToString(byte[] bytes, String encoding) throws IOException {
        int bufferSize = 4096;
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream ungzip = null;

        ungzip = new GZIPInputStream(in,bufferSize);
        byte[] buffer = new byte[bufferSize];
        int n;
        while ((n = ungzip.read(buffer)) > 0)
        {
            out.write(buffer, 0, n);
            out.flush();
        }

        if (ungzip!=null)
        {
            ungzip.close();
        }
        in.close();
        String s = out.toString(encoding);
        out.close();
        return s;
    }

    public static String uncompressToString(byte[] bytes) throws IOException {
        return uncompressToString(bytes, GZIP_ENCODE_UTF_8);
    }

//    public static void main(String[] args) throws IOException
//    {
//        String s = "hello world";
//        System.out.println("字符串长度："+s.length());
//        System.out.println("压缩后：："+compress(s));
//        System.out.println("解压后："+uncompress(compress(s)));
//        System.out.println("解压字符串后："+uncompressToString(compress(s)));
//        System.out.println("解压字符串后的字符串长度："+uncompressToString(compress(s)).length());
//
//
//        String tmp = "H4sIAAAAAAAAAI2Tv47UMBDGXwWZK5fI/xLb221FRYWEkBCKbM+YWLebhMSb1ep0BdUVFIgKHoGWhzqJewuc3J5YEIdIE83P+Wa+8UzeXJE6DdZf1hHI+hkrKWXMyEquSIo7JGtWlpWhUkrKK5rhsc+QLBKyIhDHFFufFjUx3nFhgdMscT4HhjtDQQQqmWCcM4FOaA/KciiNsR5LLx1w44wCqgx41FKL0nihUILiPASqBNcqGD8nARRSW3BaOMXLoLiVQA1jFE3lwYCujDAWjFBQBaOsE9l7cCJU3gSRy6BgUlShcpUMNmjUIZvwnjGlfWVyQ9voyPqKXCxvsmlh6HJrqwXUEw5j7Np8IApalDO2fX+GWcEfvt1harr5UnwH+AABk43bBe4KZ1PhG5u2ccL3hfUpTjEdixc2tptT8HR5yPWK4IRtysKLTd+/inh46QfENufth67HIUUc/+67G8/86YIVdKaAU/R4PzXQxgHKUJZUehvYP7rd5V5m+1OcuievOdvcV/i94rhYqw8RUpP3h2r6CzYY3zW5D84XurPtPk8g7QccTmkfu9SZtXbeSHJ38/n2+8cfXz7dfru5+/phVhxii";
//        byte[] bytes = tmp.getBytes("ISO8859-1");
//        String s1 = new String(bytes, "UTF-8");
//        String urldecode = URLDecoder.decode(s1, "UTF-8");
//        byte[] decode = Base64Coder.decode(urldecode);
//        String s2 = GZIPUtils.uncompressToString(decode);
//        System.out.println(s2);
//    }
}

