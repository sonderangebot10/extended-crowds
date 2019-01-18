package util;


public class Item {

    private String title;
    private String type;
    private String question;
    private String created;
    private String sensor;


    public Item(){
        this.title = null;
        this.type = null;
        this.question = null;
        this.created = null;
        this.sensor = null;
    }

    public Item(String title){
        this.title = title;
        this.type = null;
        this.question = null;
        this.created = null;
        this.sensor = null;
    }

    public Item(String title, String type) {
        this.title = title;
        this.type = type;
        this.question = null;
        this.created = null;
        this.sensor = null;
    }

    public Item(String question, String type, String created, String sensor) {

        this.question = question;
        this.created = created;
        this.sensor = sensor;

        this.type = type + ", Created: " + created;
        this.title = question;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getCreated() {
        return created;
    }

    public String getSensor() {
        return sensor;
    }

    public String getQuestion() {
        return question;
    }
}
