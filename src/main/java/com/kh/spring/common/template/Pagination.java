package com.kh.spring.common.template;

import com.kh.spring.common.model.vo.PageInfo;

public class Pagination {
	
	public static PageInfo getPageInfo(int listCount, int currentPage, int pageLimit, int boardLimit) {
		PageInfo pi = new PageInfo();
		pi.setBoardLimit(boardLimit);
		pi.setPageLimit(pageLimit);
		pi.setListCount(listCount);
		pi.setCurrentPage(currentPage);
		// 1. 최대 페이지 갯수 (전체 게시글 갯수 / 한 페이지당 몇개? (boardLimit))
		int maxPage = (int)Math.ceil((double)listCount/boardLimit);
		// 2. 시작 페이지 (프론트에서 페이징 바 만들 때 사용)
		int startPage = (currentPage-1)/pageLimit*pageLimit+1;
		// 3. 종료 페이지
		int endPage = startPage+pageLimit-1;
		if (endPage > maxPage) {
			endPage = maxPage;
		}
		pi.setStartPage(startPage);
		pi.setEndPage(endPage);
		pi.setMaxPage(maxPage);
		return pi;
	}

}
