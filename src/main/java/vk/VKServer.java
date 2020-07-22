package vk;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import model.User;
import model.Word;

import java.util.HashMap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class VKServer {
    public static VKCore vkCore;
    public static List<Word> allWords = Arrays.asList(
        new Word("позволять","allow"),
        new Word("яблоко","apple"),
        new Word("машина","car"),
        new Word("небо","sky"),
        new Word("море","sea"),
        new Word("апельсиновый сок","orange juice"),
        new Word("космос","space"),
        new Word("мир","peace"),
        new Word("добрый","kind"),
        new Word("мочь","can"),
        new Word("уборка","clear"),
        new Word("игра","game"),
        new Word("дискусия","discussion"),
        new Word("долина","valley"),
        new Word("изумруд","emerald"),
        new Word("колокол","bell"),
        new Word("ложь","lie"),
        new Word("лягушка","frog"),
        new Word("магазин","shop"),
        new Word("марксизм","Мarxism"),
        new Word("марш","march"),
        new Word("нокаут","knock-out"),
        new Word("ночь","night"),
        new Word("обезьяна","monkey"),
        new Word("пейзаж","landscape"),
        new Word("фильм","film"),
        new Word("первый","first"),
        new Word("король","king"),
        new Word("школа","school"),
        new Word("университет","university"),
        new Word("печаль","sad"),
        new Word("персик","peach"),
        new Word("письмо","letter"),
        new Word("пилюля","pill"),
        new Word("полярный","аrctic"),
        new Word("сестра","sister"),
        new Word("сеть","net"),
        new Word("сильный","strong"),
        new Word("спортсмен","sportsman")
    );

    public static HashMap<Integer, User> userData = new HashMap<>();
    static {
        try{
            vkCore = new VKCore();

        }catch(ApiException | ClientException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running server...");

        while (true) {
            Thread.sleep(300);
            try {
                Message msg = vkCore.getMessage();
                if (msg != null && (!msg.getText().isEmpty())) {
                    Executors.newCachedThreadPool().execute(() -> sendMessage(getReplyMessage(msg), msg.getPeerId(), msg.getRandomId()));
                }
            } catch (ClientException e) {
                System.out.println("Try to reconnect...");
                Thread.sleep(10000);
            }
        }
    }

    public static String getReplyMessage(Message msg) {
        String userMessageText = msg.getText();
        int userId = msg.getPeerId();
        User data = userData.getOrDefault(userId, null);
        if (data == null) {
            data = new User(userId);
            userData.put(userId, data);

        }

        String  replyMessage;

        if(data.isTranslating()){
            if (data.currentWord.rus.equalsIgnoreCase(userMessageText)){
                data.totalCorrectAnswer++;
                replyMessage = "Верно! " + System.lineSeparator();
            } else{
                data.totalWrongAnswer++;
                replyMessage = "Ты ошибся " + System.lineSeparator();
            }
            replyMessage += "Слово: " + data.currentWord.eng + " Перевод:" + data.currentWord.rus;
            data.answerWords.add(data.currentWord);
            data.currentWord = null;

        }else if(userMessageText.equalsIgnoreCase("word")){
            Word translateWord = null;
            for (Word word: allWords){
                if(!data.answerWords.contains(word)){
                    translateWord = word;
                    break;
                }
            }
            if(translateWord == null) {
                replyMessage = "Ты Умудрился перевести всё! ";

            } else {
                data.currentWord = translateWord;
                replyMessage = " Переведи-ка... " + translateWord.eng;
            }
        } else if(userMessageText.equalsIgnoreCase("My score")) {
            replyMessage = "✅ - " + data.totalCorrectAnswer + System.lineSeparator() +
                    "⚠⚠⚠ " + data.totalWrongAnswer + System.lineSeparator() + " ⚠⚠⚠";
        } else {
            replyMessage = "Тааких приколов я ещё не видел🤨🤨🤨   '"+userMessageText+"'";
        }
        return replyMessage;

    }

    public static void sendMessage(String messageText, int userId, int randomId) {
        try {
            vkCore.vk.messages()
                    .send(vkCore.actor)
                    .userId(userId)
                    .randomId(randomId)
                    .message(messageText)
                    .execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }

}
