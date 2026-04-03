package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Role;
import org.example.entity.User;
import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResponse {
    private Long id;
    private String username;
    private BigDecimal balance;
    private List<FavoriteResponse> favorites;

    public static UserResponse fromUser(User user){
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getBalance(),
                user.getFavorites().stream().map(FavoriteResponse::fromFavorite).toList()
        );
    }
}
