package net.devstudy.resume.controller;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final ProfileService profileService;

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestParam("newPassword") String newPassword) {
        Long currentId = SecurityUtil.getCurrentId();
        if (currentId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        profileService.updatePassword(currentId, newPassword);
        return ResponseEntity.noContent().build();
    }
}
