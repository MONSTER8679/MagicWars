package com.example.magicwars;

import org.bukkit.Material;

public enum WandType {
    FIRE_STAFF("fire_staff", "عصا النار", Material.BLAZE_ROD, 20, 2.0, "تطلق كرة نار", false),
    ICE_WAND("ice_wand", "عصا الجليد", Material.PACKED_ICE, 25, 3.0, "تجمّد الأعداء وتبطئهم", false),
    LIGHTNING_WAND("lightning_wand", "عصا البرق", Material.LIGHTNING_ROD, 35, 5.0, "صاعقة مدمّرة", false),
    HEAL_WAND("heal_wand", "عصا الشفاء", Material.STICK, 15, 4.0, "تشفيك فورًا", false),
    BLINK_WAND("blink_wand", "عصا الانتقال", Material.ENDER_EYE, 30, 6.0, "تنقلك للأمام فجأة", false),
    WIND_WAND("wind_wand", "عصا الريح", Material.FEATHER, 18, 3.0, "تدفع الأعداء بقوة", true),
    VOID_WAND("void_wand", "شظية العدم", Material.ECHO_SHARD, 40, 6.0, "تسحب الأعداء إلى العدم", true),
    STORM_WAND("storm_wand", "عصا العاصفة", Material.WIND_CHARGE, 45, 8.0, "إعصار يرفع الأعداء", true),
    DRAGON_WAND("dragon_wand", "نَفَس التنين", Material.DRAGON_BREATH, 50, 9.0, "مخروط ناري يحرق أمامك", true),
    TIME_WAND("time_wand", "ساعة الرمال", Material.CLOCK, 55, 10.0, "مجال يجمّد كل الأعداء حولك", true),
    METEOR_WAND("meteor_wand", "حجر النيزك", Material.MAGMA_BLOCK, 60, 12.0, "نيزك يسقط من السماء ويحطم", true);

    public final String id;
    public final String displayName;
    public final Material material;
    public final int manaCost;
    public final double cooldownSec;
    public final String description;
    public final boolean purchasable;

    WandType(String id, String displayName, Material material, int manaCost,
             double cooldownSec, String description, boolean purchasable) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.manaCost = manaCost;
        this.cooldownSec = cooldownSec;
        this.description = description;
        this.purchasable = purchasable;
    }

    public static WandType byId(String id) {
        for (WandType t : values())
            if (t.id.equalsIgnoreCase(id)) return t;
        return null;
    }
}
