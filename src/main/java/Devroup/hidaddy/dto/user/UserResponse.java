package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String userName;
    private String phone;
    private String partnerPhone;
    private String profileImageUrl;
    private String email;

    public UserResponse(User user) {
        this.userName = user.getName();
        this.partnerPhone = user.getPartnerPhone();
        this.phone = user.getPhone();
        this.profileImageUrl = user.getProfileImageUrl();
        this.email = user.getEmail();  // BabyRepository로 가져온 이름
    }
}