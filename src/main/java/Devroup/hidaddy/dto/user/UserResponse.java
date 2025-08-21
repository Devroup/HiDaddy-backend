package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long userId;
    private String userName;
    private String partnerPhone;
    private String profileImageUrl;
    private String email;

    public UserResponse(User user) {
        this.userId = user.getId();
        this.userName = user.getName();
        this.partnerPhone = user.getPartnerPhone();
        this.profileImageUrl = user.getProfileImageUrl();
        this.email = user.getEmail();  // BabyRepository로 가져온 이름
    }
}