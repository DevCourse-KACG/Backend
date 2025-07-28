package com.back.domain.club.club.service;

import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;

    /**
     * 마지막으로 생성된 클럽을 반환합니다.
     * @return 마지막으로 생성된 클럽
     */
    public Club getLastCreatedClub() {
        return clubRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("마지막으로 생성된 클럽이 없습니다."));
    }
}
