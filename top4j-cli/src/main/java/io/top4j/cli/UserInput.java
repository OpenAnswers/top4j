package io.top4j.cli;

/**
 * Created by ryan on 02/02/16.
 */
public class UserInput {

    private volatile String text = "t";
    private volatile boolean isDigit = false;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isDigit() {
        return isDigit;
    }

    public void setIsDigit(boolean isDigit) {
        this.isDigit = isDigit;
    }
}
