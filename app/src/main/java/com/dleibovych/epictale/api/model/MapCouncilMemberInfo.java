package com.dleibovych.epictale.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dleibovych.epictale.api.dictionary.Gender;
import com.dleibovych.epictale.api.dictionary.Profession;
import com.dleibovych.epictale.api.dictionary.ProficiencyLevel;
import com.dleibovych.epictale.api.dictionary.Race;

/**
 * @author Hamster
 * @since 25.12.2014
 */
public class MapCouncilMemberInfo implements Parcelable {

    public final String name;
    public final Gender gender;
    public final Race race;
    public final Profession profession;
    public final ProficiencyLevel proficiencyLevel;
    public final int power;
    public final float powerBonusPositive;
    public final float powerBonusNegative;
    public final int friends;
    public final int enemies;
    
    public MapCouncilMemberInfo(final String name, final Gender gender, final Race race,
                                final Profession profession, final ProficiencyLevel proficiencyLevel,
                                final int power, final float powerBonusPositive, final float powerBonusNegative,
                                final int friends, final int enemies) {
        this.name = name;
        this.gender = gender;
        this.race = race;
        this.profession = profession;
        this.proficiencyLevel = proficiencyLevel;
        this.power = power;
        this.powerBonusPositive = powerBonusPositive;
        this.powerBonusNegative = powerBonusNegative;
        this.friends = friends;
        this.enemies = enemies;
    }

    // Parcelable stuff

    public MapCouncilMemberInfo(final Parcel in) {
        name = in.readString();
        gender = Gender.values()[in.readInt()];
        race = Race.values()[in.readInt()];
        profession = Profession.values()[in.readInt()];
        proficiencyLevel = ProficiencyLevel.values()[in.readInt()];
        power = in.readInt();
        powerBonusPositive = in.readFloat();
        powerBonusNegative = in.readFloat();
        friends = in.readInt();
        enemies = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeInt(gender.ordinal());
        out.writeInt(race.ordinal());
        out.writeInt(profession.ordinal());
        out.writeInt(proficiencyLevel.ordinal());
        out.writeInt(power);
        out.writeFloat(powerBonusPositive);
        out.writeFloat(powerBonusNegative);
        out.writeInt(friends);
        out.writeInt(enemies);
    }

    public static final Parcelable.Creator<MapCouncilMemberInfo> CREATOR = new Parcelable.Creator<MapCouncilMemberInfo>() {
        @Override
        public MapCouncilMemberInfo createFromParcel(Parcel source) {
            return new MapCouncilMemberInfo(source);
        }

        @Override
        public MapCouncilMemberInfo[] newArray(int size) {
            return new MapCouncilMemberInfo[size];
        }
    };

}
