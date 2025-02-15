package com.ds.ims.api.service;

import com.ds.ims.api.dto.InternshipDto;
import com.ds.ims.api.mapper.InternshipMapper;
import com.ds.ims.storage.entity.InternshipEntity;
import com.ds.ims.storage.entity.InternshipUserEntity;
import com.ds.ims.storage.entity.UserEntity;
import com.ds.ims.storage.entity.status.InternshipUserStatus;
import com.ds.ims.storage.repository.InternshipUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями стажировок
 */
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Service
public class InternshipUserService {
    GitlabService gitlabService;
    UserService userService;
    InternshipUserRepository internshipUserRepository;

    /**
     * Найти все по пользователю и статусу
     *
     * @param user                 - пользователь
     * @param internshipUserStatus - статус
     * @return список пользователей
     */
    public List<InternshipUserEntity> findByUserAndStatus(UserEntity user, InternshipUserStatus internshipUserStatus) {
        return internshipUserRepository.findByUserAndStatus(user, internshipUserStatus);
    }

    /**
     * Найти все по id пользователя и стажировки
     *
     * @param userId       - id пользователя
     * @param internshipId - id стажировки
     * @return пользователь стажировки
     */
    public Optional<InternshipUserEntity> findByUserIdAndInternshipId(Long userId, Long internshipId) {
        return internshipUserRepository.findByUserIdAndInternshipId(userId, internshipId);
    }

    /**
     * Найти все по id стажировки и статусу
     *
     * @param internshipId         - id стажировки
     * @param internshipUserStatus - статус
     * @return список пользователей
     */
    public List<InternshipUserEntity> findAllByInternshipIdAndStatus(Long internshipId, InternshipUserStatus internshipUserStatus) {
        return internshipUserRepository.findAllByInternshipIdAndStatus(internshipId, internshipUserStatus);
    }

    /**
     * Создать пользователя стажировки
     *
     * @param internship - стажировка
     * @param user       - пользователь
     */
    public void createInternshipUser(InternshipEntity internship, UserEntity user) {
        if (findByUserIdAndInternshipId(user.getId(), internship.getId()).isPresent()) {
            throw new BadRequestException("User already registered to this internship");
        }
        InternshipUserEntity internshipUserEntity = new InternshipUserEntity();
        internshipUserEntity.setUser(user);
        internshipUserEntity.setInternship(internship);
        internshipUserEntity.setStatus(InternshipUserStatus.ACTIVE);
        internshipUserEntity.setEntryDate(LocalDateTime.now());
        gitlabService.createUser(user);
        internshipUserRepository.save(internshipUserEntity);
    }

    /**
     * Получить стажировки пользователя
     *
     * @param accountId - id аккаунта
     * @return список стажировок
     */
    public List<InternshipDto> getInternshipsForUser(Long accountId) {
        UserEntity user = userService.findByAccountId(accountId).orElseThrow(() -> new NotFoundException("User not registered to any internship"));
        System.out.println("User: " + user.getId() + " " + user.getAccount().getId());
        List<InternshipUserEntity> internshipUserEntities = findByUserAndStatus(user, InternshipUserStatus.ACTIVE);
        if (internshipUserEntities.isEmpty()) {
            throw new NotFoundException("User not registered to any internship");
        }
        List<InternshipDto> internships = new ArrayList<>();
        for (InternshipUserEntity internshipUserEntity : internshipUserEntities) {
            internships.add(InternshipMapper.INSTANCE.toDto(internshipUserEntity.getInternship()));
        }
        return internships;
    }

    /**
     * Проверить пользователя на участие в стажировке
     *
     * @param internshipId - id стажировки
     * @param accountId    - id аккаунта
     */
    public void checkUserForMembership(Long internshipId, Long accountId) {
        Long userId = userService.findByAccountId(accountId).orElseThrow(() -> new NotFoundException("User not found")).getId();
        Optional<InternshipUserEntity> internshipUserEntity = findByUserIdAndInternshipId(userId, internshipId);
        if (!internshipUserEntity.isPresent()) {
            throw new ForbiddenException("User don't have access to this internship");
        }
    }

    /**
     * Обновить статус
     *
     * @param internshipUserEntity - пользователь стажировки
     * @param status               - статус
     */
    public void updateStatus(InternshipUserEntity internshipUserEntity, InternshipUserStatus status) {
        internshipUserEntity.setStatus(status);
        internshipUserRepository.save(internshipUserEntity);
        ResponseEntity.ok().build();
    }

    /**
     * Обновить статус
     *
     * @param internshipId - id стажировки
     * @param userId       - id пользователя
     * @param status       - статус
     * @return ответ
     */
    public ResponseEntity<?> updateStatus(Long internshipId, Long userId, InternshipUserStatus status) {
        InternshipUserEntity internshipUserEntity = findByUserIdAndInternshipId(userId, internshipId)
                .orElseThrow(() -> new NotFoundException("User not registered to this internship"));
        internshipUserEntity.setStatus(status);
        internshipUserRepository.save(internshipUserEntity);
        return ResponseEntity.ok().build();
    }
}
