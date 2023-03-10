package org.uasound.bot.telegram.chat.export.bot;

import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.uasound.data.entity.SharedAudio;
import org.uasound.bot.telegram.chat.export.selfbot.SelfBotAdapter;
import org.uasound.data.entity.DerivedData;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class BotIntegrationStrategy {
    private final ExportIntegrationService service;

    private final SelfBotAdapter client;

    @Setter @Getter
    private long bucketChat;

    public void integrate(final DerivedData data, final Consumer<SharedAudio> consumer){
        client.send(new TdApi.ForwardMessages(
                TelegramIntegrationService.BUCKET_CHAT_ID,
                0,
                data.getGroupId(),
                new long[]{ data.getPostId() },
                null,
                false,
                true,
                false
        ), (res) ->
                service.getUpdatesHandler().await(data, (data2, sharedAudio) ->
                consumer.accept(sharedAudio)));
    }

    public SharedAudio transform(final DerivedData data, final Message message){
        return SharedAudio.builder()
                .internalId(data.getInternalId())
                .postId(data.getPostId())
                .bucketId(message.getChatId())
                .messageId(message.getMessageId())
                .fileUniqueId(message.getAudio().getFileUniqueId())
                .fileId(message.getAudio().getFileId())
                .remoteFileId(message.getAudio().getFileUniqueId())
                .schema("0.2b")
                .build();
    }
}
