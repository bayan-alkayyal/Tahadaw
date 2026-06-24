package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Api.PremiumRequiredException;
import org.example.tahadaw.DTO.OUT.PremiumStatusDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.Payment;
import org.example.tahadaw.Model.PremiumAccess;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.PremiumAccessRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PremiumService {

    private final UserRepository userRepository;
    private final PremiumAccessRepository premiumAccessRepository;

    public PremiumStatusDTOOut getPremiumStatus(Long userId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            return ResponseMapper.toPremiumStatusDto(false, null);
        }

        return premiumAccessRepository.findByUser(user)
                .map(access -> ResponseMapper.toPremiumStatusDto(true, access.getActivatedAt()))
                .orElse(ResponseMapper.toPremiumStatusDto(true, null));
    }

    public void requirePremium(User user) {
        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            throw new PremiumRequiredException("Premium access is required for this feature.");
        }
    }

    @Transactional
    public void activatePremium(User user, Payment payment) {
        if (Boolean.TRUE.equals(user.getIsPremium()) && premiumAccessRepository.findByUser(user).isPresent()) {
            payment.setStatus("PAID");
            return;
        }

        user.setIsPremium(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        PremiumAccess access = new PremiumAccess();
        access.setUser(user);
        access.setPayment(payment);
        access.setActivatedAt(LocalDateTime.now());
        premiumAccessRepository.save(access);

        payment.setStatus("PAID");
    }
}
