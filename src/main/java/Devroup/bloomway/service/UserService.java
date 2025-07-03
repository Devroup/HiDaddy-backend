package Devroup.bloomway.service;

import Devroup.bloomway.entity.User;
import Devroup.bloomway.repository.UserRepository;
import Devroup.bloomway.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String saveOrLoginUser(String name, String email, String phone, String partnerPhone, String login_type, String social_id) {
        // 소셜 ID로 기존 유저 찾기
        Optional<User> userOptional = userRepository.findAll().stream()
                .filter(u -> u.getSocial_id().equals(social_id) && u.getLogin_type().equals(login_type))
                .findFirst();

        User user = userOptional.orElseGet(() -> {
            User newUser = new User(name, email, phone, partnerPhone, login_type, social_id);
            return userRepository.save(newUser);
        });

        // JWT 발급
        return jwtUtil.createToken(user);
    }
}