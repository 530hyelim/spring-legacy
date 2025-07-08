package com.kh.spring.board.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.Board;
import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.common.template.Pagination;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor // final 예약어 붙은 애들만 매개변수로. 의존성 자동으로 주입 됨
/*
 * 왜 final필드와 @NonNull이 붙은 필드만 인자로 받을까?
 * 한 번 초기화하면 변경할 수 없는 필드를 의미 => 반드시 값을 초기화해야 하는 필드
 * RequiredArgsConstructor는 이런 '초기화가 필수인 필드'에 대해 생성자를 만들어주는 어노테이션
 * 일반 non-final 필드는 생성자에서 초기화할 의무가 없어서 "필수값"으로 보기 어려워서 포함안됨
 * @AllArgsConstructor를 쓰면 final이 아닌 필드도 포함해서 모든 필드를 파라미터로 받는 생성자가 생김
 * => 의존성 주입이 가능. 근데 이러면 필드가 많거나 나중에 추가되는 필드까지 모두 생성자 파라미터에 포함되므로
 * 불필요한 의존성까지 생성자에 포함될 수 있어서 final로 선언하는게 더 깔끔하고 안전한 설계
 */
@RequestMapping("/board") // 메서드 단위로 썼던걸 컨트롤러 레벨에서 "공용주소 설정"이 가능
@Slf4j // 로깅 프레임워크
public class BoardController {

	private final BoardService boardService;
	private final ServletContext application; 
	// ServletContext : application scope를 가진 서블릿 전역에서 사용가능한 객체
	// 테이블 형태로 저장된 여러 게시판들을 어플리케이션 스코프에 저장해서 header.jsp에 출력하기 위함
	private final ResourceLoader resourceLoader;
	/*
	 * ResourceLoader
	 *  - 스프링에서 제공하는 자원 로딩 클래스
	 *  - classpath, file시스템, url 등 다양한 경로의 자원을 동일한 인터페이스로 로드하는 메서드를 제공
	 *   자원들이 로컬상, classpath, url 에 존재한다면 경로가 달라서 로딩하는 방법이 다 다름
	 *   => 전부 추상화시켜서 동일한 방법으로 다운로드 할 수 있게 함
	 */
	// BoardType 전역객체 설정
	//  - 어플리케이션 전역에서 사용할 수 있는 BoardType 객체를 추가
	//  - 서버 가동중에 1회 수행되어 application에 자동으로 등록
	@PostConstruct // pom.xml에 의존성 추가 필요
	public void init() {
		// key=value, BOARD_CODE=BoardType객체 (resultType으로 얻어온 데이터 전체)
		// N=일반게시판, P=사진게시판
		Map<String, String> boardTypeMap = boardService.getBoardTypeMap();
		application.setAttribute("boardTypeMap", boardTypeMap);
		log.info("boardTypeMap : {}", boardTypeMap);
	}
	
	// 게시판 목록보기 서비스
	// 일반게시판, 사진게시판, 롤게시판 등등 모든 목록보기 페이지를 하나의 메서드에서 경로를 매핑하는 방법
	// 1. GetMapping의 속성값을 "문자열 배열" 형태로 관리
	//@GetMapping({"/list/N", "/list/P"}) // 비권장
	// 2. Rest한 방식으로 url을 작성하는 경우 mapping해줘야 하는게 메서드 하나당 여러개가 될 수 있음
	// /detail/N/1 게시글이 만개 있으면 매핑도 만개 시켜줘야함
	// => GetMapping에 동적 경로 변수를 사용
	@GetMapping("/list/{boardCode}") // boardCOde : 자원경로 변수
	// - {boardCode}는 N, P, C, L, E 등 동적으로 바뀌는 모든 보드코드 값을 저장할 수 있다.
	// - 선언한 동적 경로 변수는 @PathVariable 어노테이션으로 추출하여 사용할 수 있따.
	// - @PathVariable로 자원경로 추출 시, 추출한 변수는 model 영역(request scope)에 자동으로 추가된다.
	public String selectList(
			@PathVariable("boardCode") String boardCode,
			// 페이징 처리 : 한 페이지의 몇 개의 데이터를 보여줄건지 정의하는 기능
			// argument resovler를 사용하지 않고
			// requestParam을 통해 수동으로 바인딩 하는 이유 : 디폴트 밸류가 필요하기 때문
			// 아무것도 요청하지 않으면 처음들어갔을때 page값 전달하지 않고 기본값 1로 첫페이지 보여줌
			@RequestParam(value="currentPage", defaultValue="1") int currentPage,
			// currentPage
			//  - 현재 요청한 페이지 번호 (페이징 처리에 필요한 변수)
			//  - 기본값은 1로 처리하여, 값을 전달하지 않은 경우 항상 1페이지로 요청하게 처리
			@RequestParam Map<String,Object> paramMap, // 검색기능 추가를 위한 검색 키워드
			/*
			 * @RequestParam Map<String,Object>
			 *  - 스프링만의 고유한 작동방식 : 클라이언트가 요청시 전달한 파라미터의 key, value값을 Map 형태로
			 *    만들어서 RequestParamMethodArgumentResolver가 대신 대입해줌
			 *    - 기본적으로 쿼리 파라미터는 String 값으로 들어오기 때문에, 실제로는 Map<String, String>
			 *      으로 받는게 일반적. Object를 쓰면 스프링이 내부적으로 String 값을 Object에 넣는 식으로 동작
			 *      하고, 타입 변환 없이 문자열 그대로 들어갑니다
			 *  - 현재 메서드로 전달할 파라미터의 갯수가 정해져 있지 않은 경우, 일반적인 vo 클래스로 바인딩되지 않는
			 *    경우 사용한다 (ex. 검색 파라미터 등)
			 *  - 반드시 @RequestParam 어노테이션을 추가해야 바인딩해준다.
			 */
			Model model // = requestScope
			) {
		/*
		 * 업무로직
		 * 1. 페이징 처리
		 *   1) 현재 요청한 게시판 코드 및 검색정보와 일치하는 게시글의 총 갯수를 조회
		 *   2) 게시글 갯수, 페이지 번호, 기본 파라미터들을 추가하여 페이징정보 (PageInfo)객체를 생성
		 *     com.kh.spring.common.model.vo 패키지 안에 존재
		 * 2. 현재 요청한 게시판코드와 일치하면서, 현재 페이지에 해당하는 게시글 정보를 조회
		 * 3. 게시글 목록페이지로 게시글정보, 페이징정보, 검색정보를 담아서 forward
		 */
		
		// 1-1. 총 갯수 조회
		paramMap.put("boardCode", boardCode); // 검색조건 + 게시판 코드
		int listCount = boardService.selectListCount(paramMap);
		
		// 1-2. 기본파라미터 추가해서 페이징 정보 생성
		int pageLimit = 10;
		int boardLimit = 10;
		// 페이지 정보 템플릿을 이용하여 PageInfo 생성
		// com.kh.spring.common.template의 Pagination 클래스
		PageInfo pi = Pagination.getPageInfo(listCount, currentPage, pageLimit, boardLimit);
		
		// 2. 현재 요청한 페이지에 맞는 게시글 조회하기
		List<Board> list = boardService.selectList(pi, paramMap);
		
		// 3. 모델영역에 전달받은 정보 담아서 포워딩
		model.addAttribute("list", list);
		model.addAttribute("pi", pi);
		model.addAttribute("param", paramMap);
		return "board/boardListView";
	}
}
