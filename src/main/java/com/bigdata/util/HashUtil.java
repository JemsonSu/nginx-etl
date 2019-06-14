package com.bigdata.util;

/**
 * 古智鹏提供
 * @author xian
 *
 */

public class HashUtil {
//    private static final Logger logger;
    public static long bkdrHashFactor=31L;
//    public static long bkdrHashFactor;

    public static long bkdrhash(String string) {
        long l = 0L;
        if (string.length() > 0) {
            char[] arrc = string.toCharArray();
            for (int i = 0; i < arrc.length; ++i) {
                l = 31L * l + (long)arrc[i];
            }
        }
        return l;
    }

    public static long userIdHash(String string) {
        if (bkdrHashFactor == -1L) {
//            throw new RuntimeException("Init bkdr hash factor failed");
        	
        	System.err.println("Init bkdr hash factor failed");
        }
        long l = 0L;
        if (string.length() > 0) {
            char[] arrc = string.toCharArray();
            for (int i = 0; i < arrc.length; ++i) {
                l = bkdrHashFactor * l + (long)arrc[i];
            }
        }
        return l;
    }

    public static long userIdHash(byte[] arrby) {
        if (bkdrHashFactor == -1L) {
//            throw new RuntimeException("Init bkdr hash factor failed");
        	System.err.println("Init bkdr hash factor failed");
        }
        long l = 0L;
        for (byte by : arrby) {
            l = bkdrHashFactor * l + (long)by;
        }
        return l;
    }

   /* static {
        logger = LoggerFactory.getLogger(HashUtil.class);
        bkdrHashFactor = 31L;
        ZookeeperClient zookeeperClient = null;
        try {
            zookeeperClient = new ZookeeperClient();
            bkdrHashFactor = zookeeperClient.getGlobalConfigInfo().getBkdrHashFactor();
        }
        catch (Exception exception) {
            logger.error("Can't init bkdr hash factor", exception);
            bkdrHashFactor = -1L;
        }
        finally {
            if (zookeeperClient != null) {
                try {
                    zookeeperClient.close();
                }
                catch (Exception exception) {
                    logger.error("Close zookeeper client failed", exception);
                }
            }
        }
    }*/


//    public static void main(String[] args) {
//
////
//        long l = userIdHash("1995082719960117");
//        System.out.println(l);
//
//
//    }



}

