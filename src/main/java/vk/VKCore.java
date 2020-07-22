package vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;

import java.util.List;

public class VKCore {
    public VkApiClient vk;
    public GroupActor actor;
    private static int ts; // Временная метка
    private static int maxMsgId = -1; //Лимит id сообщений

    private static final String ACCESS_TOKEN =
            "4ceb90a149cec946a13e9f6a0ee7c5bbd04c29f494126242c571809192e595f839e291efe9891a9d0ddea"; // Ключ API
    private static final int GROUP_ID = 197345816;

    public VKCore() throws ClientException, ApiException {
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        actor = new GroupActor(GROUP_ID, ACCESS_TOKEN);
        ts = getTs();
    }

    public Message getMessage() throws ClientException, ApiException {
        //Создаем запрос для метода Messages.getLongPollHistory (https://vk.com/dev/messages.getLongPollHistory)
        MessagesGetLongPollHistoryQuery eventsQuery = vk.messages()
                .getLongPollHistory(actor)
                .ts(ts);

        if (maxMsgId > 0) {
            // Если значение maxMsgId было установлено (больше изначального -1)
            // Установить лимит id сообщений для GetLongPollHistoryResponse.getMessages().getItems()
            eventsQuery.maxMsgId(maxMsgId);
        }

        List<Message> messages = eventsQuery
                .execute()
                .getMessages()
                .getItems();

        if (!messages.isEmpty()) {
            try {
                ts = getTs();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }

        if (!messages.isEmpty() && !messages.get(0).isOut()) {
            int messageId = messages.get(0).getId();
            if (messageId > maxMsgId) {
                maxMsgId = messageId;
            }
            return messages.get(0);
        }
        return null;
    }

    private int getTs() throws ClientException, ApiException {
        return vk.messages()
                .getLongPollServer(actor) // создает запрос для метода Messages.getLongPollServer
                .execute() // возвращает LongPollParams
                .getTs(); // возвращает номер последнего события, начиная с которого нужно получать данные
    }
}
