package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String userName;
    private String phone;
    private String partnerPhone;
    private String selectedBabyName;

    public UserResponse(User user, String selectedBabyName) {
        this.userName = user.getName();
        this.partnerPhone = user.getPartnerPhone();
        this.phone = user.getPhone();
        this.selectedBabyName = selectedBabyName;  // BabyRepository로 가져온 이름
    }
}