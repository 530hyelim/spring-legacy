package com.kh.spring.security.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.validator.MemberValidator;
import com.kh.spring.member.model.vo.Member;
import com.kh.spring.security.model.vo.MemberExt;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j // 로깅 프레임워크
public class SecurityController {
   private BCryptPasswordEncoder passwordEncoder;
   private MemberService mService;
   
   // 생성자방식 의존성 주입(생성자가 현재 클래스에 1개라면 @Autowired 생략 가능)
   public SecurityController(BCryptPasswordEncoder passwordEncoder, MemberService mService) {
      this.passwordEncoder = passwordEncoder;
      this.mService = mService;
   }
   
   // 에러페이지 포워딩용 url
   @GetMapping("/security/accessDenied")
   public String accessDenied(Model model) {
      model.addAttribute("errorMsg","접근 불가");
      return "common/errorPage";
   }
   
   // 회원가입 페이지 이동
   @GetMapping("/security/insert")
   public String enroll(@ModelAttribute Member member) {
      // ModelAttribute
      // - 커맨드객체 바인딩시 사용 (요청 파라미터를 바인딩해서 객체에 넣어주고)
	  //  - 커맨드 객체?
	  //    클라이언트가 보낸 요청 파라미터(form 데이터 등)을 스프링이 자동으로 바인딩해주는 일반 자바객체(POJO)
	  //    요청 파라미터 이름과 객체 필드명이 일치하면 자동으로 값이 들어감
      // - model영역에 커맨드객체를 저장하는 기능 (그 객체를 Model에 자동등록 해주는 어노테이션)
	  //  - Model 객체?
	  //    컨트롤러에서 뷰로 데이터를 넘길 때 사용하는 객체
	  //    model.addAttribute("이름", 객체) => 뷰에서 ${이름.필드}로 접근 가능
	  // - 언제 씀? 명시적으로 바인딩 대상 객체를 표시하고 싶을 때, 뷰에서 해당 객체를 사용해야 할 때
      return "member/memberEnrollForm";
   }
   
   /*
    * InitBinder
    *  - 현재 컨트롤러에서 바인딩 작업을 수행할 때 실행되는 객체
    *  - @ModelAttribute에 대한 바인딩 설정을 수행
    *  
    * 처리 순서
    * 1. 클라이언트의 요청 파라미터를 커맨드 객체 필드에 바인딩
    * 2. 바인딩 과정에서 WebDataBinder가 필요한 경우, 타입 변환이나 유효성 검사를 수행
    * 3. 유효성 검사 결과 BindingResult에 저장 
    */
   @InitBinder
   public void initBinder(WebDataBinder binder) {
      binder.addValidators(new MemberValidator());
      
      // 타입 변환
      // 문자열 형태의 날짜값을 Date타입으로 변환
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd"); // 생년월일
      dateFormat.setLenient(false); // yyMMdd형식이 아닌 경우 에러를 발생시킬지
      
      binder.registerCustomEditor(Date.class, "birthDay", new CustomDateEditor(dateFormat, true)); // birthday=990510과 같은 데이터가 들어오는 경우 수행되는 커스텀에디터 등록
   }
   
   @PostMapping("/security/insert")
   public String register(
		 // @Validated? 유효성 검사 트리거
		 // !!중요!! 반드시 뒤에 BindingResult가 있어야 함!
         @Validated @ModelAttribute Member member,
         /*
          * BindingResult
          *  - 유효성검사 결과를 저장하는 객체
          *  - forward시 자동으로 jsp에게 전달되며, form태그 내부에 에러내용을 바인딩할 때 사용 
          */
         BindingResult bindingResult,
         RedirectAttributes ra
         ) {
      // 유효성 검사
      if (bindingResult.hasErrors()) {
    	  return "member/memberEnrollForm";
      }
      // 유효성 검사 통과시 비밀번호 정보는 암호화하여 회원가입 진행
      String encryptedPassword = passwordEncoder.encode(member.getUserPwd());
      member.setUserPwd(encryptedPassword);
      mService.insertMember(member);
      // 회원가입 완료 후 로그인 페이지로 리다이렉트
      return "redirect:/member/login";
   }
   
   /*
    * Authentication
    *  - Principal : 인증에 사용된 사용자 객체 (MemberExt)
    *  - Credentials : 인증에 필요한 비밀번호에 대한 정보를 가진 객체 (내부적으로 인증작업시 사용)
    *  - Authorities : 인증된 사용자가 가진 권한을 저장하는 객체
    *  UserDetails 를 바탕으로 authentication을 만듦
    *  보안과 상관없는 데이터 -> principal, 보안관련 데이터 -> credentials, 권한관련 -> authorities
    */
   @GetMapping("/security/myPage")
   public String myPage(Authentication auth2, Principal principal2, Model model) {
	   // 인증된 사용자 정보 가져오기
	   // 1. SecurityContextHolder 이용 (쓰레드 로컬. 권장됨)
	   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	   // filter의 첫번째 단계에서 authentication 객체가 securityContextHolder에 저장됨
	   MemberExt principal = (MemberExt) auth.getPrincipal(); // object 형태로 저장돼서 다운캐스팅 필요
	   model.addAttribute("loginUser", principal); // form 태그의 데이터 바인딩을 위해
	   
	   // 2. ArgumentResolver를 이용한 자동바인딩 (현재 메서드를 실행할 때 선언한 매개변수가 있으면 알아서 처리해주는 클래스)
	   // 1, 2번 방식을 가장 대중적으로 많이 사용. Credential=protected로 로그에서도 중요한정보 안보이게 설정됨
	   // 2번 방식으로 principal 뽑으면 rememberMeAuthenticationToken으로 나옴
	   log.info("auth = {}", auth);
	   log.info("principal = {}", principal);
	   log.info("auth2 = {}", auth2);
	   log.info("principal2 = {}", principal2);
	   
	   return "member/myPage";
   }
   
   @PostMapping("/security/update")
   public String update(
		   @Validated @ModelAttribute MemberExt loginUser,
		   BindingResult bindResult, // 규칙상 반드시 우측에 유효성검사 결과 담을 객체
		   Authentication auth, // argument resolver 방식으로 로그인한 사용자 인증정보 가져오기
		   RedirectAttributes ra
		   ) {
	   if(bindResult.hasErrors()) {
		   // 현재는 userId에 대한 유효성 검사하는 코드가 없으므로 모두 통과할 것
		   return "redirect:/security/myPage";
	   }
	   // 비즈니스 로직
	   // 1. db의 member 객체를 수정
	   int result = mService.updateMember(loginUser);
	   // 2. 변경된 회원정보를 db에서 얻어온 후 새로운 인증정보를 생성하여 스레드로컬에 저장
	   // 로그아웃하고 재로그인하면 새로운 인증정보 생성됨. 로그아웃 없이 서비스 이용하게 하려면?
	   //  - 변경된 회원정보(loginUser) => principal은 이미 얻어왔고
	   //  - authorities, credentials 필요 => 인증 토큰에 필요함
	   
	   // 새로운 authentication 객체 생성
	   Authentication newAuth = new UsernamePasswordAuthenticationToken(
			   loginUser, auth.getCredentials(), auth.getAuthorities());
	   SecurityContextHolder.getContext().setAuthentication(newAuth);
	   ra.addFlashAttribute("alertMsg","내 정보 수정 성공");
	   return "redirect:/security/myPage";
   }
}

