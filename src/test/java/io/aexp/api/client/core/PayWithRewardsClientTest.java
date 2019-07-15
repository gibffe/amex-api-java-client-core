package io.aexp.api.client.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.aexp.api.client.core.configuration.PropertiesConfigurationProvider;
import io.aexp.api.client.core.security.authentication.AuthProvider;
import io.aexp.api.client.core.security.authentication.HmacAuthBuilder;
import io.aexp.api.client.core.utils.JsonUtility;
import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class PayWithRewardsClientTest {

    String client_id = "Sf6WXs6GjkUSDly9UItj3GPRslAVsxCM";
    String client_secret = "OMJtRxsoTTJMicQWataT0PZF1ugpP7Ym";
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Test
    public void test() throws Exception {

        // prepare search message
        Card card = new Card();
        card.setNumber("375987654321001");

        SearchRewardsMessage message = new SearchRewardsMessage();
        message.setMerchantId("PWP_ONLINE");
        message.setCard(card);
        message.setTimestamp(timestamp());

        String payload = JsonUtility.getInstance().getString(message);

        // test client
        String response = searchRewards(payload, client_id, client_secret);
        System.out.println(response);
    }

    String timestamp() {
        return date_format.format(new Date());
    }

    // -- sample client body ---------------------------------------------------------------------------------------------------------------------
    //
    private static final String SEARCH_REWARDS_RESOURCE_URL = "https://api.qasb.americanexpress.com/loyalty/v1/accounts/rewards/search";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public String searchRewards(String searchPayload, String CLIENT_ID, String CLIENT_SECRET) throws IOException {
        Properties properties = new Properties();
        properties.put("CLIENT_KEY", CLIENT_ID);
        properties.put("CLIENT_SECRET", CLIENT_SECRET);
        properties.put("REWARDS_SEARCH_RESOURCE_URL", SEARCH_REWARDS_RESOURCE_URL);
        PropertiesConfigurationProvider configurationProvider = new PropertiesConfigurationProvider();
        configurationProvider.setProperties(properties);

        AuthProvider authProvider = HmacAuthBuilder.getBuilder()
                .setConfiguration(configurationProvider)
                .build();

        String url = configurationProvider.getValue("REWARDS_SEARCH_RESOURCE_URL");

        HttpUrl httpUrl = HttpUrl.parse(url);
        HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();

        // This generates the AMEX specific authentication headers needed for this API.
        Map<String, String> headers = authProvider.generateAuthHeaders(searchPayload, url, "POST");
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, searchPayload);
        Request.Builder builder = new Request.Builder()
                .url(httpUrlBuilder.build())
                .post(body);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        Request request = builder.build();

        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Response response = httpClient.newCall(request).execute();
        return response.body().string();
    }
    //
    // -- sample client body ---------------------------------------------------------------------------------------------------------------------

}

class SearchRewardsMessage implements Serializable {

    @JsonProperty("merchant_client_id")
    String merchantId;

    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("card")
    Card card;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}

class VerifyRewardsMessage extends SearchRewardsMessage {

    @JsonProperty("basket")
    Basket basket;

    public Basket getBasket() {
        return basket;
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
    }
}

class Basket implements Serializable {

    @JsonProperty("amount")
    int amount;

    @JsonProperty("reservation_id")
    String reservationId;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
}

class Card implements Serializable {

    @JsonProperty("number")
    String number;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}


