# Structure Events（结构事件）

Minecraft **Forge 1.20.1** 模组。

玩家走进世界生成的结构（村庄、神殿、要塞、海底神殿、林地府邸、远古城市、下界要塞、堡垒、末地城等）时，会触发：

- 屏幕大标题 + 副标题
- 动作栏 / 聊天提示
- 音效
- 药水效果
- 在玩家附近生成生物

也支持其它模组注册的结构，只要在配置里写上结构 ID。

## 环境

- Minecraft 1.20.1
- Forge **47.4.10**（其它 47.x 一般也可）
- JDK 17

## 编译

1. 安装 JDK 17。
2. 把本目录用 IntelliJ IDEA 打开（导入 `build.gradle`）。
3. 第一次需要联网下载 Forge/Minecraft 映射，时间较长。
4. 终端执行：

```bash
./gradlew genIntellijRuns
./gradlew build
```

如果没有 `gradlew`，先从 [Forge 1.20.1 MDK](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) 复制 `gradlew`、`gradlew.bat`、`gradle/wrapper/` 到本目录。

产物：`build/libs/structureevents-1.0.0.jar`  
丢进游戏的 `mods` 文件夹即可。

开发测试：IDEA 里运行 `runClient`。

## 游戏内命令

| 命令 | 权限 | 作用 |
| --- | --- | --- |
| `/structureevents check` | 所有人 | 查看当前位置检测到的结构 ID（含其它模组） |
| `/structureevents list` | 所有人 | 列出当前世界已注册的全部结构 ID |
| `/structureevents list yungsbetterstrongholds` | 所有人 | 只列出某个模组命名空间的结构 |
| `/structureevents dump` | OP 2 | 把全部结构 ID 写到游戏目录 `structureevents-structure-ids.txt` |
| `/structureevents reload` | OP 2 | 重新加载 JSON 事件表 |
| `/structureevents test minecraft:stronghold` | OP 2 | 立刻对当前玩家播放该结构事件 |

## 检测其它模组的结构

结构只要注册进了 `Registries.STRUCTURE`，和原版用同一套检测，不需要额外前置库。

在 `structureevents-events.json` 里把 `structureId` 写成该模组的 ID 即可，例如：

```json
{
  "structureId": "repurposed_structures:village_oak",
  "enabled": true,
  "title": "橡木村庄",
  "subtitle": "另一种风味的聚落",
  "mobs": [],
  "effects": []
}
```

查 ID 的方法：

1. 走进结构后执行 `/structureevents check`
2. `/structureevents list <模组id>`
3. `/locate structure` 按 Tab 补全
4. 模组 jar 里的 `data/<modid>/worldgen/structure/`

然后 `/structureevents reload`。

注意：有些模组的「建筑」其实是 feature / 自己的生成器，没有进结构注册表，这类检测不到。大型 Jigsaw 结构必须站在零件包围盒内。

## 配置

### `config/structureevents-common.toml`（也可在游戏里点 Common → Modify）

总开关、检测间隔、冷却、是否显示标题/刷怪等。

1.1.0 起在 `[structures]` 里有三份字符串列表，可在游戏配置界面增删：

| 项 | 空列表 | 有内容 |
| --- | --- | --- |
| `detectStructures` | 仍用 JSON 里 enabled 的结构 | 只检测名单中的结构 |
| `enterEffectStructures` | JSON 里写了 effects 就会给 | 只有命中的结构才给进入药水 |
| `leaveClearStructures` | 离开不清除（旧行为） | 离开时撤掉本模组进入时上过的药水 |

写法：`minecraft:desert_pyramid` 精确匹配；`minecraft:village` 匹配所有 `village_*`；`#minecraft:village` 匹配结构标签。

副标题、标题、刷怪、给哪种药：仍改 `structureevents-events.json`。

### `config/structureevents-events.json`

