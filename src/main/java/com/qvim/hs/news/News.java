package com.qvim.hs.news;

import lombok.Data;

/**
 * Created by RINES on 22.04.17.
 */
@Data
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
    private final int rating;

}
