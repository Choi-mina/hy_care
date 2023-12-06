package com.example.hycare.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
    private String summary;
    private String symptom;
}
