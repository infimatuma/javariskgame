package lv.dium.riskserver;

import redis.clients.jedis.Jedis;

public class JedisConnection{
    public static Jedis link;
    private static volatile Boolean isLocked = false;

    public static void connect(){
        if(link == null){
            //link = new Jedis("paris.cloudyhost.info", 6379);
            //System.out.println("Autentification " + link.auth("Risk#777b&"));

            link = new Jedis("localhost", 6379);

            System.out.println("Server Ping: " + link.ping());
        }
    }

    public static Jedis getLink() {
        if(link == null){
            if(lock()) {
                JedisConnection.connect();
            }
        }
        return link;
    }

    private static synchronized boolean lock(){
        try {
            Integer totalWaitTime = 0;
            while(isLocked){
                Thread.sleep(100);
                totalWaitTime += 100;

                if(totalWaitTime > 5000){
                    throw new Exception("Failed to acquire game lock (5s).");
                }
            }
            unlock();
            isLocked = true;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    private static void unlock(){
        isLocked = false;
    }
}
