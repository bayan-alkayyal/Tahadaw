package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;

    public void addRecipient(Long userId , Recipient recipient){

        User user = userRepository.findUserById(userId).orElse(null);

        if(user == null){
            throw new ApiException("User not found");
        }

        recipient.setUser(user);
        recipient.setCreatedAt(LocalDateTime.now());
        recipient.setUpdatedAt(LocalDateTime.now());

        recipientRepository.save(recipient);
    }

    public List<Recipient> getRecipients(){
        return recipientRepository.findAll();
    }

    public void updateRecipient(Long recipientId, Recipient recipient){

        Recipient oldRecipient = recipientRepository.findRecipientById(recipientId).orElse(null);

        if(oldRecipient == null){
            throw new ApiException("Recipient not found");
        }

        oldRecipient.setName(recipient.getName());
        oldRecipient.setRelationship(recipient.getRelationship());
        oldRecipient.setAge(recipient.getAge());
        oldRecipient.setGender(recipient.getGender());
        oldRecipient.setInterests(recipient.getInterests());
        oldRecipient.setHobbies(recipient.getHobbies());
        oldRecipient.setFavoriteColors(recipient.getFavoriteColors());
        oldRecipient.setFavoriteBrands(recipient.getFavoriteBrands());
        oldRecipient.setDislikes(recipient.getDislikes());
        oldRecipient.setPersonalityStyle(recipient.getPersonalityStyle());
        oldRecipient.setSizeInfo(recipient.getSizeInfo());
        oldRecipient.setNotes(recipient.getNotes());
        oldRecipient.setConsentAcknowledged(recipient.getConsentAcknowledged());

        oldRecipient.setUpdatedAt(LocalDateTime.now());

        recipientRepository.save(oldRecipient);
    }


    public void deleteRecipient(Long recipientId){

        Recipient recipient = recipientRepository.findRecipientById(recipientId).orElse(null);

        if(recipient == null){
            throw new ApiException("Recipient not found");
        }

        recipientRepository.delete(recipient);
    }
}
