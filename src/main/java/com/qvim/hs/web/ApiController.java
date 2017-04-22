package com.qvim.hs.web;

import com.qvim.hs.api.ApiError;
import com.qvim.hs.api.ApiRequest;
import com.qvim.hs.news.News;
import com.qvim.hs.news.NewsCache;
import com.qvim.hs.rates.Currency;
import com.qvim.hs.rates.IntegrityCache;
import com.qvim.hs.rates.TimedExchangeRate;
import com.qvim.hs.util.ScaleFactor;
import com.qvim.hs.util.TimestampUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by RINES on 22.04.17.
 */
@Controller
@CrossOrigin
@RequestMapping(value="/api")
public class ApiController {

    @RequestMapping(value="/", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public String getError() {
        ApiRequest request = new ApiRequest();
        request.error(ApiError.UNKNOWN_METHOD);
        return request.toJsonString();
    }

    @RequestMapping(value="/{method:.+}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public String getError(@PathVariable(required = false) String method) {
        return getError();
    }

    @RequestMapping(value="/retrieveData", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public String retrieveData(@RequestParam(name = "target_currency", required = false) String targetCurrency,
                              @RequestParam(name = "to_currency", required = false) String toCurrency,
                              @RequestParam(name = "start_date", required = false) Long startDate,
                              @RequestParam(name = "end_date", required = false) Long endDate,
                              @RequestParam(name = "scale", required = false) Integer scale,
                              @RequestParam(name = "people", required = false) Boolean people) {
        ApiRequest request = new ApiRequest();
        if(targetCurrency == null) {
            request.error(ApiError.PARAMETER_NOT_SPECIFIED, "target_currency");
            return request.toJsonString();
        }
        if(toCurrency == null) {
            request.error(ApiError.PARAMETER_NOT_SPECIFIED, "to_currency");
            return request.toJsonString();
        }
        if(startDate == null) {
            request.error(ApiError.PARAMETER_NOT_SPECIFIED, "start_date");
            return request.toJsonString();
        }
        if(endDate == null) {
            request.error(ApiError.PARAMETER_NOT_SPECIFIED, "end_date");
            return request.toJsonString();
        }
        if(scale == null) {
            request.error(ApiError.PARAMETER_NOT_SPECIFIED, "scale");
            return request.toJsonString();
        }
        if(people == null) {
            request.error(ApiError.PARAMETER_NOT_SPECIFIED, "people");
            return request.toJsonString();
        }
        if(scale < 0 || scale >= ScaleFactor.values().length) {
            request.error(ApiError.INVALID_PARAMETER, "scale");
            return request.toJsonString();
        }
        ScaleFactor scaleFactor = ScaleFactor.values()[scale];
        Currency target = getCurrency(targetCurrency);
        if(target == null) {
            request.error(ApiError.INVALID_CURRENCY, targetCurrency);
            return request.toJsonString();
        }
        Currency to = getCurrency(toCurrency);
        if(to == null) {
            request.error(ApiError.INVALID_CURRENCY, toCurrency);
            return request.toJsonString();
        }
        TimedExchangeRate[] graph = IntegrityCache.getExchangeRate(target, to);
        if(graph == null) {
            request.error(ApiError.NO_EXCHANGE_RATE_DATA, targetCurrency, toCurrency);
            return request.toJsonString();
        }
        graph = IntegrityCache.getExchangeRateTimedScaled(target, to, startDate, endDate, scaleFactor);
        if(graph == null) {
            request.error(ApiError.NO_EXCHANGE_RATE_DATA_FOR_GIVEN_PERIOD, targetCurrency, toCurrency);
            return request.toJsonString();
        }
        News[] news = NewsCache.getInterestingNewsTimed(startDate, endDate, people, graph, scaleFactor);
        if(news == null) {
            request.error(ApiError.UNEXPECTED_ERROR, "news can't be loaded");
            return request.toJsonString();
        }
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        for(TimedExchangeRate rate : graph) {
            long time = TimestampUtil.customTimestampToUnixtime(rate.getTimestamp());
            float value = rate.getRate();
            JSONObject point = new JSONObject();
            point.put("time", time);
            point.put("rate", value);
            array.add(point);
        }
        result.put("graph", array);
        array = new JSONArray();
        for(News n : news) {
            JSONObject json = new JSONObject();
            json.put("text", n.getText());
            json.put("author", n.getAuthor());
            if(n.getUrl() != null && !n.getUrl().isEmpty())
                json.put("url", n.getUrl());
            json.put("time", n.getDate());
            json.put("rating", n.getRating());
            array.add(json);
        }
        result.put("news", array);
        request.setResult(result);
        return request.toJsonString();
    }
    @RequestMapping(value="/currencies", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    public String currencies() {
        ApiRequest request = new ApiRequest();
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        for(Currency c : Currency.values())
            if(c != Currency.USD)
                array.add(c.name());
        result.put("currencies", array);
        request.setResult(result);
        return request.toJsonString();
    }

    private static Currency getCurrency(String name) {
        try {
            return Currency.valueOf(name.toUpperCase());
        }catch(Throwable t) {
            return null;
        }
    }

}
