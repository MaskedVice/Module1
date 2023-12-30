package com.scripfinder.module1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseObject<T> {
    private Boolean responseType;
    private T responseResult;
    private Integer responseCount;
    private Integer responseCode;
    
}
