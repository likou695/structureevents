package com.xdww.structureevents.config;

import java.util.ArrayList;
import java.util.List;

public final class DefaultEvents {
    private DefaultEvents() {
    }

    public static List<StructureEventDefinition> create() {
        List<StructureEventDefinition> list = new ArrayList<>();

        list.add(village("minecraft:village_plains", "平原村庄", "炊烟升起，村民正在忙碌"));
        list.add(village("minecraft:village_desert", "沙漠村庄", "烈日下的绿洲聚落"));
        list.add(village("minecraft:village_savanna", "热带草原村庄", "风吹过金黄的屋顶"));
        list.add(village("minecraft:village_snowy", "雪原村庄", "壁炉的暖意从屋里透出来"));
        list.add(village("minecraft:village_taiga", "针叶林村庄", "松针与木屋的味道"));

        list.add(structure("minecraft:desert_pyramid", "沙漠神殿", "沙子下面藏着陷阱", "minecraft:entity.evoker.prepare_attack"));
        list.add(structure("minecraft:jungle_pyramid", "丛林神庙", "藤蔓后有人在盯着你", "minecraft:entity.zombie.ambient"));
        list.add(structure("minecraft:swamp_hut", "沼泽小屋", "黑猫竖起了毛", "minecraft:entity.witch.ambient"));
        list.add(structure("minecraft:igloo", "雪屋", "地下室传来铁栏的碰撞声", "minecraft:block.iron_door.close"));

        list.add(structure("minecraft:pillager_outpost", "掠夺者前哨站", "弩已经上弦", "minecraft:entity.pillager.ambient"));
        list.add(structure("minecraft:mansion", "林地府邸", "走廊里回响着脚步", "minecraft:entity.vindicator.ambient"));
        list.add(structure("minecraft:monument", "海底神殿", "监护者苏醒了", "minecraft:entity.elder_guardian.curse"));

        list.add(structure("minecraft:mineshaft", "废弃矿井", "蛛网在黑暗中颤动", "minecraft:entity.spider.ambient"));
        list.add(structure("minecraft:mineshaft_mesa", "恶地矿井", "轨道深处有东西在爬", "minecraft:entity.spider.ambient"));

        list.add(structure("minecraft:stronghold", "要塞", "末地传送门就在附近", "minecraft:entity.silverfish.ambient"));
        list.add(structure("minecraft:ancient_city", "远古城市", "不要发出声音", "minecraft:ambient.cave"));

        list.add(structure("minecraft:fortress", "下界要塞", "烈焰人从桥上俯视", "minecraft:entity.blaze.ambient"));
        list.add(structure("minecraft:bastion_remnant", "堡垒遗迹", "猪灵发现了你的金饰……或者没有", "minecraft:entity.piglin.angry"));
        list.add(structure("minecraft:nether_fossil", "下界化石", "巨大的骸骨沉默地立着", "minecraft:entity.skeleton.ambient"));

        list.add(structure("minecraft:end_city", "末地城", "潜影贝藏在紫块后面", "minecraft:entity.shulker.ambient"));

        list.add(simpleTitle("minecraft:shipwreck", "沉船", "潮水拍打着腐朽的甲板"));
        list.add(simpleTitle("minecraft:shipwreck_beached", "搁浅沉船", "箱子可能还在船舱里"));
        list.add(simpleTitle("minecraft:ocean_ruin_cold", "寒冷海底废墟", "石砖被珊瑚覆盖"));
        list.add(simpleTitle("minecraft:ocean_ruin_warm", "温暖海底废墟", "沙子埋住了石阶"));
        list.add(simpleTitle("minecraft:buried_treasure", "埋藏的宝藏", "X 就在你脚下"));
        list.add(simpleTitle("minecraft:trail_ruins", "古迹废墟", "陶片记录着被遗忘的故事"));
        list.add(simpleTitle("minecraft:ruined_portal", "废弃传送门", "黑曜石还在微微发热"));
        list.add(simpleTitle("minecraft:ruined_portal_desert", "沙漠废弃传送门", "沙子几乎吞没了传送门"));
        list.add(simpleTitle("minecraft:ruined_portal_jungle", "丛林废弃传送门", "藤蔓缠住了黑曜石"));
        list.add(simpleTitle("minecraft:ruined_portal_mountain", "山地废弃传送门", "风从破碎的门框穿过"));
        list.add(simpleTitle("minecraft:ruined_portal_nether", "下界废弃传送门", "另一侧曾经通往主世界"));
        list.add(simpleTitle("minecraft:ruined_portal_ocean", "海底废弃传送门", "气泡从裂缝里冒出"));
        list.add(simpleTitle("minecraft:ruined_portal_swamp", "沼泽废弃传送门", "泥水浸过黑曜石"));

        return list;
    }

    private static StructureEventDefinition village(String id, String title, String subtitle) {
        StructureEventDefinition def = simpleTitle(id, title, subtitle);
        def.sound = "minecraft:entity.villager.ambient";
        return def;
    }

    private static StructureEventDefinition structure(String id, String title, String subtitle, String sound) {
        StructureEventDefinition def = simpleTitle(id, title, subtitle);
        def.sound = sound;
        def.soundVolume = 1.0f;
        return def;
    }

    private static StructureEventDefinition simpleTitle(String id, String title, String subtitle) {
        StructureEventDefinition def = new StructureEventDefinition();
        def.structureId = id;
        def.title = title;
        def.subtitle = subtitle;
        def.actionbar = "进入结构：" + title;
        def.chat = "";
        def.sound = "minecraft:block.note_block.chime";
        def.soundVolume = 0.7f;
        return def;
    }
}
