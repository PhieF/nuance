package com.spipi.spipimediaplayer;

import android.view.View;

/**
 * Created by alexandre on 26/05/15.
 */
public class ButtonItem extends Item implements Comparable<ButtonItem>{

    private final View.OnClickListener onClickListener;
    private String text;


    public ButtonItem(String text, View.OnClickListener onClickListener){
    this.text=text;
        this.onClickListener=onClickListener;
    }
      @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ButtonItem that = (ButtonItem) o;

        return !(text != null ? !text.equals(that.text) : that.text != null);

    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

    @Override
    public String getDisplayName() {
        return text;
    }

    public void onClick(View v){
        onClickListener.onClick(v);
    }
    @Override
    public String getThumbnail() {
        return null;
    }
    @Override
    public String getPicture() {
        return null;
    }

    @Override
    public int compareTo(ButtonItem albumItem) {
        return getDisplayName().compareTo(albumItem.getDisplayName());
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }
}
