package com.kh.spring.board.model.vo;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Board {
	private int boardNo;
	private String boardTitle;
	private String boardContent;
	private String boardCd; // char로 하면 mybatis가 자동으로 안해줌
	private String boardWriter; // userNo, userName값을 동시에 관리하기 위함
	// 게시글 저장할때는 userNo값으로, 조회해 올 떄에는 userName값으로 하려고
	private int count;
	private Date createDate;
	private String status;
}
