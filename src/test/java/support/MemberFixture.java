package support;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberInfo;

import java.util.ArrayList;
import java.util.List;

public class MemberFixture {
    public static MemberInfo createMemberInfo() {
        return MemberInfo.builder()
                .email("test@example.com")
                .bio("소개입니다")
                .profileImageUrl(null)
                .build();
    }

    public static Member createMember(String nickname) {
        return Member.builder()
                .nickname(nickname)
                .password("password123")
                .memberInfo(createMemberInfo())
                .presets(null)
                .build();
    }

    public static List<Member> createMultipleMember(int count) {
        List<Member> memberInfoList = new ArrayList<>();
        for (int i = 0; i <= count; i++) {
            memberInfoList.add(createMember("테스트유저" + i));
        }

        return memberInfoList;
    }
}