第一次启动世界时自动生成。默认只有标题、副标题和音效。`chat` 为空，`mobs` 和 `effects` 不预写。聊天总开关 `showChatMessage` 默认关闭。

字段说明：

```json
{
  "structureId": "minecraft:desert_pyramid",
  "enabled": true,
  "title": "沙漠神殿",
  "subtitle": "沙子下面藏着陷阱",
  "actionbar": "进入结构：沙漠神殿",
  "chat": "§c危险！§r你闯入了沙漠神殿！",
  "sound": "minecraft:entity.evoker.prepare_attack",
  "soundVolume": 1.0,
  "soundPitch": 1.0,
  "titleFadeIn": 10,
  "titleStay": 70,
  "titleFadeOut": 20,
  "cooldownSeconds": -1,
  "mobs": [
    { "entityId": "minecraft:husk", "count": 3, "radius": 5, "persist": false }
  ],
  "effects": [
    { "effectId": "minecraft:hunger", "seconds": 6, "amplifier": 0, "ambient": true, "particles": false }
  ]
}
```

- `structureId`：`/locate structure` 用的 ID，例如 `minecraft:village_plains`、`minecraft:ancient_city`。模组结构同样写成 `modid:name`。
- `cooldownSeconds`：`-1` 表示使用 toml 里的默认冷却（默认 180 秒）。同一座结构实例离开后再进来，冷却没到不会重复刷怪。
- `count: 0` 的刷怪项不会生成生物（远古城市默认不刷监守者）。

用 `/locate structure minecraft:xxx` 或本模组的 `/structureevents check` 查 ID。

## 注意

- 检测用的是 `StructureManager#getStructureWithPieceAt`，玩家必须真正站在结构零件的包围盒里，站在村庄很远的外围可能检测不到。
- 结构所在区块需要已经生成。
- 刷怪发生在服务器端，建议先在创造/测试世界调好 JSON 再用于生存。
- 默认不会给远古城市刷监守者；下界/末地的默认刷怪偏危险，请按需要改数量。

### `config/structureevents-biomes.json`

第一次进世界自动生成。群系持续药水和群系刷怪写在同一条里。人还在这个群系时，大约每秒补一次药水，所以牛奶清掉后还会再上来。离开群系就不再补。

```json
{
  "biomeId": "minecraft:dark_forest",
  "enabled": true,
  "effects": [
    { "effectId": "minecraft:darkness", "seconds": 12, "amplifier": 0, "ambient": true, "particles": false }
  ],
  "spawns": [
    {
      "entityId": "minecraft:zombie",
      "count": 1,
      "chance": 0.2,
      "intervalSeconds": 10,
      "minDistance": 12,
      "maxDistance": 22
    }
  ]
}
```

- `exemptItems`：身上有其中任一物品时，这个群系不再给药水，也不再刷怪。
- `exemptAdvancements`：完成其中任一进度后，这个群系不再给药水，也不再刷怪。进度 ID 用 `/advancement` 能看到的那种，例如 `minecraft:story/enter_the_nether`。

```json
"exemptItems": ["minecraft:torch"],
"exemptAdvancements": ["minecraft:adventure/kill_a_mob"]
```

旧的 `structureevents-biomes.json` 不会自动加这两项。自己补上，再 `/structureevents reload`。空列表或不写等于没有豁免。
- `chance`：每次间隔到了之后的刷怪概率，0 到 1。
- 怪刷在离玩家 `minDistance` 到 `maxDistance` 格的可站立位置，不刷在脚下。

### `config/structureevents-blocks.json`

右键使用方块时按概率刷怪。

```json
{
  "blockId": "minecraft:crafting_table",
  "enabled": false,
  "chance": 0.08,
  "entityId": "minecraft:witch",
  "count": 1,
  "radius": 5,
  "cooldownSeconds": 12
}
```

三份 JSON 都用 `/structureevents reload` 重读，不用重启。

## 许可

MIT。版权人 qiulin39。工程根目录和分发 jar 里都有 `LICENSE`。
