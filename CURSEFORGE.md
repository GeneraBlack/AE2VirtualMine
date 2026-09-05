# ⛏️ AE2 Virtual Mine

**Bring virtual mining, digital quarrying, and automated ore extraction directly into your Applied Energistics 2 ME Network!**

Requires **Applied Energistics 2** and **NeoForge (Minecraft 1.21.1)**.

---

### 🌟 What is AE2 Virtual Mine?

**AE2 Virtual Mine** introduces generative **Virtual Mine Storage Cells** to Applied Energistics 2. Instead of building massive, lag-inducing chunk-eating quarries or complicated void-miner contraptions, you can now virtualize mineral mining and ore extraction straight inside your ME Drives or ME Chests!

Simply partition a Mining Cell with any raw ore, metal, mineral, or stone type, insert it into a powered ME Drive, and the cell will passively extract real ores and mining byproducts on a regular schedule.

---

### ✨ Key Features

* 📦 **5 Cell Tiers (1k to 256k):** Production scales with cell size.
  * **1k Mine Cell:** 1 drop every 3 seconds (0.5 AE/t idle drain)
  * **4k Mine Cell:** 4 drops every 3 seconds (1.0 AE/t idle drain)
  * **16k Mine Cell:** 16 drops every 3 seconds (2.0 AE/t idle drain)
  * **64k Mine Cell:** 64 drops every 3 seconds (4.0 AE/t idle drain)
  * **256k Mine Cell:** 256 drops every 3 seconds (8.0 AE/t idle drain)
* 🛑 **Zero Network Flooding (Smart Auto-Stop):**
  * Generated items are placed **strictly** into the cell itself.
  * Once the cell is full (byte or type capacity reached), the cell **automatically halts production**.
  * Items will **never** overflow into other drives or storage cells in your ME network!
* ⚡ **Zero Energy Waste:** If a cell is full, it consumes **zero AE energy** for drops until items are extracted from the cell.
* 💎 **Full Vanilla Ore & Quarry Support (Out of the Box):**
  * **Metals & Minerals:** Raw Iron (with cobblestone & gravel byproducts), Raw Copper (with granite), Raw Gold (with quartz/andesite), Nether Gold Ore (gold nuggets), Coal, Redstone, Lapis Lazuli, Diamond, Emerald, Ancient Debris (Netherite), Nether Quartz, Glowstone Dust, and Amethyst Shards (with calcite & basalt).
  * **Quarry & Stones:** Cobblestone, Stone, Deepslate, Cobbled Deepslate, Granite, Diorite, Andesite, Tuff, Calcite, Dripstone, Obsidian, Crying Obsidian, Netherrack, Basalt, Blackstone, End Stone, Sand, Red Sand, Gravel, Flint, and Clay.
* 🔍 **Automatic Modded Ore & Material Discovery:**
  * Automatically detects modded ores and raw materials via common NeoForge tags (`#c:ores`, `#c:raw_materials`, `#c:gems`, `#c:dusts`, `#c:stones`).
  * Yields the modded raw resource along with realistic quarry byproducts (Cobblestone or Cobbled Deepslate) without requiring manual configuration.
* 🛠️ **Two Easy Configuration Methods:**
  * **AE2 Cell Workbench:** Configure the ore or stone in the workbench partition filter.
  * **In-Hand Fast Config:** Sneak + Right-Click with a raw ore or stone in your off-hand to set it immediately! (Sneak + Right-Click with an empty off-hand resets the cell).
* 📊 **Authentic AE2 Tooltip:**
  * Real-time byte & type usage ("*X of Y Bytes used*") with dynamic color feedback (Green → Orange → Red).
  * Storage cell contents preview showing upgrade cards and item icons with amounts.
* 📋 **Datapack Extensible:** Add custom mining deposits or customize drop tables via standard JSON recipes (`ae2virtualmine:mine_drop`).

---

### ⚙️ Configuration Options

All settings are easily configurable in `config/ae2virtualmine-common.toml`:
* **Drop Interval:** Adjust the generation speed (default: 60 ticks / 3.0 seconds).
* **AE Energy Drain:** Adjust or disable the AE power cost per drop (default: 10.0 AE).
* **Drop Rates:** Configure how many items each cell tier yields per cycle.

---

### ❓ Frequently Asked Questions (FAQ)

**Q: Can I use this mod in my modpack?**
> **Yes, absolutely!** You are welcome to include AE2 Virtual Mine in any public or private modpack on CurseForge, Modrinth, or elsewhere.

**Q: Does it work with ME Chests as well as ME Drives?**
> Yes! Virtual Mine Cells work in both standard ME Drives and ME Chests.

**Q: What happens when the cell gets full?**
> It stops producing completely. It will not waste energy, and it will not push excess items into other storage cells on the network. As soon as you withdraw items via an ME Terminal or export bus, production resumes automatically.

**Q: How do I remove the configured deposit from a cell?**
> You can clear it either in an AE2 Cell Workbench by removing the filter item, or by holding the cell in your main hand with an empty off-hand and pressing **Sneak + Right-Click**.

---

### 📦 Dependencies

* **Minecraft 1.21.1**
* **NeoForge 21.1.172+**
* **Applied Energistics 2 (AE2) 19.2.10+**