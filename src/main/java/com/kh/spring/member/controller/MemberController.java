package com.kh.spring.member.controller;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.vo.Member;

@Controller // component-scan 에 의해 자동으로 bean 객체로 등록됨
@SessionAttributes({"loginUser"}) // Model에 들어가는 데이터 중 Session에 보관시킬 데이터를 설정하는 주석
public class MemberController {
	
	@Autowired // 필드기반 의존성 주입 (생성자, setter 기반도 있음)
	private MemberService mService; // = new MemberServiceImpl();
	// 구식. memberServiceImpl에 기술해놓은 spring과 관련된 어노테이션은 사용할 수 없음
	// vo나 dto 클래스 외에는 스프링 ioc 컨테이너가 만들어놓은 객체를 주입받아서 사용해야 함
	/*
	 * Spring의 Dependency Injection (DI)
	 *  - 의존성 주입
	 *  - 어플리케이션을 구성하는 객체를 개발자가 직접 생성하는게 아닌, 스프링이 생성한 객체를 주입받아서 사용하는 방식
	 *  - new 연산자를 직접 사용하지 않고, 자료형 선언만 한 후 @Autowired 어노테이션을 통해 주입받음
	 */
	@RequestMapping(value="/member/login", method=RequestMethod.GET) // 핸들러 매핑에 정보가 등록됨
	// url 패턴과 전송방식을 함께 보관. 405에러 method not allowed (post방식을 안만들어서)
	public String loginMember() {
		return "member/login"; // forwarding할 jsp의 경로
	}
	
	// 스프링의 argument resolver
//	@RequestMapping(value="/member/login", method=RequestMethod.POST)
//	public String login(HttpServletRequest request) {
		// 매개변수가 있으면 argument resolver라는 애가 request 값을 바인딩 (핸들러 매핑에 담겨있음)
		// 가장 원시적인 servlet과 같은 방식
//		System.out.println(request.getParameter("userId"));
//		return "main";
//	}
	
	// RequestParam 어노테이션
	// servlet의 request.getParameter("키")로 뽑은 역할을 대신 수행해주는 어노테이션
	// 클라이언트가 요청한 값을 대신 변환하여 바인딩 해주는 역할은 argumentResolver가 수행
//	@RequestMapping(value="/member/login", method=RequestMethod.POST)
//	public String login(@RequestParam(value="userId", defaultValue="mkm") String userId) {
		// 변수단위, vo 클래스단위, 맵형태의 데이터로 받아올 수 있음. defaultValue는 선택사항
//		System.out.println(userId);
//		return "main";
//	}
	
	// @RequestParam 생략
	// 매개변수의 이름과 일치하는 request의 파라미터의 키값을 추출하여 바인딩
	// 일치하는게 없으면 null값이 들어감
//	@RequestMapping(value="/member/login", method=RequestMethod.POST)
//	public String login(String userId, String userPwd) { // requestParam 기능이 자동으로 실행됨
//		System.out.println(userId + " :: " + userPwd);
//		return "main";
//	}
	
