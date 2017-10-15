package com.unique.shop.spamer;

import com.unique.shop.utils.Auth;
import com.unique.shop.utils.Log;
import com.unique.shop.utils.Utils;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAuthException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.account.UserSettings;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.wall.WallCreateCommentQuery;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Spamer {

    private static int rnd_group_count = 20;

    private static String spamText =
                    "⚠ Maгaзин модной одежды\n" +
                            "\uD83C\uDF1E Хочешь выглядеть круто?\n" +
                            "\uD83D\uDD25 Огромные cкидки\n" +
                            "⛔ Работа без предоплаты\n" +
                            "\uD83D\uDD25 Известные бренды THRASHER, PALACE, GUCCI, BAPE..\n" +
                            "⛔ Конкурс на топ шмот\n" +
                            "\uD83D\uDC9A Подробнее у меня на стeнe! ";

    private static VkApiClient vk;
    private static List<UserActor> user = new ArrayList<>();
    private static Scanner scan = new Scanner(System.in);

    public static void main(String args[]) throws ClientException, ApiException {
        readFile("cashe");
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);


        String token;

        try {

            Log.send("DO", "1. Использовать кешированный токен");
            Log.send("DO", "2. Авторизовать новый аккаунт");
            Log.send("DO", "Укажите нужную цифру:");

            if (scan.nextInt() == 1) authCasheToken();
            else user.add(connectUser());

        } catch (NullPointerException ex) {
            ex.printStackTrace();
            user.add(connectUser());

        }

        List<String> groups = new ArrayList<>();
        try {

            for(String s : getCasheGroups()) groups.add(s);
            Log.send("DO", "1. Пропиарить в кешированном списке");
            Log.send("DO", "2. Создать новый список");
            Log.send("DO", "Укажите нужную цифру:");

            if (scan.nextInt() == 2) {

                Log.send("DO", "Вставьте набор ссылок, перечисленных через запятую:");
                String scanGroup = new Scanner(System.in).nextLine();
                saveList(scanGroup.split(","));

            }

        } catch (NullPointerException ex) {

            Log.send("2");

            Log.send("DO", "Вставьте набор ссылок, перечисленных через запятую:");
            String scanGroup = scan.nextLine();
            saveList(scanGroup.split(","));

        }

        //if (groups.length == 0) throw new NullPointerException("Список групп не найден");
        for(String s : getCasheGroups()) if (!groups.contains(s)) groups.add(s);
        Log.send("GROUPS", "Загружено " + groups.size() + " групп.");

        for (int a = 1; a <= rnd_group_count && a < groups.size(); a++) {

            String groupLink = groups.get(ThreadLocalRandom.current().nextInt(groups.size()));
            groups.remove(groupLink);

            try {

                String name = groupLink.replace("https://vk.com/", "");

                Log.send("[" + a + "/" + rnd_group_count + "] Обработка группы: " + name);

                Thread.sleep(30000L);
                List<WallpostFull> posts = vk.wall().get(getRandomUser()).domain(name).count(3).execute().getItems();

                for (WallpostFull post : posts) {

                    try {

                        Thread.sleep(15000L);

                        WallCreateCommentQuery query = vk.wall().createComment(getRandomUser(), post.getId()).message(spamText).guid(ThreadLocalRandom.current().longs() + "").ownerId(post.getOwnerId());
                        if (post.getComments().getCount() != 0) query.replyToComment(post.getComments().getCount());
                        query.execute();

                        Log.send("SPAM", "Создан коммент в группе " + name + " (" + groupLink + ")");

                    } catch (Exception ex) {

                        if (ex.getMessage().contains("(213)")) {
                            Log.send("Комментарии у группы " + name + " закрыты");
                            break;
                        }

                        Log.send("Ошибка при создании коммента: " + ex.getMessage());
                        System.out.println();
                    }

                }
            } catch (Exception ex) {
                Log.send("Ошибка при парсинге групп: " + ex.getMessage());
                System.out.println();
            }
        }
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

        Log.send("DO", "Укажите ссылку на токен:");

        Scanner scan = new Scanner(System.in);
        String token = scan.nextLine();
        saveToken(token);

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
            Log.send("Вы авторизовались под именем " + account.getFirstName() + " " + account.getLastName() + "(https://vk.com/id" + Integer.valueOf(a[1]) + ")" );
        } catch (ApiAuthException e) {
            Log.send("Неудачная авторизация пользователя с id " + a[1] + ".");
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return actor;
    }

    private static void saveToken(String token) {
        try {

            new File(System.getenv("APPDATA") + "\\unique___shop").mkdir();
            FileWriter writer = new FileWriter(System.getenv("APPDATA") + "\\unique___shop\\cashe.txt", false);
            writer.write(token + "\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveList(String... groups) {
        try {

            new File(System.getenv("APPDATA") + "\\unique___shop").mkdir();
            FileWriter writer = new FileWriter(System.getenv("APPDATA") + "\\unique___shop\\groups.txt", false);
            for (String s : groups) writer.write(s + "\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String name) {
        String text = "";
        try {
            new File(System.getenv("APPDATA") + "\\unique___shop\\" + name + ".txt").createNewFile();
            FileReader reader = new FileReader(System.getenv("APPDATA") + "\\unique___shop\\" + name + ".txt");
            int c;
            while ((c = reader.read()) != -1) text += (char) c;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }

    private static String authCasheToken() throws NullPointerException {

        if (!readFile("cashe").contains("token=")) throw new NullPointerException("Token not find in property");
        String[] cashes = readFile("cashe").split("\n");
        for (String s : cashes) user.add(connectUser(s));
        return Utils.extractPattern(readFile("cashe"), "token=(.*?)\n");

    }

    private static String[] getCasheGroups() throws NullPointerException {

        if (!readFile("cashe").contains("token=")) throw new NullPointerException("Groups not find in property");
        return readFile("groups").split("\n");
    }

    private static UserActor getRandomUser() {
        return user.get(ThreadLocalRandom.current().nextInt(user.size()));
    }

}
