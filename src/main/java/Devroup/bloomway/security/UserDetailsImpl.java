package Devroup.bloomway.security;

import Devroup.bloomway.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // 우리는 권한 시스템 사용 안 함
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인이므로 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return user.getSocialId(); // 인증 기준은 socialId
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
