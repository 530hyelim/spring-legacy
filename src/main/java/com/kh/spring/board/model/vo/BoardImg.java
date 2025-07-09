package com.kh.spring.board.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BoardImg {
	private int boardImgNo; // 시퀀스 자동증가 pk값
	private String originName; // 파일의 원본 이름 + 저장경로
	private String changeName; // 파일의 수정된 이름 (웹 서버상에 저장되어 있는 이름 - 중복때매)
	private int refBno; // 연관게시글번호 boardNo의 외래키
	private int imgLevel; // 사진게시판에서 사용됨. 0레벨 : 썸네일용, 123 : 내부 상세보기 이미지용
}
