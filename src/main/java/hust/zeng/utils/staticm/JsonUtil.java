package hust.zeng.utils.staticm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hust.zeng.utils.pojo.Group;
import hust.zeng.utils.pojo.User;

public class JsonUtil {

    /**
     * 将对象（包括数组）转为String
     * 
     * @param object
     * @return
     */
    public static String toJSON(Object object) {
        return JSON.toJSONString(object);
    }

    /**
     * 将String转为JSONObject
     * 
     * @param text
     * @return
     */
    public static JSONObject fromJSON(String text) {
        return JSON.parseObject(text);
    }

    /**
     * 将String转为T
     * 
     * @param text
     * @param clazz
     * @return
     */
    public static <T> T fromJSON(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * 将String转为JSONArray
     * 
     * @param text
     * @return
     */
    public static JSONArray parseArray(String text) {
        return JSON.parseArray(text);
    }

    /**
     * 将String转为List<T>
     * 
     * @param text
     * @param clazz
     * @return
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    
    public static void main(String[] args) {

        System.out.println("1.Object >> String");

        // 1.1.将普通对象转为JsonString
        Group group = new Group();
        group.setId(1L);
        group.setName("group1");

        User guestUser = new User();
        guestUser.setId(10L);
        guestUser.setName("admin");

        User rootUser = new User();
        rootUser.setId(11L);
        rootUser.setName("guest");

        group.addUser(guestUser);
        group.addUser(rootUser);

        String voString = JSON.toJSONString(group);
        System.out.println(voString);

        // 1.2.将Map对象转为JsonString
        HashMap<String, Object> map = new HashMap<>(); //JSONObject map = new JSONObject();
        map.put("id", 7);
        HashMap<String, Object> info = new HashMap<>();
        info.put("name", "HUST");
        info.put("adreess", "wuhan");
        map.put("info", info);
        String mapStr = JSON.toJSONString(map);
        System.out.println(mapStr);

        // 1.3.将List对象转为JsonString
        ArrayList<String> list = new ArrayList<>(); //JSONArray list = new JSONArray();
        list.add("aa");
        list.add("bb");
        String listStr = JSON.toJSONString(list);
        System.out.println(listStr);

        // 1.4.将数组对象转为JsonString
        Object[] array = new Object[3];
        array[0] = 1;
        array[1] = 2;
        array[2] = 3;
        String arrayStr = JSON.toJSONString(array);
        System.out.println(arrayStr);

        System.out.println("2.String → T 或 Map<String, Object>");

        // 2.1.将String转为T
        Group group2 = JSON.parseObject(voString, Group.class);
        System.out.println(group2);

        // 2.2.将String转为JSONObject
        Map<String, Object> map2 = JSON.parseObject(mapStr);
        System.out.println(map2);

        System.out.println("3.String → List<T> 或 List<Object>");
        
        // 3.1.将String转为List<T>
        List<String> list2 = JSON.parseArray(listStr, String.class);
        System.out.println(list2);

        // 3.2.将String转为JSONArray
        List<Object> array2 = JSON.parseArray(arrayStr);
        System.out.println(array2);

    }
}
