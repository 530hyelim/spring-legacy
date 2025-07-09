package com.kh.spring.board.model.dao;


import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.spring.board.model.vo.Board;
import com.kh.spring.board.model.vo.BoardExt;
import com.kh.spring.board.model.vo.BoardImg;
import com.kh.spring.board.model.vo.BoardType;
import com.kh.spring.common.model.vo.PageInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j // field log 보이지않지만 생성됨
@Repository // 빈객체로 등록
@RequiredArgsConstructor
public class BoardDaoImpl implements BoardDao{
	
	private final SqlSessionTemplate session; // 생성자방식 의존성 주입

	// BoardType 전역객체 설정. BOARD_CODE=BoardType객체 (resultType으로 얻어온 데이터 전체)
	@Override
	public Map<String, String> getBoardTypeMap() {
		/*
		 * selectMap
		 *  - Map<Key,value> 형태의 데이터를 반환하는 메서드
		 *  - 두번째 매개변수로는 어떤 '컬럼'을 key로 사용할지를 작성
		 *  - SELECT key, value FROM TABLE
		 *  BOARD_CD인데 alias 설정해줄것. 항상 동일해야 함
		 *  key 값에 대해 getter 함수 호출하는데 BoardType 클래스 필드명이 boardCd이기 떄문
		 */
		return session.selectMap("board.getBoardTypeMap", "boardCd");
	}
	
	// 게시글의 총 갯수 조회 (검색 결과, 보드 타입에 따라서)
	@Override
	public int selectListCount(Map<String, Object> paramMap) {
		return session.selectOne("board.selectListCount", paramMap);
	}

	// 페이징 처리가 완료된 게시글 조회
	@Override
	public List<Board> selectList(PageInfo pi, Map<String, Object> paramMap) {
		/*
		 * 특정 페이지의 데이터를 가져오는 방법들(페이징 처리)
		 * 1. ROWNUM, ROW_NUMBER()으로 페이징 처리된 쿼리 조회하기
		 *  - SELECT * FROM (
		 *  		SELECT ROWNUM AS RNUM, INNER.*
		 *  		FROM (
		 *  			-- 특정 컬럼을 기준으로 조회된 쿼리
		 *  		) INNER
		 *  	)
		 *  	WHERE RNUM BETWEEN A AND B
		 *   - 쿼리문이 복잡하고, 코드의 가독성이 떨어지나 필요한 행만 조회하여 가져올 수 있기 때문에
		 *     메모리 낭비나 성능저하가 별로 없는 방식
		 *   - 1페이지 요청 시 -> WHERE RNUM BETWEEN 1 AND 10
		 *     2페이지 요청 시 -> WHERE RNUM BETWEEN 11 AND 20
		 * 
		 * 2. RowBounds를 활용한 방식 (마이바티스에서 페이징 처리 시 제공하는 객체)
		 *   - Mybatis에서 쿼리 결과에 대해 페이징 처리를 적용하는 도구
		 *   - 전체 쿼리결과를 자바 어플리케이션으로 가져온 후, 지정한 위치(offset)에서 특정 갯수
		 *     (limit)를 잘라내는 방식으로 페이징 처리를 진행한다
		 *   - 조회된 모든 행의 결과를 가져오기 때문에 어플리케이션으로 가져올 데이터가 수만건 이상인 경우 
		 *     메모리낭비 및 성능저하가 발생할 수 있다
		 *   - "소규모 데이터 쿼리"시 사용하는 것을 권장
		 *   
		 * 3. OFFSET과 FETCH 예약어를 사용하여 쿼리 조회 (Oracle 12c 이상만 사용가능)
		 *   - 코드의 복잡성을 줄이고 가독성을 크게 확보하는 방식
		 *   [표현법]
		 *   SELECT
		 *   	... 조회할 컬럼
		 *   FROM 테이블
		 *   ... 조건절
		 *   ORDER BY절
		 *   OFFSET 시작행 ROWS FETCH NEXT 조회할 갯수 ROWS ONLY
		 */
		// 몇 번째 행부터 가져올 지 (자바기준이라 인덱스처럼 사용돼서 0번부터 시작)
		int offset = (pi.getCurrentPage()-1)*pi.getBoardLimit();
		int limit = pi.getBoardLimit(); // offset 위치에서부터 몇 개의 행을 가져올지
		
		// 2번 방법으로 조회하기
//		RowBounds rowBounds = new RowBounds(offset, limit);
//		return session.selectList("board.selectList", paramMap, rowBounds);
		
		// 3번 방법으로 조회하기
		paramMap.put("offset", offset);
		paramMap.put("limit", limit);
		return session.selectList("board.selectList", paramMap);
	}

	@Override
	public List<BoardType> selectBoardTypeList() {
		return null;
	}

	@Override
	public int insertBoard(Board b) {
		log.debug("게시글 등록 이전 b : {}", b);
		int result = session.insert("board.insertBoard", b);
		log.debug("게시글 등록 이후 b : {}", b);
		return result;
	}

	@Override
	public int insertBoardImg(BoardImg bi) {
		return 0;
	}

	@Override
	public int insertBoardImgList(List<BoardImg> imgList) {
		return session.insert("board.insertBoardImgList", imgList);
	}

	@Override
	public BoardExt selectBoard(int boardNo) {
		return session.selectOne("board.selectBoard", boardNo);
	}

	@Override
	public int increaseCount(int boardNo) {
		return session.update("board.increaseCount", boardNo);
	}

	@Override
	public List<BoardImg> selectBoardImgList(int boardNo) {
		return null;
	}

	@Override
	public int updateBoard(Board board) {
		return 0;
	}

	@Override
	public int deleteBoardImg(String deleteList) {
		return 0;
	}

	@Override
	public int updateBoardImg(BoardImg bi) {
		return 0;
	}

	@Override
	public List<String> selectFileList() {
		return null;
	}
}
