package com.kh.spring.common.model.vo;

import lombok.Data;

@Data
public class PageInfo {
	private int listCount; // 총 게시글 갯수
	private int currentPage; // 현재 요청한 페이지
	private int pageLimit; // 페이지바 하단에서 보여줄 페이지 갯수 (1~10 리미트가 10)
	private int boardLimit; // 한페이지당 보여줄 게시글 갯수
	// 리미트는 하드코딩. 10개 20개씩보기 하면 동적으로 보드리밋 변경하는거
	
	private int maxPage;
	private int startPage;
	private int endPage;
}
