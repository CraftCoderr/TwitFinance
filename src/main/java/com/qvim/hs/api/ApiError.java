package com.qvim.hs.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by RINES on 22.04.17.
 */
@RequiredArgsConstructor
public enum ApiError {

    UNKNOWN_METHOD("Method you have specified doesn't exist."),
    EMPTY_RESULT("Server returned empty answer to your request. Something is really wrong :/"),
    UNEXPECTED_ERROR("There is an unexpected error whilst trying to handle your request. Contact support, huh? Additional info: %s."),
    PARAMETER_NOT_SPECIFIED("Parameter %s is not specified in your query."),
    INVALID_PARAMETER("Parameter %s in your query is invalid."),
    INVALID_CURRENCY("Currency %s does not exist in our database."),
    NO_EXCHANGE_RATE_DATA("We don't have data for %s compared to %s at all."),
    NO_EXCHANGE_RATE_DATA_FOR_GIVEN_PERIOD("We don't have data for %s compared to %s for given period of time.");

    @Getter
    private final String description;

}
