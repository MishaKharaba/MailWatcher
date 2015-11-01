package com.eleks.mailwatcher.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.gmail.model.Label;

public class LabelRec implements Parcelable
{
    public String id;
    public String name;

    public LabelRec(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    protected LabelRec(Parcel in)
    {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<LabelRec> CREATOR = new Creator<LabelRec>()
    {
        @Override
        public LabelRec createFromParcel(Parcel in)
        {
            return new LabelRec(in);
        }

        @Override
        public LabelRec[] newArray(int size)
        {
            return new LabelRec[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(name);
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof LabelRec)) return false;
        LabelRec l = (LabelRec) o;
        boolean b = equals(id, l.id);
        if (b)
            b = equals(name, l.name);
        return b;
    }

    private static boolean equals(String s1, String s2)
    {
        if (s1 == s2) return true;
        if (s1 == null) return false;
        return s1.equals(s2);
    }
}
