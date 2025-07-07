package com.kh.spring.member.model.vo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// lombok 어노테이션들. 해당 클래스 파일에만 추가됨. 기본생성자 여러군데에서 많이쓰여서 꼭 쓰자
//@Setter @Getter @NoArgsConstructor @AllArgsConstructor @ToString @EqualsAndHashCode

@NoArgsConstructor // 커맨드 객체로 생성하려면 얘랑 Data 어노테이션만 있으면 됨
@AllArgsConstructor
//@RequiredArgsConstructor // final 예약어가 붙은 생성자에 대해서만... 뭐라구요?
// final 예약어가 있으면 기본생성자 호출을 못해서 컴파일에러남. 무조건 초기화 시켜줘야되니까
@Data
@Builder // 필수는 아님
public class Member {
	// JPA 쓰면 entity 예약어를 통해 자바클래스로 데이터베이스 관리할 수 있음
	// 자바클래스로 쿼리문 조회, 테이블 생성같은거 가능. 개발방법론이 아예 다름
	private int userNo;
	private String userId;
	private String userPwd;
	private String userName;
	private String profileImg; // BLOB 형태가 아니라 정적파일 경로보관
	private String email;
    private String birthday;
    private String gender;
    private String phone;
    private String address;
    private Date enrollDate;
    private Date modifyDate;
    private String status;
}
