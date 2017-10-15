package com.unique.shop;

import com.unique.shop.utils.Log;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetResponse;

import java.util.*;

public class AutoAnswer {

    private final int updatetime = 1;
    private final int timeanswer = 10;

    private static Set<Message> cashe = new HashSet<>();


    public AutoAnswer() {
        new Thread(()-> {

            while (true) {

                try {

                    List<Message> msgs = Main.getVk().messages().get(Main.getUser()).timeOffset(updatetime).out(false).execute().getItems();
                    if (msgs.size() != 0) Log.send("Find " + msgs.size() + " new msgs!");
                    cashe.addAll(msgs);

                    Thread.sleep(updatetime * 1000L + 1L);
                    check();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ApiException e) {
                    e.printStackTrace();
                } catch (ClientException e) {
                    e.printStackTrace();
                }


            }

        }).run();
    }

    public void check() {

        Iterator<Message> iter = cashe.iterator();
        if (iter.hasNext()) {

            Message msg = iter.next();

            Log.send(((System.currentTimeMillis() / 1000L) - timeanswer) + " - " + msg.getDate() + " >= " + 0);
            if (((System.currentTimeMillis() / 1000L) - timeanswer) - msg.getDate() >= 0) {

                //Main.getVk().messages().get

                try {


                    Main.getVk().messages().send(Main.getUser()).userId(msg.getUserId()).
                            message("Я, конечно, рад что ты написал(а) мне, но я сейчас временно отсутствую, поэтому ты можешь пока послушать музычку.").
                            attachment("audio255579513_456239123").execute();
                    cashe.remove(msg);


                } catch (ApiException e) {
                    e.printStackTrace();
                } catch (ClientException e) {
                    e.printStackTrace();
                }


            }


        }


    }


}
