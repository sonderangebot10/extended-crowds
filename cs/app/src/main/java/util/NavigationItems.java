package util;

public class NavigationItems {

    private String title, subtitle;
    private Integer icon;

    public NavigationItems(String title, String subtitle, Integer icon) {
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public Integer getIcon() {
        return icon;
    }

}