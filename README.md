# 女仆手搓学（Maid：Hand Made）功能分类文档
> 让女仆可以使用[机械动力：手搓学]的物品进行加工

[English](#english) | [中文](#中文)

---

# **English**

## Introduction

Allows maids to use items from [Create: Hand Made] for processing tasks.

## Binding System

Tools that support binding (Press Hammer, Stirring Staff, Infusion Gun, Bellows) can bind to a target block. Once bound, the maid will keep working at that block until it is broken.


## Tool Usage

* **Press Hammer**
  - Usage: Give the maid a Press Hammer in her main hand. She will automatically find basins, belts, and depots with valid pressing recipes.
  - Details:
    - Shift + Right-click the target block with the hammer to bind it, then give the bound hammer to the maid. She will remember the position and keep working there until the block is broken.
    - Shift + Right-click air or an invalid block to clear binding; Shift + Right-click a new target to replace an existing binding.
    - If no binding is set, the maid will travel to any basin, belt, or depot that has a valid recipe.
    - Target validation only checks for recipes: basins must be non-empty and match a pressing recipe, depots must have an item and match a pressing recipe, belts must have at least one item that matches a pressing recipe.
    - Plays particles and mechanic press sound on each operation, and consumes 1 durability.
    - Only belts have special standing rules: the maid may not stand on the belt face or directly above it; basins and depots have no restriction.

* **Mortar**
  - Usage: Give the maid a Mortar in her main hand and grindable materials in her offhand. She will grind in place.
  - Details:
    - Place the mortar in main hand or backpack, and materials in offhand or backpack — the maid will start grinding automatically.
    - The maid stands still while grinding. Products enter her backpack on completion, and she will automatically restock and continue until materials run out or the mortar breaks.
    - If interrupted mid-grind, unfinished materials in the mortar are returned to the backpack — nothing is lost.

* **Crusher Mortar**
  - Usage: Give the maid a Crusher Mortar in her main hand and crushable materials in her offhand. She will crush in place.
  - Details:
    - Place the crusher mortar in main hand or backpack, and materials in offhand or backpack — the maid will start crushing automatically.
    - The maid stands still while crushing. Products enter her backpack on completion, and she will automatically restock and continue until materials run out or the crusher mortar breaks.
    - When no crushing recipe is available, it will automatically fall back to grinding recipes.
    - If interrupted mid-crush, unfinished materials in the crusher mortar are returned to the backpack — nothing is lost.

* **Pointer**
  - Usage: Give the maid a Pointer in her main hand and processing media in her backpack. She will automatically find depots and belts with items to process.
  - Details:
    - Right-click a depot with the pointer to bind it, then give the bound pointer to the maid. She will remember the position and keep working there until the block is broken.
    - If no binding is set, the maid will travel to any depot or belt with items.
    - **Offhand auto-stocking**: When the offhand is empty, the maid automatically searches her backpack for a medium that matches a recipe with the item on the target block, and moves it to her offhand.
    - Target validation only checks for item presence; the recipe is evaluated at processing time. If no matching recipe is found, no processing occurs.
    - Plays item pickup sound and matching particles on each operation, and consumes the offhand tool according to the recipe.

* **Stirring Staff**
  - Usage: Give the maid a Stirring Staff in her main hand. She will automatically find basins with valid stirring recipes.
  - Details:
    - Shift + Right-click a basin with the staff to bind it, then give the bound staff to the maid. She will remember the position and keep working there until the block is broken.
    - Shift + Right-click air or an invalid block to clear binding; Shift + Right-click a new target to replace an existing binding.
    - If no binding is set, the maid will travel to any basin with a valid recipe.
    - The maid stands still while stirring. From tick 8 onwards, plays sound and stirring particles every 4 ticks.
    - One stirring cycle lasts 60 ticks. On completion, applies the recipe output and consumes 1 durability.
    - If the basin has no matching recipe, stirring will not begin. If interrupted, the cycle is voided and no output is produced.

* **Infusion Gun**
  - Usage: Give the maid an Infusion Gun in her main hand. She will automatically choose between drawing, injecting, or storing fluid based on the gun's contents.
  - Details:
    - Shift + Right-click a block to bind it. The maid will prioritize working at the bound position. See "Binding System" above.
    - Give the infusion gun to the maid (main hand or backpack). She will automatically seek nearby fluid source blocks, fluid handlers, depots, and belts.
    - Gun is empty: Priority 1 — draw from fluid source blocks; Priority 2 — extract from fluid containers
    - Gun has fluid: Priority 1 — inject items on belts; Priority 2 — inject items on depots (matching infusion recipes or fillable items); Priority 3 — store into fluid containers.
    - **Basin rules**: Basins can only be drawn from (cannot be injected into).
    - Each operation consumes 1 durability, plays matching fluid sounds and particles. Depots update block state after injection.

* **Bellows**
  - Usage: Give the maid a Bellows in her main hand and processing media in her backpack. She will automatically process items on depots and belts.
  - Details:
    - Shift + Right-click a block to bind it. The maid will prioritize working at the bound position. See "Binding System" above.
    - Give the bellows to the maid (main hand or backpack). She will automatically process items on depots and belts.
    - **Offhand auto-stocking of media**: When the offhand is empty, the maid automatically searches her backpack for a medium recognized by the BellowsMediaRegistry (e.g. blaze rods, ice blocks, note blocks) and moves it to her offhand.
    - Target validation only checks for item presence; medium compatibility is evaluated at processing time. If no valid medium is available, no processing occurs.
    - Consumes 1 durability on success. The maid stands still while processing.

* **Hand Saw**
  - Usage: Give the maid a Hand Saw in her main hand and cuttable materials in her offhand. She will cut in place.
  - Details:
    - Place the hand saw in main hand or backpack, and materials in offhand or backpack — the maid will start cutting automatically.
    - The maid stands still while cutting. Produces one item every 30 ticks, and plays saw sound and particles every 4 ticks from tick 10 onwards.
    - Products enter the backpack on completion, and she will automatically restock and continue until the offhand runs out of materials or no valid recipe exists.
    - Supports multi-output and probability-based outputs, and preserves container residues. Each cut consumes 1 durability.
    - Automatically stops when the main hand no longer holds the hand saw or the offhand materials run out.

---

# **中文**

## 简介

让女仆可以使用[机械动力：手搓学]的物品进行加工

## 绑定系统

带有绑定功能的工具（冲压锤、搅拌杖、灌注枪、风箱）支持绑定方块，绑定后女仆会一直去该方块工作，直到方块被破坏。

## 工具使用

* **冲压锤(Press Hammer)**
  - 使用方法：女仆主手拥有冲压锤，自动寻找有配方的工作盆、传送带和置物台进行加工。
  - 细节：
    - 手持 Shift+右键绑定目标方块，再将绑定的冲压锤给女仆，女仆会记住方块位置并一直在该方块上加工；除非绑定的方块被破坏。
    - Shift+右键空气或无效方块可清除绑定；已有绑定时 Shift+右键新目标会自动替换。
    - 不绑定直接交给女仆时，会在工作盆、传送带和置物台上有配方时自动前往加工。
    - 判断目标只看有没有配方：工作盆需非空且匹配冲压配方，置物台需有物品且匹配 冲压 配方，传送带需带上至少一个物品匹配 冲压 配方。
    - 加工时播放粒子和机械压床音效，并消耗冲压锤 1 点耐久。
    - 站位上只有传送带特殊：不能站在带面上或带正上方，必须站旁边；工作盆、置物台不限。

* **研钵(Mortar)**
  - 使用方法：女仆主手拥有研钵，副手拥有可研磨的原料，自动原地加工。
  - 细节：
    - 将研钵和可研磨的原料交给女仆（主手或背包放研钵，副手或背包放原料），女仆会自动开始研磨。
    - 研磨期间女仆站定不动，完成后产物进入背包，并自动从背包补料连续研磨，直到没料或研钵损坏。
    - 过程中被打断时，研钵里未完成的原料会退回背包，不会丢失。

* **碾钵(Crusher Mortar)**
  - 使用方法：女仆主手拥有研钵，副手拥有可粉碎的原料，自动原地加工。
  - 细节：
    - 将碾钵和可粉碎的原料交给女仆（主手或背包放研钵，副手或背包放原料），女仆会自动开始粉碎。
    - 粉碎期间女仆站定不动，完成后产物进入背包，并自动从背包补料连续粉碎，直到没料或碾钵损坏。
    - 当没有粉碎配方时，会自动处理研磨配方。
    - 过程中被打断时，碾钵里未完成的原料会退回背包，不会丢失。

* **指杆(Pointer)**
  - 使用方法：女仆主手放指杆，背包放加工介质，自动寻找有物品的置物台和传送带进行加工。
  - 细节：
    - 手持右键置物台绑定方块位置，再将绑定的指杆给女仆，女仆会记住方块位置并一直在该方块上加工；除非绑定的方块被破坏。
    - 不绑定直接交给女仆时，会在置物台上有物品时自动前往加工。
    - **副手自动补料**：副手空时，女仆会自动从背包里找能和目标方块物品匹配配方的介质，搬到副手槽。
    - 加工目标只看有没有物品，配方在加工时再判；没有匹配配方则不会加工。
    - 加工时播放物品拾取音效和对应粒子，并按配方消耗副手工具。

* **搅拌杖(Stirring Staff)**
  - 使用方法：女仆主手拥有搅拌杖，自动寻找有配方的工作盆进行搅拌。
  - 细节：
    - 手持 Shift+右键工作盆绑定方块位置，再将绑定的搅拌杖给女仆，女仆会记住方块位置并一直在该方块上加工；除非绑定的方块被破坏。
    - Shift+右键空气或无效方块可清除绑定；已有绑定时 Shift+右键新目标会自动替换。
    - 不绑定直接交给女仆时，会在工作盆上有配方时自动前往加工。
    - 搅拌期间女仆站定不动，从第 8 tick 起每 4 tick 播放一次音效和搅拌粒子。
    - 一次搅拌持续 60 tick，完成后应用配方产出结果，并消耗搅拌杖 1 点耐久。
    - 盆里没有匹配配方时不会开始；过程中被打断则本次搅拌作废，不会产出。

* **灌注枪(Infusion Gun)**
  - 使用方法：女仆主手拥有灌注枪，根据枪内流体状态自动选择抽液、注液或存液。
  - 细节：
    - 手持 Shift+右键工作盆绑定方块位置，再将绑定的搅拌杖给女仆，女仆会记住方块位置并一直在该方块上加工；除非绑定的方块被破坏。
    - Shift+右键空气或无效方块可清除绑定；已有绑定时 Shift+右键新目标会自动替换。
    - 将灌注枪交给女仆（主手或背包放灌注枪），女仆会自动寻找附近的流体源、流体容器、置物台和传送带进行操作。
    - 枪内为空时：优先吸取流体源方块，其次从流体容器抽取流体。
    - 枪内有流体时：优先注入传送带上的物品，其次注入置物台上的物品（匹配灌注配方或可被填充的物品），最后存入流体容器。
    - 每次操作消耗灌注枪 1 点耐久，并播放对应的流体音效和粒子；置物台加工后会更新方块状态。

* **风箱(Bellows)**
  - 使用方法：女仆主手拥有风箱，背包放加工介质，自动加工置物台和传送带上的物品。
  - 细节：
    - 手持 Shift+右键绑定方块位置，女仆会优先去绑定位置操作；绑定系统见上方「绑定系统」。
    - 将风箱交给女仆（主手或背包放风箱），女仆会对置物台或传送带上的物品自动鼓风加工。
    - **副手自动补介质**：副手空时，女仆会自动从背包里找风箱可识别的介质（BellowsMediaRegistry 注册过的物品），搬到副手槽。
    - 加工目标只看有没有物品，介质能不能加工在加工时再判；没有对应介质则不会加工。
    - 加工成功时消耗风箱 1 点耐久，加工期间女仆站定不动。

* **手锯(Hand Saw)**
  - 使用方法：女仆主手拥有手锯，副手拥有可切削的原料，自动原地加工。
  - 细节：
    - 将手锯和可切削的原料交给女仆（主手或背包放手锯，副手或背包放原料），女仆会自动开始切削。
    - 切削期间女仆站定不动，每 30 tick 切一份，从第 10 tick 起每 4 tick 播放一次锯木音效和粒子。
    - 切削完成后产物进入背包，并自动从背包补料连续切削，直到副手没料或没配方。
    - 产物支持多产物和概率产物，并保留容器残留；切削一次消耗手锯 1 点耐久。
    - 主手不再是手锯或副手原料耗尽时自动结束。