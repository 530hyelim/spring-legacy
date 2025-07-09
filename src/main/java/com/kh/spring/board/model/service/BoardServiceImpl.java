package com.kh.spring.board.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.board.model.dao.BoardDao;
import com.kh.spring.board.model.vo.Board;
import com.kh.spring.board.model.vo.BoardExt;
import com.kh.spring.board.model.vo.BoardImg;
import com.kh.spring.board.model.vo.BoardType;
import com.kh.spring.common.Utils;
import com.kh.spring.common.model.vo.PageInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

	private final BoardDao boardDao;

	@Override
	public Map<String, String> getBoardTypeMap() {
		return boardDao.getBoardTypeMap();
	}
	
	@Override
	public int selectListCount(Map<String, Object> paramMap) {
		return boardDao.selectListCount(paramMap);
	}

	@Override
	public List<Board> selectList(PageInfo pi, Map<String, Object> paramMap) {
		return boardDao.selectList(pi, paramMap);
	}

	@Override
	@Transactional(rollbackFor = {Exception.class}) // 선언적 트랜잭션 관리
	// 어떠한 종류의 에러든 반환된다면 롤백한다. 그 외 모두 커밋
	// 제시 안하면 런타임 에러만으로 한정돼서 sql 익셉션을 캐치 못 함. 상위 예외로 만들어둔 것
	/*
	 * @Transactional
	 *  - 선언적 트랜잭션 관리용 어노테이션
	 *  - 예외가 발생하면 무조건 rollback 처리한다.
	 *  - rollbackFor를 지정하지 않으면 RuntimeException에러가 발생한 경우만 rollback 한다.
	 */
	public int insertBoard(Board b, List<BoardImg> imgList) { // proxy에 의해 대신 수행되는 메서드 (AOP시간)
		/*
		 * 0. 게시글 데이터 전처리 (개행문자 처리 및 xss 공격 핸들링)
		 * 1. 연관게시글 번호 (refBno)추가를 위해 게시글 테이블에 데이터를 먼저 추가해야 함
		 *   (원래 첨부파일은 필수사항이 아니니까 일단 게시글을 먼저 추가하고 첨부파일이 있다면 첨부파일 추가)
		 * 2. 첨부파일 테이블에 데이터 추가
		 * 3. 첨부파일 및 테이블 등록 (1,2번 과정)실패 시 롤백(직접하는게 아니라 에러 반환)
		 */
		// 데이터 전처리
		//  - 게시글 내용 : XSS 핸들링 및 개행문자 처리
		//  - 게시글 제목 : XSS 핸들링
		// 설명같은거 할 때 <script> 같은거 쓰면 &lt;로 변환돼서 그냥 < 문자 그 자체로 해석
		// \r\n 개행문자를 <br>태그로 치환해서 데이터베이스에 저장해야 함
		// 개행문자 못쓰는건 아닌데 얼마나 많이쓰든 항상 하나로만 처리되니까
		b.setBoardContent(Utils.XSSHandling(b.getBoardContent()));
		b.setBoardContent(Utils.newLineHandling(b.getBoardContent()));
		b.setBoardTitle(Utils.XSSHandling(b.getBoardTitle()));
		
		// 게시글 저장
		//  - mybatis의 selectKey 기능을 이용하여 boardNo 값을 b 객체에 바인딩
		int result = boardDao.insertBoard(b);
		if (result == 0) {
			throw new RuntimeException("게시글 등록 실패");
		}
		// 첨부파일 등록
		//  - 전달받은 imgList가 비어있지 않은 경우 진행
		//  - 게시글 번호를 추가로 refBno 필드에 바인딩
		if (!imgList.isEmpty()) {
			for (BoardImg bi : imgList) {
				bi.setRefBno(b.getBoardNo());
			}
			int imgResult = boardDao.insertBoardImgList(imgList);
			// 반복문으로 insert 여러번 (효율적이지 못함) vs. 다중 insert문
			// 쿼리문을 파싱하고 준비해야하는 과정이 여러번 수행되어야 하기 때문에
			if (imgResult != imgList.size()) {
				throw new RuntimeException("첨부파일 등록 실패");
			}
		}
		return result;
	}

	@Override
	public BoardExt selectBoard(int boardNo) {
		return boardDao.selectBoard(boardNo);
	}

	@Override
	public int increaseCount(int boardNo) {
		// 하나밖에 업성서 트랜잭션 고나리x
		return boardDao.increaseCount(boardNo);
	}

	@Override
	public List<BoardImg> selectBoardImgList(int boardNo) {

		return null;
	}

	@Override
	public int updateBoard(Board board, String deleteList, MultipartFile upfile, List<MultipartFile> upfiles)
			throws Exception {

		return 0;
	}

	@Override
	public List<String> selectFileList() {

		return null;
	}

	@Override
	public List<BoardType> selectBoardTypeList() {

		return null;
	}
}
