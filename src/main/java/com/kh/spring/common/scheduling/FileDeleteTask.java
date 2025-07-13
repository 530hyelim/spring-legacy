package com.kh.spring.common.scheduling;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.BoardImg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDeleteTask {
	/*
     * 파일 삭제 스케쥴러
     *  - 목표 : DB에는 존재하지 않으나 WEB-SERVER상에 존재하는 쓸모없는 파일을 삭제.
     * 업무로직
     * 1. 데이터베이스(board_img 테이블)에 등록된 모든 이미지 파일 경로 목록을 조회
     * 2. 모든 게시판 유형(boardType)을 조회하여, 각각의 게시판 디렉토리 경로를 탐색
     * 3. 해당 디렉토리에서 실제 서버에 존재하는 이미지 파일 목록 을 수집
     * 4. 각 파일이 DB에 등록되어 있는 파일인지 여부를 판단
     * 5. DB에 없는 파일(즉, 더 이상 사용되지 않는 파일)이라면 삭제 처리
     * 6. 유저활동량이 적은 매달 1일 4시에 실행되도록 설정
     */
	private final BoardService boardService;
	private final ServletContext application; 
	
//	@Scheduled(cron = "0 0 4 1 * ?")
	@Scheduled(fixedRate = 5000)
	public void fileDelete() {
		Map<String, String> boardTypeMap = boardService.getBoardTypeMap();
		for(String boardCd : boardTypeMap.keySet()) {
			String realPath = application.getRealPath("/resources/images/board/"+boardCd);
			File file = new File(realPath);
			
			if (file.exists()) {
				File[] images = file.listFiles();
				if (images != null) {
					for (File img : images) {
						BoardImg bi = boardService.getBoardImg("/resources/images/board/"+boardCd+"/"+img.getName());
						if (bi == null) {
							img.delete();
						}
					}
				}
			}
		}
	}
}
