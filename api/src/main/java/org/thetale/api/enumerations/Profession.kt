package org.thetale.api.enumerations

import org.thetale.api.R

enum class Profession constructor(val code: Int, val professionName: String, val iconResId: Int) {

    SMITH(0, "кузнец", R.drawable.profession_blacksmith),
    FISHER(1, "рыбак", R.drawable.profession_fisherman),
    TAILOR(2, "портной", R.drawable.profession_tailor),
    CARPENTER(3, "плотник", R.drawable.profession_carpenter),
    HUNTER(4, "охотник", R.drawable.profession_hunter),
    GUARD(5, "стражник", R.drawable.profession_warden),
    TRADER(6, "торговец", R.drawable.profession_merchant),
    INNKEEPER(7, "трактирщик", R.drawable.profession_innkeeper),
    THIEF(8, "вор", R.drawable.profession_rogue),
    FARMER(9, "фермер", R.drawable.profession_farmer),
    MINER(10, "шахтёр", R.drawable.profession_miner),
    PRIEST(11, "священник", R.drawable.profession_priest),
    HEALER(12, "лекарь", R.drawable.profession_physician),
    ALCHEMIST(13, "алхимик", R.drawable.profession_alchemist),
    EXECUTIONER(14, "палач", R.drawable.profession_executioner),
    WIZARD(15, "волшебник", R.drawable.profession_magician),
    MAYOR(16, "мэр", R.drawable.profession_mayor),
    BUREAUCRAT(17, "бюрократ", R.drawable.profession_bureaucrat),
    ARISTOCRAT(18, "аристократ", R.drawable.profession_aristocrat),
    BARD(19, "бард", R.drawable.profession_bard),
    TRAINER(20, "дрессировщик", R.drawable.profession_tamer),
    HERDER(21, "скотовод", R.drawable.profession_herdsman)

}
