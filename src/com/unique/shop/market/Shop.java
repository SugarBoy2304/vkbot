package com.unique.shop.market;

import com.google.gson.*;
import com.unique.shop.Main;
import com.unique.shop.utils.Log;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.market.MarketItem;
import com.vk.api.sdk.queries.market.MarketGetQuery;
import jdk.nashorn.internal.parser.JSONParser;

import javax.management.ObjectName;
import javax.xml.ws.Response;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop {

    private static int id;
    private static MarketGetQuery query;

    public Shop(int id) {
        this.id = id;
        try {
            List<GroupFull> list = Main.getVk().groups().getById(Main.getUser()).groupId(id + "").execute();
            Log.send("Группа " + list.get(0).getName() + " была найдена.");
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        query = Main.getVk().market().get(Main.getUser(), -id);

        /*Scanner scan = new Scanner(System.in);
        while (true) {

            Log.send("ITEMLIST | SAVEITEMS | BACK");
            String[] a = scan.nextLine().split(Pattern.quote(" "));
            String com = a[0];

            if (com.equalsIgnoreCase("ITEMLIST")) {

                for (MarketItem item : getItemList()) Log.send(item.getTitle());

            } else if (com.equalsIgnoreCase("SAVEITEMS")) {

                saveItems();

            } else if (com.equalsIgnoreCase("back")) break;*/

    }

    public Object[] getItemList(int album) {

        List<MarketItem> all = new ArrayList<>();
        Map<Integer, List<String>> map = new HashMap<>();

        boolean b = true;
        int offset = 0;

        while (b) {
            try {

                Thread.sleep(350L);
                MarketGetQuery query = Main.getVk().market().get(Main.getUser(), -id).offset(offset * 100).albumId(album);
                Map<String, Integer> extended = new HashMap<>();
                extended.put("photos", 1);
                query.unsafeParam("extended", extended);

                List<MarketItem> cashe = query.execute().getItems();

                map.putAll(parseImg(query.executeAsString()));

                Log.send("Load items " + (offset * 100 + cashe.size()));
                offset++;
                if (cashe.size() < 100) b = false;
                all.addAll(cashe);

            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new Object[]{ all, map };
    }

    public List<MarketItem> getItemList() {

        List<MarketItem> all = new ArrayList<>();

        boolean b = true;
        int offset = 0;


        while (b) {
            try {

                Thread.sleep(350L);
                List<MarketItem> cash = Main.getVk().market().get(Main.getUser(), -id).offset(offset * 100).execute().getItems();
                Log.send("Load items " + (offset * 100 + cash.size()));
                offset++;
                if (cash.size() < 100) b = false;
                all.addAll(cash);

            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return all;
    }

    public void saveItems() {
        try {
            System.out.println(query.offset(100).count(1).executeAsRaw().getContent());
            Log.send("________________________________");
            List<MarketItem> cash = query.offset(100).count(1).execute().getItems();
            for (MarketItem item : cash) {


                Log.send(item.getTitle());

                Log.send(item.getThumbPhoto());


            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, List<String>> parseImg(String text) {

        Map<Integer, List<String>> map = new HashMap<>();

        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(text).getAsJsonObject().getAsJsonObject("response").getAsJsonArray("items");

        for (int q = 0; q < json.size(); q++) {
            List<String> list = new ArrayList<>();

            JsonObject obj = json.get(q).getAsJsonObject();

            int id = obj.get("id").getAsInt();
            JsonArray photosJson = obj.getAsJsonArray("photos");
            for (int a = 0; a < photosJson.size(); a++) list.add(photosJson.get(a).getAsJsonObject().get("photo_604").getAsString());
            map.put(id, list);
        }

        return map;
    }

}
