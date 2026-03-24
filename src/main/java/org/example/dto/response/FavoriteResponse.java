package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Favorite;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private LocalDateTime addedAt;


    public static FavoriteResponse fromFavorite(Favorite favorite){
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getProduct().getId(),
                favorite.getUser().getId(),
                favorite.getAddedAt()
        );
    }


}
