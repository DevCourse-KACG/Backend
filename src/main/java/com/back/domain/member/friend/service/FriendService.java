package com.back.domain.member.friend.service;

import com.back.domain.member.friend.dto.FriendDelDto;
import com.back.domain.member.friend.dto.FriendDto;
import com.back.domain.member.friend.entity.Friend;
import com.back.domain.member.friend.entity.FriendStatus;
import com.back.domain.member.friend.repository.FriendRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;
import com.back.domain.member.member.repository.MemberInfoRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final FriendRepository friendRepository;

    /**
     * 친구 추가 요청을 처리하는 메서드
     * @param memberId
     * @param friendEmail
     * @return FriendDto
     */
    @Transactional
    public FriendDto addFriend(Long memberId, String friendEmail) {
        // 친구 요청을 보낼 회원과 받는 회원 조회
        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        MemberInfo responderInfo = memberInfoRepository.findByEmailWithMember(friendEmail)
                .orElseThrow(() -> new NoSuchElementException("친구 대상이 존재하지 않습니다."));
        Member responder = responderInfo.getMember();

        // 자기 자신을 친구로 추가하는 경우 예외 처리
        if (requester.equals(responder)) {
            throw new ServiceException(400, "자기 자신을 친구로 추가할 수 없습니다.");
        }

        // id 순
        Member lowerMember = memberId < responder.getId() ? requester : responder;
        Member higherMember = memberId < responder.getId() ? responder : requester;

        // 이미 친구인 경우 예외 처리
        friendRepository
                .findByMembers(requester, responder)
                .ifPresent(existingFriend -> {
                    String errMsg;
                    // 친구 관계 상태에 따라 에러 메시지
                    switch (existingFriend.getStatus()) {
                        case PENDING -> {
                            // 요청자 여부에 따라 에러 메시지
                            if (existingFriend.getRequestedBy().equals(requester)) {
                                errMsg = "이미 친구 요청을 보냈습니다. 상대방의 수락을 기다려주세요.";
                            } else {
                                errMsg = "이미 친구 요청을 받았습니다. 수락 또는 거절해주세요.";
                            }
                        }
                        case ACCEPTED -> errMsg = "이미 친구입니다.";
                        case REJECTED -> errMsg = "이전에 거절한 친구 요청입니다. 다시 요청할 수 없습니다.";
                        default -> errMsg = "처리할 수 없는 친구 관계 상태입니다.";
                    }
                    throw new ServiceException(409, errMsg);
                });

        // 친구 요청 생성
        Friend friend = Friend.builder()
                .requestedBy(requester)
                .member1(lowerMember)
                .member2(higherMember)
                .status(FriendStatus.PENDING)
                .build();

        // 친구 요청 저장
        friendRepository.save(friend);

        return new FriendDto(friend, responder, responderInfo);
    }

    /**
     * 친구 요청을 수락하는 메서드
     * @param memberId
     * @param friendId
     * @return FriendDto
     */
    @Transactional
    public FriendDto acceptFriend(Long memberId, Long friendId) {
        // 친구 요청을 받은 회원과 보낸 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        Member friendMember = memberRepository.findById(friendId)
                .orElseThrow(() -> new NoSuchElementException("친구 대상이 존재하지 않습니다."));

        Friend friend = friendRepository.findByMembers(member, friendMember)
                .orElseThrow(() -> new NoSuchElementException("친구 요청이 존재하지 않습니다."));

        // 받는이가 아닌 요청자가 친구 요청을 수락하는 경우 예외 처리
        if (member.equals(friend.getRequestedBy())) {
            throw new ServiceException(400, "요청한 사람이 친구 수락할 수 없습니다. 친구에게 요청 수락을 받으세요.");
        }

        // 친구 요청의 상태가 PENDING이 아닌 경우 예외 처리
        if (friend.getStatus() == FriendStatus.ACCEPTED) {
            throw new ServiceException(400, "이미 친구입니다.");
        }

        // 친구 요청 수락
        friend.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(friend);

        return new FriendDto(friend, friendMember, friendMember.getMemberInfo());
    }

    @Transactional
    public FriendDto rejectFriend(Long memberId, Long friendId) {
        // 친구 요청을 받은 회원과 보낸 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        Member friendMember = memberRepository.findById(friendId)
                .orElseThrow(() -> new NoSuchElementException("친구 대상이 존재하지 않습니다."));

        Friend friend = friendRepository.findByMembers(member, friendMember)
                .orElseThrow(() -> new NoSuchElementException("친구 요청이 존재하지 않습니다."));

        // 받는이가 아닌 요청자가 친구 요청을 거절하는 경우 예외 처리
        if (member.equals(friend.getRequestedBy())) {
            throw new ServiceException(400, "요청한 사람이 친구 요청을 거절할 수 없습니다. 친구의 요청 수락/거절을 기다리세요.");
        }

        // 이미 친구인 경우 예외 처리
        if (friend.getStatus() == FriendStatus.ACCEPTED) {
            throw new ServiceException(400, "이미 친구입니다. 친구 삭제를 이용해 주세요.");
        }

        // 친구 요청 거절
        friend.setStatus(FriendStatus.REJECTED);
        friendRepository.save(friend);

        return new FriendDto(friend, friendMember, friendMember.getMemberInfo());
    }

    /**
     * 친구 삭제를 처리하는 메서드
     * @param memberId
     * @param friendId
     * @return FriendDelDto
     */
    @Transactional
    public FriendDelDto deleteFriend(Long memberId, Long friendId) {
        // 친구 요청을 보낸 회원과 받는 회원 조회
        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        Member friendMember = memberRepository.findById(friendId)
                .orElseThrow(() -> new NoSuchElementException("친구 대상이 존재하지 않습니다."));

        Friend friend = friendRepository.findByMembers(requester, friendMember)
                .orElseThrow(() -> new NoSuchElementException("친구 요청이 존재하지 않습니다."));

        // 친구 요청의 상태가 ACCEPTED가 아닌 경우 예외 처리
        if (friend.getStatus() != FriendStatus.ACCEPTED) {
            throw new ServiceException(400, "친구 요청이 수락되지 않았습니다.");
        }

        // 친구 삭제
        friendRepository.delete(friend);

        return new FriendDelDto(friendMember);
    }
}