	/*
	 * 커맨드 객체 방식
	 *  메서드의 매개변수로 vo 클래스 타입을 지정하는 경우, 요청 전달값의 name 속성과 일치하는 vo 클래스 필드의
	 *  setter 함수를 호출하여 바인딩
	 * 작동방식 1 : @ModelAttribute를 붙여서 커맨드 객체임을 명시. 추가적인 기능이 있음!
	 *  @sessionAttribute와 연동되어 있음 (세션에 데이터를 저장하는 기능..?)
	 * 작동방식 2 : 명시하지 않는 경우 자동바인딩만 이루어짐
	 */
//	@RequestMapping(value="/member/login", method=RequestMethod.POST)
//	public String login(Member m) { // 매개변수의 클래스와 일치하는 기본생성자 호출
		// 파라미터의 key값과 클래스의 필드명이 일치하는 경우 setter 함수 호출해서 바인딩
//		System.out.println(m.getUserId() + " :: " + m.getUserPwd());
//		return "main"; // viewname을 그대로 반환
//	}
	
//	@RequestMapping(value="/member/login", method=RequestMethod.POST)
//	public ModelAndView login(Member m, ModelAndView mv, Model model) {
		// 디스패쳐 서블릿이 미리 만들어둔 모델앤뷰 객체를 가져온다
		// Model 안에는 전달할 응답 데이터를 저장하면 된다
		/*
		 * 로그인 비즈니스 작업 처리 완료 후, "응답 데이터"를 담아 응답 페이지로 redirect(재요청)
		 * 
		 * Model은 내부에 데이터를 추가 시 addAttribute() 함수를 사용하여 데이터를 추가
		 * ModelAndView는 데이터 추가 시 addObject()를 사용하며 view 설정시 setViewName()을 사용
		 * 
		 * 응답데이터를 담을 수 있는 객체 (request/session의 setAttribute()를 대체)
		 * 1) Model
		 *  - 포워딩할 응답 뷰페이지로 전달하고자 하는 데이터를 맵형식으로 담을 수 있는 객체
		 *  - 기본 requestScope에 설정을 통해 sessionScope에도 데이터를 담을 수 있다
		 *  - 클래스 선언부에 @SessionAttributes를 추가하면 데이터가 세션스코프로 저장됨
		 * 2) ModelAndView
		 *  - ModelAndView에서 Model은 데이터를 담을 수 있는 맵형태의 객체
		 *  - View는 이동할 페이지에 대한 정보를 담고있는 객체
		 *  - 기본 requestScope에 데이터를 보관
		 */
//		model.addAttribute("errorMsg", "오류발생"); // request.setAttribute()와 똑같음
		// @SessionAttributes 설정을 통해 request.getSession().setAttribute()도 가능
//		mv.addObject("errorMsg", "오류발생"); // 지금은 중복데이터를 담고있음
//		mv.setViewName("common/errorPage"); // 원래는 데이터 초기화를 위해 리디렉트 해줘야 함
		// 재요청시에는 모델을 이용해야 되는게 세션스코프에 데이터 담아야되기 때문에
		// "로그인 성공"과 같은 일회성 메시지는 아예 다른 영역에 담음 => 나중에 부가설명
		// errorMsg를 리퀘스트 스코프에 담아서 errorPage로 포워딩
//		return mv;
//	}
	
	//@RequestMapping(value="/member/login", method=RequestMethod.POST)
	@PostMapping("/member/login")
	public ModelAndView login(
		@ModelAttribute Member m, 
		ModelAndView mv, 
		Model model,
		HttpSession session, // 로그인 성공 시 사용자 정보를 보관할 객체 (고전적인 방식)
		// 모델로 세션 데이터 관리해도 됨
		RedirectAttributes ra
	) {
		// 로그인 요청 처리 (스프링 시큐리티를 적용할 때랑 아닐때랑 다름)
		Member loginUser = mService.loginMember(m);
		// 로그인 성공 시 회원정보, 실패 시 null값이 전달
		
		// 스프링 시큐리티가 적용됐을 때와 다를 것
		// 똑같이 세션에 보관하지만 사용자 인증정보를 직접 만든 객체가 아니라 암호화된 데이터가 들어갈 것
		// 다양함. 쿠키에 보관하는 경우도 있음
		if(loginUser != null) {
			// 사용자 인증정보 (loginUser)를 session에 보관
			//session.setAttribute("loginUser", loginUser);
			model.addAttribute("loginUser", loginUser); // 원래는 리퀘스트 스코프에 저장되는데
			// 클래스 레벨에 설정한 어노테이션을 통해서 세션스코프에 저장
			//session.setAttribute("alertMsg", "로그인 성공!"); // 일회성 메시지인데 영구히 저장됨
			ra.addFlashAttribute("alertMsg", "로그인 성공!");
			// 편법을 사용해서 리디렉트 시 세션스코프에 정보 저장했다가 자동으로 삭제
			/*
			 * RedirectAttributes의 flashAttribute는 데이터를 sessionScope에 담고,
			 * 리다이렉트가 완료되면 sessionScope에 있던 데이터를 requestScope로 변경 (초기화)
			 */
		} else {
			//session.setAttribute("alertMsg", "로그인 실패!");
			ra.addFlashAttribute("alertMsg", "로그인 실패!");
		}
		mv.setViewName("redirect:/"); // 메인페이지로 리디렉트
		// 컨텍스트 패스는 생략되었는데 디스패쳐 서블릿이 추가해주는것으로 추정됨
		return mv;
	}
	
