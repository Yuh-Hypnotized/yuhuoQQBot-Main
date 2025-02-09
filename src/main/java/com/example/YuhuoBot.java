package com.example;

import com.google.gson.Gson;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageRecallEvent.GroupRecall;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public final class YuhuoBot extends JavaPlugin {
    public static final YuhuoBot INSTANCE = new YuhuoBot();

    private YuhuoBot() {
        super(new JvmPluginDescriptionBuilder("com.example.yuhuoBot", "0.1.0")
                .name("yuhuoBot")
                .author("Yuh_Hypnotized")

                .build());
    }

    private List<messageInfo> messageCache = new ArrayList<>();

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");

        Config config = loadConfig();
        if (config == null) {
            getLogger().error("Failed to load config file!");
            return;
        }

        List<Long> whitelistedGroupID = config.whitelistedGroupID;
        List<Long> adminID = config.adminID;

        Listener<GroupMessageEvent> listener = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                event -> {
                    MessageChain message = event.getMessage();
                    int messageID = event.getSource().getIds()[0];
                    long groupID = event.getGroup().getId();
                    long userID = event.getSender().getId();

                    if (whitelistedGroupID.contains(groupID)) {
                        messageCache.add(new messageInfo(messageID, message));

                        // 提取纯文本内容（避免包含 Mirai 内部格式）
                        String messageString = message.contentToString().trim(); // 去除前后空格

                        if (messageString.startsWith("/")) {
                            // 分割命令和参数（例如 "/help" → ["help"], "/anti-recall toggle" → ["anti-recall", "toggle"]）
                            String[] commandParts = messageString.substring(1).split("\\s+"); // 按空格分割

                            if (commandParts.length == 0) {
                                return; // 空命令
                            }

                            String mainCommand = commandParts[0].toLowerCase(); // 统一小写处理

                            switch (mainCommand) {
                                case "help":
                                case "yuhuo":
                                    event.getGroup().sendMessage(
                                            "/help, /yuhuo - 帮助菜单\n" +
                                                    "/anti-recall toggle - 开/关防撤回\n" +
                                                    "----------\n" +
                                                    "/24 start - 开始一局24点游戏\n" +
                                                    "/24 ff - 放弃一局24点游戏\n" +
                                                    "/24 answer <answer> - 回答24点游戏的答案\n" +
                                                    "/24 lb - 查看本群24点游戏排行榜\n" +
                                                    "----------\n" +
                                                    "/gob join - 五子棋 上桌\n" +
                                                    "/gob leave - 五子棋 下桌\n" +
                                                    "/gob start - 五子棋 开始游戏\n" +
                                                    "/gob play <row> <col> - 在第row行第col列落子\n" +
                                                    "/gp <row> <col> - 同上一条，gob play的缩写\n" +
                                                    "/gob ff - 弃权，判对手获胜\n" +
                                                    "/gob status - 查询五子棋游戏状态\n" +
                                                    "/gob profile - 查询五子棋个人资料/数据\n" +
                                                    "/gob lb - 查询本群五子棋积分排行榜\n" +
                                                    "                yuhuo Bot v0.2.2"
                                    );
                                    break;
                            }
                        }
                    }
                });

    }

    private Config loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.json")) {
            if (input == null) {
                getLogger().error("Config file not found!");
                return null;
            }
            InputStreamReader reader = new InputStreamReader(input);
            return new Gson().fromJson(reader, Config.class);
        }
        catch (Exception e) {
            getLogger().error("Failed to load config file!", e);
            return null;
        }
    }
}