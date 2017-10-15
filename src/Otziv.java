import javax.swing.text.Document;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Otziv {

    public static void main(String args[]) {


        String def = "Отзыв о [<item>|<itemName>]: <blockquote>[[<photo_user>|40x40px|<user>]]'''[<user>|Яна Богданова]'''ᅠᅠ<gray>Доставка: 9 днейᅠᅠРегион: Центр. Россия</gray> \n" +
                "[[photo255579513_456243854|noborder;left;100x100px|https://pp.userapi.com/c841037/v841037027/a31a/W-QOVKNiT_w.jpg]] Заказала ветровку, качеством довольна, превзошла мои ожидания (тк имеет водоотталкивающее свойство). Спасибо Дмитрию☺\n" +
                "<br><br>\n" +
                "</blockquote>";

        String s = "\" /><title>Никита Сельский</title><link rel=\"s";
        System.out.println(  check("<title>[a-zA-Z]+</title>", s /*getText("https://vk.com/lucky_pepper")*/)  );

        /*Scanner r = new Scanner(System.in);

        System.out.println("Web link item:");
        def.replaceAll("<item>", r.nextLine());

        System.out.println("Item name:");
        def.replaceAll("<itemName>", r.nextLine());

        System.out.println("User link:");
        String link = r.nextLine();
        def.replaceAll("<user>", link);
        String name = check("<title>[a-zA-Z] [a-zA-Z] | ВКонтакте</title>", getText(link));*/


    }

    public static String getText(String url) {
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

    public static String check(String shablon, String text) {
        Pattern p = Pattern.compile(shablon);
        Matcher m = p.matcher(text);
        String line = null;
        while (m.find())
            line = "" + text.substring(m.start(), m.end());
        if (line == null)
            return null;
        return line;
    }


}
