package com.kh.spring.board.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.Board;
import com.kh.spring.board.model.vo.BoardExt;
import com.kh.spring.board.model.vo.BoardImg;
import com.kh.spring.common.Utils;
import com.kh.spring.common.model.vo.PageInfo;
import com.kh.spring.common.template.Pagination;
import com.kh.spring.member.model.vo.Member;

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
	//  - 서버 가동중에 생성자가 생성된 이후 (자바의 어노테이션) 1회 수행되어 application에 자동으로 등록
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
	// /detail/N/1 게시글이 만개 있으면 매핑도 만개 시켜줘야함 (Rest API를 비슷하게 모방한 구조)
	// 프론트와 백이 완벽하게 나뉘어져 있는 restful 구조가 아니기 때문
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
		 * 프론트에 페이지바 + 데이터베이스에서 조회할 두가지 목적
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
	
	// 게시판 등록 폼 이동 서비스
	@GetMapping("/insert/{boardCode}")
	public String enrollForm(
			@ModelAttribute Board b, 
			@PathVariable("boardCode") String boardCode,
			Model model
			) {
		// model attribute 없으면 에러남
		model.addAttribute("b", b);
		return "board/boardEnrollForm";
	}
	
	// 게시판 등록 기능
	@PostMapping("/insert/{boardCode}")
	public String insertBoard(
			@ModelAttribute Board b, // validate 생략. 테이블에 넣으려고 가져온 데이터
			// boardTitle이랑 boardContent 밖에 데이터가 없음
			// 추가하려면 다른 필드들에 대한 값들이 있어야 함 (boardCd, boardWriter)
			// 그래서 auth 얻어오는 것. principal 내부의 사용자 정보에서 꺼내올 예정
			@PathVariable("boardCode") String boardCode,
			Authentication auth,
			Model model,
			RedirectAttributes ra,
			/*
			 * RequestParam 이 알아서 new ArrayList 이런식으로 항상 객체 생성해줌
			 * List<MultipartFile>
			 *  - MultipartFile
			 *    - multipart/form-data 방식으로 전송된 파일데이터를 바인딩해주는 클래스
			 *    - 파일의 원본 이름, 크기, 실제로 존재하는지, 저장기능 등 다양한 메서드를 제공
			 *  - name 속성값이 upfile으로 전달되는 모든 파일 파람을 하나의 컬렉션으로 모아오기 위해 선언
			 *  - @RequestParam + List/Map 사용 시 바인딩할 데이터가 없더라도 항상 객체 자체는 생성된다
			 */
			@RequestParam(value="upfile", required=false) List<MultipartFile> upfiles
			) {
		/*
		 * 업무로직
		 * 1. 유효성 검사(생략) 욕설필터링 같은거. cross site scripting 공격 방어
		 * 2. 첨부파일이 존재하는지 확인
		 *   1) 존재한다면 첨부파일을 웹서버상에 저장하는 로직 필요
		 *   2) 존재하지 않는다면 3번 로직 수행
		 * 3. 게시판정보 등록 및 첨부파일정보 등록을 위한 서비스 호출
		 * 4. 게시글 및 첨부파일 등록 결과에 따른 뷰 페이지 지정
		 *   1) 성공 시 목록으로 리다이렉트
		 *   2) 실패 시 에러페이지로 포워딩 (직접 안하고 ControllerAdvice가 처리하게 할 것)
		 */
		// 첨부파일 존재여부 체크
		List<BoardImg> imgList = new ArrayList<>();
		int level = 0; // 첨부파일의 레벨을 의미
		// 0 : 썸네일, 0이 아닌 값들은 썸네일이 아닌 기타 파일들
		for (MultipartFile upfile : upfiles) {
			if(upfile.isEmpty()) {
				continue;
			}
			// 첨부파일이 존재한다면 web 서버상에 첨부파일 저장
			// 첨부파일 관리를 위해 DB에 첨부파일의 위치정보를 저장
			String changeName = Utils.saveFile(upfile, application, boardCode);
			BoardImg bi = new BoardImg();
			bi.setChangeName(changeName);
			bi.setOriginName(upfile.getOriginalFilename());
			bi.setImgLevel(level++);
			imgList.add(bi); // 연관게시글번호 refBno 값 추가 필요 (게시글 추가가 안되었기 때문에 지금 단계에서는 추가 불가)
		}
		// 게시글 등록 서비스 호출
		//  - 서비스 호출 전, 게시글 정보 바인딩
		//  - 테이블에 추가하기 위해 필요한 데이터 : 회원번호, 게시판 코드
		Member loginUser = (Member) auth.getPrincipal();
		b.setBoardWriter(String.valueOf(loginUser.getUserNo()));
		b.setBoardCd(boardCode);
		
		// 정보체크
		log.debug("board : {}", b);
		log.debug("imgList : {}", imgList);
		int result = boardService.insertBoard(b, imgList);
		
		// 게시글 등록 결과에 따른 페이지 지정
		if (result == 0) {
			throw new RuntimeException("게시글 작성 실패");
			// ExceptionController가 어플리케이션 전역에서 자동으로 캐치해서 에러페이지로 포워딩해줄 것
		}
		ra.addFlashAttribute("alertMsg","게시글 작성 성공");
		return "redirect:/board/list/" + boardCode;
		// forwarding 하면 안되고 리디렉트 시켜야 함. 포워딩 하면 새로고침 눌렀을때 계속추가됨
	}
	
	// 게시판 상세보기
	@GetMapping("/detail/{boardCode}/{boardNo}") // 단순 포워딩은 get DML 필요하면 post
	public String selectBoard(
			@PathVariable("boardCode") String boardCode,
			@PathVariable("boardNo") int boardNo,
			Authentication auth,
			Model model,
			// 사용자의 쿠키 가져오기 (새로고침했을때 조회수증가되는거 방지)
			// 1. HttpServletRequest의 getCookies() // 모든쿠키배열에서 찾고자하는 쿠키를 검색해야함
			// 2. Spring의 @CookieValue // Web MVC 모듈에서 더 편한 방법을 제공. argumentResolver가 또 찾아줌
			@CookieValue(value="readBoardNo", required=false) String readBoardNoCookie,
			HttpServletRequest req,
			HttpServletResponse res
			) {
		/*
		 * 업무로직
		 * 1. boardNo를 기반으로 게시판 정보 조회
		 * 2. 조회수 증가 서비스 호출
		 * 3. 게시판 정보를 model(즉, request영역)에 담은 후 forward 
		 */
		// 게시글 정보를 조회
		// 게시글 정보에 사용자의 이름, 첨부파일 목록을 추가로 담아서 반환하기 위해 BoardExt 사용
		BoardExt b = boardService.selectBoard(boardNo);
		log.debug("게시글 정보 : {}", b);
		if (b == null) {
			throw new RuntimeException("게시글이 존재하지 않습니다.");
		}
		/*
		 * 게시글이 존재하는 경우 조회수 증가 서비스 호출
		 * 게시글 조회수 증가 로직
		 *   일반적인 게시판 서비스
		 *     1) 사용자가 게시글을 새로고침하거나, 반복 조회시 조회수가 무한정 증가
		 *     2) 본인이 작성한 게시글을 조회할 때에도 조회수가 증가
		 *    - 사용자가 어떤 게시글을 열람했는지 정보를 저장해두어야 한다
		 *    저장방식
		 *    DB에 저장 : 모든 사용자의 게시글 열람 기록을 관리하기에 비효율적
		 *    쿠키에 저장 : 클라이언트 브라우저에 사용자가 읽은 게시글 번호를 보관(readBoardNo)
		 *    ex) readBoardNo=11/12/13/14
		 *    + 쿠키의 유효시간 1시간동안 유지되도록 설정, /spring url에 쿠키를 보관
		 */
		int userNo = ((Member) auth.getPrincipal()).getUserNo();
		// 이 url 이용하려면 무조건 인증/인가 처리되었을거기 때문에 auth가 null일수가 없음
		if (userNo != Integer.parseInt(b.getBoardWriter())) {
			boolean increase = false; // 조회수 증가를 위한 체크변수
			// readBoardNo라는 이름의 쿠키가 있는지 조사
			if (readBoardNoCookie == null) {
				// 첫 조회
				increase = true;
				readBoardNoCookie = boardNo + "";
			} else {
				// 쿠키가 있는 경우
				List<String> list = Arrays.asList(readBoardNoCookie.split("/"));
				// 기존 쿠키값들 중 게시글 번호와 일치하는 값이 하나도 없는 경우
				if (list.indexOf(boardNo + "") == -1) {
					increase = true;
					readBoardNoCookie += "/" + boardNo;
				}
			}
			if (increase) {
				int result = boardService.increaseCount(boardNo);
				if (result > 0) {
					b.setCount(b.getCount() + 1);
					// 새 쿠키 생성하여 클라이언트에게 전달
					Cookie newCookie = new Cookie("readBoardNo", readBoardNoCookie);
					newCookie.setPath(req.getContextPath()); // /spring에서만 사용하는 쿠키
					newCookie.setMaxAge(1 * 60 * 60); // 1시간
					res.addCookie(newCookie);
				}
			}
		}
		model.addAttribute("board", b);
		return "board/boardDetailView";
	}
	
	// 첨부파일 다운로드
	@GetMapping("/fileDownload/{boardNo}")
	// 500 404 등 응답상태 직접 관리할 수 있게 해주는 객체 : ResponseENtity
	// 내가 담고있는 content type에 대해서도 설정할 수 있음. file 로 설정해서 앵커태그 다운로드처럼 다운되게 할수있음
	public ResponseEntity<Resource> fileDownload(@PathVariable("boardNo") int boardNo) {
		/*
		 *  업무로직
		 *  1. 첨부파일 정보 조회(db)
		 *  2. 첨부파일의 changeName을 바탕으로 "웹서버상"의 첨부파일 로드
		 *   Resource Loader라는 클래스를 이용. 파일 형태에 상관없이 일관된 방법으로 로딩가능
		 *  3. 로드한 첨부파일을 ResponseEntity를 통해 사용자에게 반환
		 */
		// DB에서 게시글 및 첨부파일 정보 조회
		BoardExt b = boardService.selectBoard(boardNo); // 필요없는 정보도 모두 가져오기 떄문에 좋은방법은 아님
		if (b.getImgList().isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		// Resource 객체 얻어오기
		String changeName = b.getImgList().get(0).getChangeName();
		String realPath = application.getRealPath(changeName);
		File downFile = new File(realPath);
		
		if (!downFile.exists()) {
			return ResponseEntity.notFound().build();
			// db상에는 존재하지마 ㄴ웹서버상에서 자원이 삭제되었을 경우
		}
		
		// 원래라면 인풋아웃풋 스트림같은거 써야함
		Resource resource = resourceLoader.getResource("file:"+realPath);
		
		// 한글로 된 파일네임 있을수도 있으니까 인코딩 타입 설정
		String filename = "";
		try {
			filename = new String(b.getImgList().get(0).getOriginName().getBytes("utf-8"), "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ResponseEntity
			.ok()
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE) 
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename) 
			.body(resource);
		// application/octet-stream? 이게 어떤 파일 형식인지는 모르겠지만, 그냥 바이너리 데이터로 취급해줘 라는 의미
		// 브라우저는 이 타입을 보면 보통 열지 않고 다운로드하도록 처리함 (content-type이 text/html, image/png, application/pdf 등일 경우
		// 파일을 열어버릴 수 도 있음. 근데 이거는 네가 열지 말고 다운받아야 해 그리고 파일의 이름은 뭐무머야~ 라고 지정하는것)
		// 브라우저 내부 설정이나 정책을 변경하는건 불가능하고, 대신 브라우저가 이 응답을 처리할 때 어떻게 행동할지를 제안하는 역할
	}
	
}