	@GetMapping("/member/logout") // 아래에 작성되는 메서드는 핸들러 매핑에 등록됨
	public String logout(HttpSession session, SessionStatus status) {
		// 로그아웃 방법
		// 1. session 내부의 인증정보를 무효화
		//session.invalidate(); // 세션 내부의 모든 (httpSession에 직접 추가한)데이터를 삭제
		status.setComplete(); // model로 sessionScope에 이관된 데이터를 비우는 메서드
		return "redirect:/";
	}
	
	@GetMapping("/member/insert")
	public String enrollForm() {
		return "member/memberEnrollForm";
	}
	
	@PostMapping("/member/insert")
	public String insertMember(
		Member m,
		Model model,
		RedirectAttributes ra
	) {
		int result = mService.insertMember(m);
		String viewName = "";
		if (result > 0) {
			ra.addFlashAttribute("alertMsg", "회원가입 성공 ^^");
			viewName = "redirect:/member/login";
			// url 패턴 작성. 접두어 접미어 추가 자체가 안 됨 (위임처리 하는게 아니니까)
		} else {
			model.addAttribute("errorMsg", "회원가입 실패 ㅠㅠ");
			viewName = "common/errorPage";
		}
		return viewName;
	}
	
	@GetMapping("/member/myPage")
	public String myPage() {
		return "member/myPage";
	}
	
	// 데이터 암호화 + 유효성 검사 + 권한 확인(인가) => 스프링 시큐리티가 할거임
	@PostMapping("/member/update") // 마이페이지로 넘기거나, 로그아웃 해서 메인페이지로 넘기거나
	public String updateMember(
		Member m,
		RedirectAttributes ra,
		Model model // 업데이트를 실패하는 경우 포워딩을 위해
	) {
		int result = mService.updateMember(m);
		String url = "";
		
		//throw new RuntimeException("예외발생"); // 임의로 예외 강제로 발생시키기
		// 아래에 exceptionHandler 안썼으면 500 에러페이지 발생하는것임
		
		if (result > 0) {
			// 업데이트 성공 시
			ra.addFlashAttribute("alertMsg", "내 정보 수정 성공");
			url = "redirect:/member/logout";
			// 로그아웃 안시키면 수정된 정보 다시 불러오는 코드 작성해야 함
			// 로그인 시에만 loginUser에 값을 담고있기 때문에
		} else {
			model.addAttribute("errorMsg", "회원정보 수정 실패");
			url = "common/errorPage";
		}
		return url;
	}
	
	/*
	 * 스프링 예외처리 방법 
	 *  1. try - catch로 메서드별 예외처리 --> 1순위로 적용
	 *  2. 하나의 컨트롤러에서 발생하는 예외들을 모아서 처리하는 방법 --> 2순위로 적용
	 *   컨트롤러에 예외처리 메서드를 1개 추가한 후, @ExceptionHandler 어노테이션을 추가
	 *   (스프링 웹 mvc 모듈 안에 들어있음)
	 *  3. 어플리케이션 전역에서 발생하는 예외를 모아서 처리하는 방법 --> 3순위
	 *   새로운 클래스 작성 후, 클래스에 @ControllerAdvice를 추가 (예외처리 뿐만아니라 공통작업 처리도 가능)
	 *   모든 컨트롤러에서만 발생하는 예외! Service나 Repository는 안됨
	 */
//	@ExceptionHandler // 매개변수에 추가적인 속성 넣을 수 있음 IOException/ RuntimeException 같은거
//	public String exceptionHandler(Exception e, Model model) {
//		e.printStackTrace();
//		model.addAttribute("errorMsg", "서비스 이용 중 문제가 발생했습니다.");
//		return "common/errorPage";
//	}
	
