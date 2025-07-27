package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.mainpage.MainpageResponse;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.user.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional(readOnly =true)
public class MainpageService {
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;

    public MainpageResponse getMainpage(User currentUser) {
        Long selectedBabyId = currentUser.getSelectedBabyId();
        if(selectedBabyId==null)
            throw new IllegalArgumentException("선택된 아이가 없습니다.");

        Baby baby = babyRepository.findByIdAndUserId(
                        currentUser.getSelectedBabyId(),
                        currentUser.getId()
                )
                .orElseThrow(()-> new IllegalArgumentException("해당 아이를 찾을 수 없습니다."));

        return MainpageResponse.from(baby);
    }
}
