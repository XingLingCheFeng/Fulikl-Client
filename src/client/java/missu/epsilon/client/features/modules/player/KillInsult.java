package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.game.EntityKilledEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.features.value.impl.TextValue;
import missu.epsilon.client.utils.miscs.RandomUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "KillInsult", category = ModuleCategory.PLAYER, description = "Automatically insult others when you kill them")
public class KillInsult extends Module {
    public static ListValue mode = new ListValue("Mode", new String[]{"Chinese Dirty", "Chinese Clean", "English Dirty", "English Clean"}, "English Clean");
    public static NumberValue delay = new NumberValue("Delay", 4000, 0, 20000, 50);
    public static BoolValue prefix = new BoolValue("PreFix", false);
    public static TextValue prefixValue = new TextValue("PrefixWords", "@");

    public static TimerUtils timer = new TimerUtils();

    @EventTarget
    public void onEntityKilled(EntityKilledEvent event) {
        if (mc.getNetworkHandler() == null) return;
        if (timer.hasTimeElapsed(delay.get())) {
            String[] insults = getCurrentInsults();
            String msg = event.targetName() + " " + insults[RandomUtils.nextInt(0, insults.length)];

            if (prefix.get()) {
                msg = prefixValue.get() + msg;
            }

            mc.getNetworkHandler().sendChatMessage(msg);
            timer.reset();
        }
    }

    private String[] getCurrentInsults() {
        return switch (mode.get()) {
            case "Chinese Dirty" -> dirtyChineseInsults;
            case "Chinese Clean" -> cleanChineseInsults;
            case "English Dirty" -> dirtyEnglishInsults;
            default -> cleanEnglishInsults;
        };
    }

    private static final String[] cleanEnglishInsults = new String[]{
            "Pray that Simon can save you in the game",
            "That's too bad, Get Epsilon quickly",
            "Oh, Im sorry about that haha",
            "You've done enough haha",
            "Get Epsilon today! Where to get? IDK haha",
            "Do you want staff to come and ban me now? hahaha",
            "That's too sad that you cant won the game",
            "Simon and his staff cant make a great anticheat oh no",
            "You cant kill me because I have Epsilon lol"
    };

    private static final String[] dirtyEnglishInsults = new String[]{
            "L Loser game and loser player lol",
            "L Let the nigger occupy your fucking country hahaha",
            "L You are trash so you cant kill Epsilon's user",
            "L I cooked two dishes today, your father and your mother",
            "L Your nigger hahaha",
            "LMAO Because you cant kill me and all the things you can do is to start next game",
            "L Pray that Simon's garbage anticheat and loser staff can make me disappear before the end of the game",
            "L Why garbage challenge me?",
            "L Its too easy to kill you loser",
            "L Simon and his staff are too incompetent so I don't get ban lol"
    };

    private static final String[] dirtyChineseInsults = new String[]{
            "小逼崽子你打也打不过你跟我扯乎你妈了个逼呢",
            "让你们的野爹服主和废物客服来封掉你爹行不行",
            "废物就是废物",
            "我就是一个三刀砍死你妈的",
            "火葬场打电话过来说你妈粘锅了",
            "你好你爹妈的骨灰拌饭做好了",
            "停停停在我车前面认领一下你妈的肉干",
            "你就是一五一十的窝囊废了你知不知道",
            "我断断续续操你妈妈",
            "你什么东西不自量力的和你爸爸我抗衡",
            "你非要我用你那泥土毫米衡量的脸来和我进行顽固对抗呢",
            "没点实力还在我面前耀武扬威的你不嫌丢脸吗"
    };

    private static final String[] cleanChineseInsults = new String[]{
            "不要气急败坏哦",
            "停止再在这个可悲的服务器里进行游戏",
            "真遗憾，希望你下一把游戏不要再遇见我了",
            "祈祷张震能让你们正常结束这局游戏吧",
            "我觉得，写这个的水平比你高",
            "我起的，有啥问题?",
            "转人工",
            "不收徒",
            "我承认你有实力，但是我更胜一筹",
            "开了吗，我说的是灵智",
            "大家好啊我是电棍，今天来点大家想看的东西"
    };
}
