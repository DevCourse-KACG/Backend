package com.back.domain.club.club.service;

import com.back.domain.club.club.dtos.ClubControllerDtos;
import com.back.domain.club.club.entity.Club;
import com.back.domain.club.club.repository.ClubRepository;
import com.back.domain.club.clubMember.entity.ClubMember;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.aws.S3Service;
import com.back.global.enums.ClubCategory;
import com.back.global.enums.ClubMemberRole;
import com.back.global.enums.ClubMemberState;
import com.back.global.enums.EventType;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final MemberService memberService;
    private final S3Service s3Service;

    /**
     * 마지막으로 생성된 클럽을 반환합니다.
     * @return 마지막으로 생성된 클럽
     */
    public Club getLastCreatedClub() {
        return clubRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("마지막으로 생성된 클럽이 없습니다."));
    }

    /**
     * 클럽 ID로 클럽을 조회합니다.
     * @param clubId 클럽 ID
     * @return 클럽 정보
     */
    public Optional<Club> getClubById(Long clubId) {
        return clubRepository.findById(clubId);
    }

    /**
     * 클럽을 생성합니다.
     * @param club 클럽 정보
     * @return 생성된 클럽
     */
    @Transactional
    public Club createClub(Club club) {
        return clubRepository.save(club);
    }

    @Transactional
    public Club createClub(
            ClubControllerDtos.CreateClubRequest reqBody,
            MultipartFile image
    ) throws IOException {


        // 1. 이미지 없이 클럽 생성
        Club club = clubRepository.saveAndFlush(
                Club.builder()
                .name(reqBody.name())
                .bio(reqBody.bio())
                .category(ClubCategory.fromString(reqBody.category().toUpperCase()))
                .mainSpot(reqBody.mainSpot())
                .maximumCapacity(reqBody.maximumCapacity())
                .eventType(EventType.fromString(reqBody.eventType().toUpperCase()))
                .startDate(LocalDate.parse(reqBody.startDate()))
                .endDate(LocalDate.parse(reqBody.endDate()))
                .isPublic(reqBody.isPublic())
                .leaderId(reqBody.leaderId())
                .build()
        );
        // 2. 이미지가 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()){
            String imageUrl = s3Service.upload(image, "club/" + club.getId() + "/profile");
            club.updateImageUrl(imageUrl); // 클럽에 이미지 URL 설정
            clubRepository.save(club); // 이미지 URL 업데이트 후 클럽 정보 저장
        }


        // 클럽 멤버 설정
        Arrays.stream(reqBody.clubMembers()).forEach(memberInfo -> {
            // 멤버 ID로 Member 엔티티 조회
//          Member member = memberService.findById(memberInfo.id())
//              .orElseThrow(() -> new NoSuchElementException("ID " + memberInfo.id() + "에 해당하는 멤버를 찾을 수 없습니다."));

            Member member = new Member(); // TODO : 임시로 Member 객체 생성, 실제로는 memberService.findById(memberInfo.id())를 사용해야 함

            // ClubMember 엔티티 생성
            ClubMember clubMember = ClubMember.builder()
                    .member(member)
                    .role(ClubMemberRole.fromString(memberInfo.role().toUpperCase())) // 문자열을 Enum으로 변환
                    .state(ClubMemberState.INVITED) // 초기 상태는 INVITED로 설정
                    .build();

            // 연관관계 편의 메서드를 사용하여 Club에 ClubMember 추가
            club.addClubMember(clubMember);
        });

        return club;
    }

    /**
     * 클럽 정보를 업데이트합니다.
     * @param clubId 클럽 ID
     * @param dto 클럽 정보 업데이트 요청 DTO
     * @param image 클럽 이미지 파일 (선택적)
     * @return 업데이트된 클럽 정보
     * @throws IOException 이미지 업로드 중 발생할 수 있는 예외
     */
    @Transactional
    public Club updateClub (Long clubId, ClubControllerDtos.@Valid UpdateClubRequest dto, MultipartFile image) throws IOException {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽을 찾을 수 없습니다."));

        // 클럽 정보 업데이트
        String name = dto.name() != null ? dto.name() : club.getName();
        String bio = dto.bio() != null ? dto.bio() : club.getBio();
        ClubCategory category = dto.category() != null ? ClubCategory.fromString(dto.category().toUpperCase()) : club.getCategory();
        String mainSpot = dto.mainSpot() != null ? dto.mainSpot() : club.getMainSpot();
        int maximumCapacity = dto.maximumCapacity() != null ? dto.maximumCapacity() : club.getMaximumCapacity();
        boolean recruitingStatus = dto.recruitingStatus() != null ? dto.recruitingStatus() : club.isRecruitingStatus();
        EventType eventType = dto.eventType() != null ? EventType.fromString(dto.eventType().toUpperCase()) : club.getEventType();
        LocalDate startDate = dto.startDate() != null ? LocalDate.parse(dto.startDate()) : club.getStartDate();
        LocalDate endDate = dto.endDate() != null ? LocalDate.parse(dto.endDate()) : club.getEndDate();
        boolean isPublic = dto.isPublic() != null ? dto.isPublic() : club.isPublic();
        long leaderId = dto.leaderId() != null ? dto.leaderId() : club.getLeaderId();

        club.updateInfo(name, bio, category, mainSpot, maximumCapacity, recruitingStatus, eventType, startDate, endDate, isPublic, leaderId);

        // 이미지가 제공된 경우 S3에 업로드
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.upload(image, "club/" + club.getId() + "/profile");
            club.updateImageUrl(imageUrl); // 클럽에 이미지 URL 설정
        }


        return clubRepository.save(club);
    }

    public void deleteClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽을 찾을 수 없습니다."));

        // 클럽 삭제
        club.changeState(false); // 클럽 상태를 비활성화로 변경
    }

    /**
     * 클럽 정보를 조회합니다.
     * @param clubId 클럽 ID
     * @return 클럽 정보 DTO
     */
    @Transactional(readOnly = true)
    public ClubControllerDtos.ClubInfoResponse getClubInfo(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽을 찾을 수 없습니다."));

        Member leader = memberService.findById(club.getLeaderId())
                .orElseThrow(() -> new ServiceException(404, "해당 ID의 클럽 리더를 찾을 수 없습니다."));


        return new ClubControllerDtos.ClubInfoResponse(
                club.getId(),
                club.getName(),
                club.getBio(),
                club.getCategory().toString(),
                club.getMainSpot(),
                club.getMaximumCapacity(),
                club.isRecruitingStatus(),
                club.getEventType().toString(),
                club.getStartDate().toString(),
                club.getEndDate().toString(),
                club.isPublic(),
                club.getImageUrl(),
                club.getLeaderId(),
                leader.getNickname()
        );
    }

    @Transactional(readOnly = true)
    public Page<ClubControllerDtos.SimpleClubInfoResponse> getPublicClubs(Pageable pageable) {
        return clubRepository.findAllByIsPublicTrue(pageable)
                .map(club -> new ClubControllerDtos.SimpleClubInfoResponse(
                        club.getId(),
                        club.getName(),
                        club.getCategory().toString(),
                        club.getImageUrl(),
                        club.getMainSpot(),
                        club.getEventType().toString(),
                        club.getStartDate().toString(),
                        club.getEndDate().toString(),
                        club.getLeaderId(),
                        memberService.findById(club.getLeaderId())
                                .map(Member::getNickname)
                                .orElse("Unknown Leader")
                ));
    }
}
