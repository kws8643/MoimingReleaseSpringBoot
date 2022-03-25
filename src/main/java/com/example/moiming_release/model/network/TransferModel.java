package com.example.moiming_release.model.network;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferModel<T> { // TransferModel 자체가 Header + Data 통신 그 자체

    @JsonProperty("transaction_time")
    private LocalDateTime transactionTime; // 통신 시간

    private String authentication; // 토큰들이 오가는 항목.

    @JsonProperty("result_code")
    private int resultCode; // 응답 코드 // 종류 별 응답 코드?

    private String description; // 응답 설명

    private T data; // Data 부

    // 응답 종류
    // 1. OK with No Data // update? 같은 응답?
    public static <T> TransferModel<T> OK() {

        return (TransferModel<T>) TransferModel.builder()
                .transactionTime(LocalDateTime.now())
                .resultCode(200)
                .description("OK")
                .build();

    }

    public static <T> TransferModel<T> OK(String token, T data){

        return (TransferModel<T>) TransferModel.builder()
                .transactionTime(LocalDateTime.now())
                .resultCode(200)
                .description("OK")
                .authentication(token)
                .data(data)
                .build();

    }

    // 2. OK with response Data
    public static <T> TransferModel<T> OK(T data) {

        return (TransferModel<T>) TransferModel.builder()
                .transactionTime(LocalDateTime.now())
                .resultCode(200)
                .description("OK")
                .data(data)
                .build();

    }

    // 2. OK with response Description
    public static <T> TransferModel<T> OK(T data, String description) {

        return (TransferModel<T>) TransferModel.builder()
                .transactionTime(LocalDateTime.now())
                .resultCode(200)
                .description(description)
                .data(data)
                .build();

    }


    // 3. Error. (Error Result Code & Description)
    public static <T> TransferModel<T> ERROR(int resultCode, String description){

        return (TransferModel<T>) TransferModel.builder()
                .transactionTime(LocalDateTime.now())
                .resultCode(resultCode)
                .description(description)
                .build();


    }

    public String toString(){

        String jsonType = "{\n"
                + "transaction_time:\"" + String.valueOf(transactionTime) + "\""
                + "\nresultCode:" + String.valueOf(resultCode)
                + "\ndescription:\"" + description + "\""
                + "\ndata:\n"
                + data.toString()
                + "\n"
                + "}";

        return jsonType;

    }
}
