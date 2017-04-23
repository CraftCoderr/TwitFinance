package com.qvim.hs.web;

import com.qvim.hs.api.ApiError;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RINES on 23.04.17.
 */
@Controller
@CrossOrigin
@RequestMapping(value="/papi")
public class PublicApiController {

    private final Map<String, ApiMethod> methods = new HashMap<>();
    private static String methodsListCached;
    private static String errorsListCached;

    public PublicApiController() {
        new ApiMethod("currencies", "List of all currencies, the exchange rate of which is available for USD.",
                "{\n" +
                        "\t\"currencies\": [\n" +
                        "\t\t\"EUR\",\n" +
                        "\t\t\"GBP\"\n" +
                        "\t]\n" +
                        "}");
        new ApiMethod("retrieveData", "Interesting tweets & historically sorted exchange rates for given currencies in a given period of time.",
                "{\n" +
                        "\t\"news\": [\n" +
                        "\t\t{\n" +
                        "\t\t\t\"author\": \"BondVigilantes\",\n" +
                        "\t\t\t\"rating\": 100,\n" +
                        "\t\t\t\"text\": \"New post: Should the people of Middle Earth have done QE to mitigate against Smaug's tight monetary policy?\",\n" +
                        "\t\t\t\"url\": \"http://t.co/AAp7a4Sv\",\n" +
                        "\t\t\t\"time\": 1357129783000\n" +
                        "\t\t}\n" +
                        "\t],\n" +
                        "\t\"graph\": [\n" +
                        "\t\t{\n" +
                        "\t\t\t\"rate\": 1.3175229,\n" +
                        "\t\t\t\"time\": 1357128000000\n" +
                        "\t\t},\n" +
                        "\t\t{\n" +
                        "\t\t\t\"rate\": 1.3166873,\n" +
                        "\t\t\t\"time\": 1357131600000\n" +
                        "\t\t},\n" +
                        "\t\t{\n" +
                        "\t\t\t\"rate\": 1.3167449,\n" +
                        "\t\t\t\"time\": 1357135200000\n" +
                        "\t\t},\n" +
                        "\t\t{\n" +
                        "\t\t\t\"rate\": 1.3176948,\n" +
                        "\t\t\t\"time\": 1357138800000\n" +
                        "\t\t}\n" +
                        "\t]\n" +
                        "}",
                new Argument("target_currency", "EUR", true, "first currency"),
                new Argument("to_currency", "USD", false, "another currency (default value: USD)"),
                new Argument("start_date", "0", true, "the beginning of time interval in unix timestamp with millis format"),
                new Argument("end_date", "1358500800000", true, "the end of time interval in unix timestamp with millis format"),
                new Argument("scale", "0", false, "scale factor (deprecated)"),
                new Argument("people", "true", false, "do you need people or mass media tweets: true for people, false for mass media (default value: true)")
                );
        StringBuilder sb = new StringBuilder();
        sb.append("<h2 id=api class=\"small_bottom_margin\">Methods List</h2>\n" +
                "                        <table class=\"large_bottom_margin full_width\">\n" +
                "                            <tr>\n" +
                "                                <th>Method</th>\n" +
                "                                <th>Description</th>\n" +
                "                            </tr>");
        methods.values().forEach(m -> sb.append(
                        "<tr>\n" +
                        "   <td width=\"30%\"><a href=\"/papi/method/" + m.name + "\" class=\"bold block\">" + m.name + "</a></td>\n" +
                        "   <td width=\"70%\">" + m.description + "</td>\n" +
                        "</tr>\n"
        ));
        sb.append("</table>");
        this.methodsListCached = sb.toString();
        this.errorsListCached =  "<table class=\"arguments full_width\">\n" +
                "                    <tr>\n" +
                "                        <th>ID</th>\n" +
                "                        <th>Description</th>\n" +
                "                    </tr>" + addErrors(ApiError.values()) + "</table>";
    }

    private String addErrors(ApiError... errors) {
        StringBuilder sb = new StringBuilder();
        for(ApiError e : errors) {
            sb.append("<tr><td><code>" + (e.ordinal() + 1) + "</code></td>\n" +
                    "   <td><p>" + e.getDescription() + "</p></td>\n" +
                    "</tr>");
        }
        return sb.toString();
    }

    private String addArguments(Argument... as) {
        StringBuilder sb = new StringBuilder();
        for(Argument a : as) {
            sb.append("<tr><td ><code>" + a.name + "</code></td>\n" +
                    "   <td ><code>" + a.example + "</code></td>\n" +
                    "   <td >" + (a.required ? "Yes" : "No") + "</td>\n" +
                    "   <td ><p>" + a.description + "</p>\n" +
                    "   </td>\n" +
                    "</tr>");
        }
        return sb.toString();
    }

    @RequestMapping(value="/method/{name:.+}")
    public String handle(@PathVariable(required = false) String name, Model model) {
        if(name == null)
            name = "null";
        ApiMethod method = methods.get(name);
        if(method == null)
            return getMainPage(model);
        model.addAttribute("methodName", name);
        model.addAttribute("methodDescription", method.description);
        model.addAttribute("arguments", addArguments(method.arguments));
        model.addAttribute("resultExample", method.resultExample);
        return "papi_method";
    }

    @RequestMapping(value="/help")
    public String getGeneralHelp() {
        return "papi_base";
    }

    public static String getMainPageStatic(Model model) {
        model.addAttribute("methodsList", methodsListCached);
        model.addAttribute("errorsList", errorsListCached);
        return "papi";
    }

    @RequestMapping(value="/")
    public String getMainPage(Model model) {
        return getMainPageStatic(model);
    }

    private class ApiMethod {

        private final String name;
        private final String description;
        private final String resultExample;
        private final Argument[] arguments;

        public ApiMethod(String name, String description, String resultExample, Argument... arguments) {
            this.name = name;
            this.description = description;
            this.resultExample = resultExample;
            this.arguments = arguments;
            methods.put(name, this);
        }

    }

    @Data
    private class Argument {

        private final String name;
        private final String example;
        private final boolean required;
        private final String description;

    }

}