	// 비동기 요청
	@ResponseBody // 스프링 웹 mvc 모듈에서 어노테이션 중 하나 (반환되는 값이 값 그 자체임을 의미하는 주석)
	@GetMapping("/member/idCheck")
	public String idCheck(String userId) {
		int result = mService.idCheck(userId); // 아이디 존재시 1 없다면 0
		// 후속 처리는 여기서 안하고 값만 전달함
		/*
		 * 컨트롤러에서 반환되는 값은 기본적으로 응답 forwarding 경로 혹은 redirect를 위한 경로로 해석한다.
		 * 즉, 반환되는 문자열 값은 "특정 경로"로써 인식을 하는데, 경로가 아닌 값 자체를 반환시키기 위해서는
		 * @ResponseBody 어노테이션이 필요하다.
		 */
		return result+""; // 무조건 반환형이 문자열 혹은 모델앤뷰 객체
		// 1, 0 -> /WEB-INF/views/1.jsp -> 404 에러
		// vo 객체 json 형태로 반환하려면 추가적인 라이브러리가 필요함
		// Gson 사용해도 contentType 지정이 안돼서 그냥 문자열로 해석이 됨
	}
	
//	@PostMapping("/member/selectOne")
//	@ResponseBody
//	public HashMap<String, Object> selectOne(String userId) { // 반환형 vo 클래스여도 상관없음
//		HashMap<String, Object> map = mService.selectOne(userId);
//		return map; // map.toString() 형태의 문자열 데이터가 반환됨
	
		// map이 뷰 이름으로 해석되어 map.put("message", "hello"); 이런식으로 데이터 넣고 바로 반환하면
		// viewResolver가 "message=hello" 라는 이름의 뷰를 찾으려고 하고 못찾으면 에러
		// 또는 dispatcherServlet이 이상하게 처리해서 toString() 결과를 문자열로 출력할수도 있음
		// 그럼 @ResponseBody 없이 String이 아닌 객체를 반환하려고 하면 컴파일에러를 발생시키면 되는거 아님?
		// @ResponseBody는 런타임 시점에 리플렉션으로 확인하는 어노테이션이라서. 컴파일러는 어노테이션의 존재 여부에
		// 따라 동작을 바꾸지 않고 문법 체크만 함. 그래서 문법적으로는 틀린게 없어서 에러가 안남.
		// 스프링에서 쓰는 대부분의 어노테이션은 런타입 시점에 리플렉션으로 읽혀서 동작을 제어함. 모든 어노테이션이 그런건
		// 아니고, 어노테이션 자체의 유지 정책(retention policy)에 따라 다름
	
		// 스프링에서는 Gson 안쓰고 jackson-databind를 활용하여, vo클래스 arrayList map 형태의 데이터를
		// 자동으로 json 형태로 파싱해서 바인딩하기
//	}
	
	// responseEntity 객체를 반환하면 알아서 데이터를 전달해주는구나 하고 어노테이션이 필요없음
	@PostMapping("/member/selectOne")
	public ResponseEntity<HashMap<String, Object>> selectOne(String userId) {
		HashMap<String, Object> map = mService.selectOne(userId);
		ResponseEntity res = null;
		if (map != null) {
			res = ResponseEntity
					.ok() // 응답상태 : 200을 의미. 바디영역에 추가할 데이터를 담아줄 수 있음
					//.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE) 
					// 어떤 형태의 데이터인지 알려줌 (사실 잭슨이가 다 설정해줘서 없어도 됨)
					// setContentType("application/json; charset" 어쩌구를 상수형태로 저장해둠)
					.body(map); // build()의 역할도 대신하고 있음
		} else {
			res = ResponseEntity
					.notFound() // 응답상태 : 404를 의미
					// 내가 직접 응답상태를 지정해줘야 할 때. 그냥 map만 반환하려면 responseBody만 쓰면 됨
					.build();
					// viewResolver를 통해 데이터를 JSON 형태로 바꿀수도 있다. 옛날에는 Gson도 썼음
		}
		// 변환처리는 여전히 jackson이 해주는거기 때문에 컨텐츠타입이 json이다 만 지정해줘서 무조건 의존성 추가해야함
		return res;
	}
}
