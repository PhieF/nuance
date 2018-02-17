package com.spipi.spipimediaplayer;

/**
 * Created by alexandre on 26/05/15.
 */
public class TextItem extends Item implements Comparable<TextItem>{

    private String text;


    public TextItem( String text){
    this.text=text;
    }
      @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextItem that = (TextItem) o;

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

    @Override
    public String getThumbnail() {
        return null;
    }
    @Override
    public String getPicture() {
        return null;
    }

    @Override
    public int compareTo(TextItem albumItem) {
        return getDisplayName().compareTo(albumItem.getDisplayName());
    }

}
