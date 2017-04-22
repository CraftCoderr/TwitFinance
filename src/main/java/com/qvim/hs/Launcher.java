package com.qvim.hs;

import com.qvim.hs.web.ApiController;
import com.qvim.hs.web.SpringController;
import com.qvim.hs.web.WebConfiguration;
import org.springframework.boot.SpringApplication;

/**
 * Created by RINES on 21.04.17.
 */
public class Launcher {

    public static void main(String[] args) {
        new HackathonServer();
        Object[] controllers = new Object[]{
                SpringController.class, ApiController.class, WebConfiguration.class
        };
        SpringApplication.run(controllers, args);
    }

}
