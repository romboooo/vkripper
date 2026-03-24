package org.example.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteRequest {

    private Long userId;

    private Long productId;

}
