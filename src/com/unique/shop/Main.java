package com.unique.shop;

import com.google.gson.*;
import com.unique.shop.market.Shop;
import com.unique.shop.utils.Auth;
import com.unique.shop.utils.Log;
import com.unique.shop.utils.Utils;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAuthException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.account.UserSettings;
import com.vk.api.sdk.objects.market.MarketItem;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.responses.GetMarketUploadServerResponse;
import com.vk.api.sdk.queries.market.MarketAddQuery;
import com.vk.api.sdk.queries.market.MarketGetQuery;
import com.vk.api.sdk.queries.photos.PhotosSaveMarketPhotoQuery;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static String token = "your token";

    private static VkApiClient vk;
    private static UserActor user;
    private static GroupActor group;

    public static VkApiClient getVk() {
        return vk;
    }

    public static UserActor getUser() {
        return user;
    }

    public static GroupActor getGroup() {
        return group;
    }


    public static void main(String args[]) throws ClientException, ApiException, IOException, InterruptedException {


        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        user = connectUser(token);
        group = connectGroup("14ee84ca30ca1267c45ebcea66e57325266dcfa4b0f4c1f8dcfcdf0b5ca86874438afde48a45208891cad", 134278196);

        Object[] get = new Shop(126723340).getItemList(14);
        List<MarketItem> list = (List<MarketItem>) get[0];
        Map<Integer, List<String>> map = (Map<Integer, List<String>>) get[1];

        for (MarketItem item : list) {

            GetMarketUploadServerResponse server = Main.getVk().photos().getMarketUploadServer(Main.getUser(), 134278196).mainPhoto(true).cropX(0).cropY(0).cropWidth(604).execute();

            MarketAddQuery query = null;
            List<Integer> photos = new ArrayList<>();


            for (int q = 0; q < map.size(); q++) {

                if (q == 0) {

                    Photo photo = loadPhoto(map.get(item.getId()).get(q), server.getUploadUrl(), true).get(0);
                    //Main.getVk().photos().saveMarketPhoto(Main.getUser(), )
                    query = Main.getVk().market().
                            add(Main.getUser(),
                                    -134278196,
                                    item.getTitle(),
                                    "Название: " + item.getTitle() + "\nЦена: " + ((int) (Integer.valueOf(item.getPrice().getAmount()) * 0.009F) * 100F + 90F) + " рублей\n\nПо всем вопросам в сообщения сообщества.",
                                    26,
                                    ((int) (Integer.valueOf(item.getPrice().getAmount()) * 0.009F) * 100F + 90F),
                                    photo.getId());


                } else photos.add(loadPhoto(map.get(item.getId()).get(q), server.getUploadUrl(), false).get(0).getId());

            }

            query.photoIds(photos).execute();

            //loadPhoto(item.getThumbPhoto(), server.getUploadUrl());
            break;

            //Thread.sleep(500L);
            //Main.getVk().market().add(Main.getUser(), -134278196, item.getTitle(), item.getTitle(), 26, ((int) (Integer.valueOf(item.getPrice().getAmount()) * 0.009F) * 100F + 90F), 0);
            //System.out.println(item.getTitle() + " add this shop");


        }


    }

    public static List<Photo> loadPhoto(String photo, String url, boolean isMain) {

        try {


            InputStream in = new URL(photo).openStream();
            String random = ThreadLocalRandom.current().nextInt() + "";
            Files.copy(in, Paths.get("C:/Unique/cashe/" + random + ".jpg"));
            //Thread.sleep(1500L);
            File photoFile = new File("C:/Unique/cashe/" + random + ".jpg");

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            FileBody uploadFilePart = new FileBody(photoFile);
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file", uploadFilePart);
            httpPost.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(httpPost);

            String text = "";
            String json;
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            try {
                while ((json = br.readLine()) != null) text += json;
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JsonParser parser = new JsonParser();

            JsonObject obj = parser.parse(text).getAsJsonObject();
            int server = obj.get("server").getAsInt();
            String hash = obj.get("hash").getAsString();
            String hash_crop = obj.get("crop_hash").getAsString();
            String hash_data = obj.get("crop_data").getAsString();
            JsonPrimitive array = obj.get("photo").getAsJsonPrimitive();
            Log.send("server", server + "");
            Log.send("hash", hash);
            Log.send("array", obj.get("photo").toString().substring(1, obj.get("photo").toString().length()-1) );


            PhotosSaveMarketPhotoQuery query = Main.
                    getVk().
                    photos().
                    saveMarketPhoto(Main.getUser(), obj.get("photo").toString().substring(1, obj.get("photo").toString().length()-1), server, hash).
                    groupId(134278196);

            if (isMain && hash_crop != null && hash_data != null) query.cropHash(hash_crop).cropData(hash_data);

            Log.send( query.build().toString());
            query.execute();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static UserActor connectUser() {

        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(Auth.getUrl("6136168", vk)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Scanner scan = new Scanner(System.in);
        String token = scan.nextLine();

        return connectUser(token);
    }

    public static UserActor connectUser(String token) {

        String a[] = new String[0];
        try {
            a = Auth.parseRedirectUrl(token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserActor actor = new UserActor(Integer.valueOf(a[1]), a[0]);

        UserSettings account = null;
        try {
            account = vk.account().getProfileInfo(actor).execute();
            Log.send("Вы авторизовались под именем " + account.getFirstName() + " " + account.getLastName() + "(https://vk.com/id" + Integer.valueOf(a[1]) + ")");
        } catch (ApiAuthException e) {
            Log.send("Неудачная авторизация пользователя с id " + a[1] + ".");
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return actor;
    }

    public static GroupActor connectGroup(String token, int groupId) {

        GroupActor actor = new GroupActor(groupId, token);
        Log.send("Вы авторизовались под именем сообщества (https://vk.com/club" + groupId + ").");
        return actor;

    }

    public static String loadUrl(String url) {
        StringBuilder response = new StringBuilder();
        try {

            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.print(inputLine);
                response.append(inputLine);
            }

            System.out.println("");
            in.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }


}