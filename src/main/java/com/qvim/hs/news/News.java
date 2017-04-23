package com.qvim.hs.news;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by RINES on 22.04.17.
 */
@Data
@AllArgsConstructor
public class News {

    private final String text;

    private final String author;

    /**
     * In millis.
     */
    private final long date;

    private final String url;

    /**
     * Рейтинг от 0 до 100.
     */
    private int rating;

    public News clone() {
        return new News(this.text, this.author, this.date, this.url, this.rating);
    }

}
