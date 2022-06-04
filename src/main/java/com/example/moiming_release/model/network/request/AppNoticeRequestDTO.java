package com.example.moiming_release.model.network.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppNoticeRequestDTO {

    @JsonProperty("notice_title")
    private String noticeTitle;

    @JsonProperty("notice_info")
    private String noticeInfo;

    @JsonProperty("is_url_linked")
    private Boolean isUrlLinked;

    @JsonProperty("notice_url")
    private String noticeUrl;

}
