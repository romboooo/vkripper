package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Favorite;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
public class FavoriteListResponse {
    private List<FavoriteResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static FavoriteListResponse fromPage(Page<Favorite> page){
        return new FavoriteListResponse(
                page.getContent().stream().map(FavoriteResponse::fromFavorite).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
