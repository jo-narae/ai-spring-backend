package com.sesac.aibackend.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trip {

    private Long id;
    private String title;
    private String origin;
    private String destination;
}
