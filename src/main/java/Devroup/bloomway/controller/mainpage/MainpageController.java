package Devroup.bloomway.controller.mainpage;

import Devroup.bloomway.dto.mainpage.MainpageResponse;
import Devroup.bloomway.entity.User;
import Devroup.bloomway.service.MainpageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mainpage")
public class MainpageController {

    private final MainpageService mainpageService;

    @GetMapping("/{userId}/{selectedBabyId}")
    public ResponseEntity<MainpageResponse> getMainpage(
            // swagger 테스트
            @PathVariable Long userId,
            @PathVariable Long selectedBabyId
            // @AuthenticationPrincipal User currentUser
    ) {
        // swagger 테스트
        User testUser = new User();
        testUser.setId(userId);
        testUser.setSelectedBabyId(selectedBabyId);
        return ResponseEntity.ok(mainpageService.getMainpage(testUser));
        // return ResponseEntity.ok(mainpageService.getMainpage(currentUser));
    }


}
