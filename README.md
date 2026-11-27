# MagicWars ⚔

بلوقن حروب سحرية كامل لسيرفر **Paper 1.21.4** — Arabic Minecraft magic PvP plugin.

![Version](https://img.shields.io/badge/version-6.1.0-purple)
![Java](https://img.shields.io/badge/Java-21-orange)
![Paper](https://img.shields.io/badge/Paper-1.21.4-blue)

## ✨ المميزات

- **🗺️ الماندالا العظمى**: خريطة عملاقة 145×145 بتناظر 16 ضلعًا — زنبقة بتلات، زقورة سداسية بسلّم لولبي، خندق حمم وماء، 16 جسرًا و16 مسلة + 4 جزر قمرية عائمة (قمران لكل فريق)
- **🪄 11 عصا سحرية**: نار، جليد، برق، شفاء، انتقال، ريح، عدم، عاصفة، تنين، ساعة رمال، نيزك
- **👹 بوس**: الوحش البلوري (300 صحة) — ينتقم من مهاجمه ويوزع مكافآت على كل من شارك بقتله
- **⚡ التركيب الكامل بأمر واحد**: `/mwadmin setup` يبني اللوبي + منصة الأنماط + NPCs + عالم الملاعب + يولّد الملاعب تدريجيًا
- **💰 اقتصاد**: عملات وخبرة من القتل والفوز + متجر
- **🎨 مستحضرات**: 5 تأثيرات قتل + 5 آثار مشي تُشترى بالعملات
- **📅 مكافأة يومية**: `/daily` بسلسلة متزايدة
- **💎 جواهر قوة** تسقط في الملاعب أثناء المباريات

## 🚀 التثبيت

1. حمّل Java 21 + Maven
2. داخل مجلد المشروع:
   ```bash
   mvn package
   ```
3. انقل `target/magicwars-6.1.0.jar` إلى مجلد `plugins` في سيرفر Paper
4. شغّل السيرفر ثم نفّذ داخل اللعبة:
   ```
   /mwadmin setup
   ```
5. كل شيء جاهز — جرّب `/play solo`

## 📋 الأوامر

| الأمر | الوصف |
|---|---|
| `/mwadmin setup` | التركيب الكامل بضغطة واحدة |
| `/mwadmin boss` | استدعاء الوحش البلوري |
| `/play <solo\|duo\|quad>` | الانضمام لطابور نمط |
| `/mw give <player> <wand>` | إعطاء عصا |
| `/mw list` | قائمة العصي |
| `/daily` | المكافأة اليومية |
| `/killeffect list\|buy\|select\|off` | تأثيرات القتل |
| `/trail list\|buy\|select\|off` | آثار المشي |
| `/stats` | إحصائياتك |
| `/lobby` | العودة للوبي |

## 🏗️ البنية

```
src/main/java/com/example/magicwars/   (25 ملف Java)
├── MagicWarsPlugin.java    — الكلاس الرئيسي
├── SetupManager.java      — التركيب الكامل بأمر واحد
├── MapGenerator.java      — الماندالا العظمى 145×145
├── GameManager.java       — طوابير ومباريات
├── WandManager.java       — 11 تعويذة
├── BossManager.java       — الوحش البلوري
└── ...
```

## 🐛 إصلاحات v6.1

1. `MapGenerator`: خطأ فهرسة الجزر القمرية (`sat` → `sat[i]`)
2. `PowerupManager`: الجواهر كانت تُولد داخل الزقورة → الآن على الحلقة الصالحة للمشي
3. `PowerupManager`: `despawnNear` بنطاق الملعب بدل حذف كل جواهر العالم
4. `ShopManager` + `SpellBookGui`: توسيع من 9 إلى 18 خانة (تكفي 11 عصا)
5. `StatsManager`: حماية من المفاتيح التالفة
6. `NpcManager` + `PlayerListener` + `ArenaManager`: تنظيف استيرادات وسطور غير صالحة

## 📄 الترخيص

MIT
