package org.uamusic.bot.telegram;

import it.tdlight.client.SimpleTelegramClient;
import lombok.Getter;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultAudio;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.uamusic.data.entity.DerivedData;
import org.uamusic.data.service.PG.PGDataService;
import org.uamusic.export.bot.ExportIntegrationService;
import org.uamusic.export.bot.TelegramExportIntegrationService;
import org.uamusic.export.selfbot.ExportService;
import org.uamusic.export.selfbot.SelfBotExportService;
import org.uamusic.export.selfbot.SelfBotSpecies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class TelegramBot extends TelegramLongPollingCommandBot {
    private final PGDataService dataService = new PGDataService();

    private final ExportService exportService = new SelfBotExportService(this);
    private final ExportIntegrationService integrationService = new TelegramExportIntegrationService(this);

    static final SimpleTelegramClient client = SelfBotSpecies.authorizeSync(Throwable::printStackTrace);

    public TelegramBot(){
        this.dataService.init();
        this.exportService.init();
        this.integrationService.init();
    }

    @Override
    public String getBotUsername() {
        return "UASound";
    }
    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }


    @Override
    public void processNonCommandUpdate(Update update) {
      if (!update.hasInlineQuery())
            return;

        final String inlineQueryRaw = update.getInlineQuery().getQuery().replace(" ", "");

        final Collection<DerivedData> dataCoincidences = getDataService().search(inlineQueryRaw);

        System.out.println(dataCoincidences);

        final List<InlineQueryResultAudio> articleList = new ArrayList<>();

        for (DerivedData data : dataCoincidences) {
            articleList.add(InlineQueryResultAudio.builder()
                    .id(Integer.toString(ThreadLocalRandom.current().nextInt()))
                    .title(data.getTrackName())
                    .audioDuration(Integer.valueOf(data.getTrackDuration()))
                    .build());
        }

        final AnswerInlineQuery inlineQuery = AnswerInlineQuery.builder()
                 .inlineQueryId(update.getInlineQuery().getId())
                 .cacheTime(10)
                 .results(articleList)
                .build();

        try {
            execute(inlineQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleTelegramClient getTelegramClient(){
        return client;
    }

    public PGDataService getDataService() {
        return dataService;
    }
}
