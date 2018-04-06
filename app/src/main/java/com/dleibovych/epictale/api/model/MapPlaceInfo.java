package com.dleibovych.epictale.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dleibovych.epictale.api.dictionary.Race;
import com.dleibovych.epictale.util.ObjectUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Hamster
 * @since 07.10.2014
 */
public class MapPlaceInfo implements Parcelable {

    public final int id;
    public final int size;
    public final Race race;
    public final String name;
    public final int x;
    public final int y;

    public MapPlaceInfo(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        size = json.getInt("size");
        race = ObjectUtils.getEnumForCode(Race.class, json.getInt("race"));
        name = json.getString("name");
        x = json.getJSONObject("pos").getInt("x");
        y = json.getJSONObject("pos").getInt("y");
    }

    // Parcelable stuff

    private MapPlaceInfo(final Parcel in) {
        id = in.readInt();
        size = in.readInt();
        race = Race.values()[in.readInt()];
        name = in.readString();
        x = in.readInt();
        y = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(size);
        out.writeInt(race.ordinal());
        out.writeString(name);
        out.writeInt(x);
        out.writeInt(y);
    }

    public static final Parcelable.Creator<MapPlaceInfo> CREATOR = new Parcelable.Creator<MapPlaceInfo>() {
        @Override
        public MapPlaceInfo createFromParcel(Parcel source) {
            return new MapPlaceInfo(source);
        }

        @Override
        public MapPlaceInfo[] newArray(int size) {
            return new MapPlaceInfo[size];
        }
    };

}
