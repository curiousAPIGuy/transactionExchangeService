package com.wex.dto;

import lombok.Data;
import java.util.List;

@Data
public class TreasuryApiResponse {
    private List<TreasuryRate> data;

    @Data
    public static class TreasuryRate {
        private String record_date;
        private String country_currency_desc;
        private String exchange_rate;
    }
}
