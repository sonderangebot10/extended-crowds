package util;

/**
 * Created by johan_dp8ahsz on 10/5/2017.
 */

public class ListItem {

    private String name;
    private boolean selected;

    public ListItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setItemName(String name) {
        this.name = name;
    }

    public boolean getSelected() {
        return selected;
    }

    public boolean setSelected(Boolean selected) {
        return this.selected = selected;
    }
}
