package com.record.integration.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReverseGeocodeResponse {
    private Integer status;
    private String message;
    private Result result;

    @Data
    public static class Result {
        private String address;
        @JsonProperty("address_component")
        private AddressComponent addressComponent;
    }

    @Data
    public static class AddressComponent {
        private String province;
        private String city;
        private String district;
    }
}

