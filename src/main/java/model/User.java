package model;

import java.util.List;
import java.util.ArrayList;

public class User {
    public int id;
    public int totalWrongAnswer;
    public int totalCorrectAnswer;
    public List<Word> answerWords = new ArrayList<>();
    public transient Word currentWord = null;

    public User(int id) {
        this.id = id;
    }

    public boolean isTranslating() {
        return currentWord != null;
    }



}
