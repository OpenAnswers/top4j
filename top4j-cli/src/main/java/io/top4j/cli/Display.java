package io.top4j.cli;

/**
 * Created by ryan on 02/02/16.
 */
public class Display {

    private volatile String text = "116";

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
