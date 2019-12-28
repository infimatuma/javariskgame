package lv.dium.riskserver;

import redis.clients.jedis.Jedis;

public class JedisLocal {
    public static Jedis link;

    public static void connect(){
        if(link == null){
            link = new Jedis("localhost", 6379);
            System.out.println("Server Ping: " + link.ping());
        }
    }

    public static Jedis getLink() {
        if(link == null){
            JedisLocal.connect();
        }
        return link;
    }
}
