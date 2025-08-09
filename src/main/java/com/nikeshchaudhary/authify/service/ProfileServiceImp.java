package com.nikeshchaudhary.authify.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.nikeshchaudhary.authify.entity.UserEntity;
import com.nikeshchaudhary.authify.io.ProfileRequest;
import com.nikeshchaudhary.authify.io.ProfileResponse;
import com.nikeshchaudhary.authify.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImp implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    public ProfileResponse createProfile(ProfileRequest profileRequest) {
        UserEntity newProfile = convertToUserEntity(profileRequest);
        if (!profileRepository.existsByEmail(profileRequest.getEmail())) {
            newProfile = profileRepository.save(newProfile);
            return convertToProFileResponse(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

    }

    private UserEntity convertToUserEntity(ProfileRequest profileRequest) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .name(profileRequest.getName())
                .email(profileRequest.getEmail())
                .password(profileRequest.getPassword())
                .build();
    }

    private ProfileResponse convertToProFileResponse(UserEntity userEntity) {
        return ProfileResponse.builder()
                .userId(userEntity.getUserId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .isAccountVerified(userEntity.isAccountVerified())
                .build();
    }

}
