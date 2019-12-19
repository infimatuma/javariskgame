package hello;

import redis.clients.jedis.Jedis;

public class JedisConnection{
    public static Jedis link;

    public static void connect(){
        if(link == null){
            link = new Jedis("paris.cloudyhost.info", 6379);
            System.out.println("Autentification " + link.auth("Risk#777b&"));

            //link = new Jedis("localhost", 6379);

            System.out.println("Server Ping: " + link.ping());
        }
    }

    public static Jedis getLink() {
        if(link == null){
            JedisConnection.connect();
        }
        return link;
    }
}
