package com.qvim.hs.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by RINES on 23.04.17.
 */
@Controller
@CrossOrigin
public class SiteController {

    @RequestMapping(value="/tool", produces = {MediaType.TEXT_HTML_VALUE})
    public String getToolPage() {
        return "tool";
    }

    @RequestMapping(value="/papi", produces = {MediaType.TEXT_HTML_VALUE})
    public String getPapiWithoutSlash(Model model) {
        return PublicApiController.getMainPageStatic(model);
    }

    @RequestMapping(value="/about", produces = {MediaType.TEXT_HTML_VALUE})
    public String getAboutPage() {
        return "about";
    }

}
