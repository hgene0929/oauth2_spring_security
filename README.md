# oauth2_spring_security

[학습내용]
1. 초기화 과정 이해
- SecurityBuilder / SecurityConfigurer
  - SecurityBuilder는 빌더 클래스로서 웹 보안을 구성하는 빈 객체와 설정 클래스들을 생성하는 역할을 하며 WebSecurity, HttpSecurity 가 있다
  - SecurityConfigurer는 Http 요청과 관련된 보안처리를 담당하는 필터들을 생성하고 여러 초기화 설정에 관여한다
  - SecurityBuilder는 SecurityConfigurer를 포함하고 있으며 인증 및 인가 초기화 작업은 SecurityConfigurer에 의해 진행된다
  - 즉, 초기화시 SecurityBuilder의 build()에 의해 SecurityConfigurer(구현체)가 생성되어 내부의 init() & configure()을 통해 초기화가 이루어진다
  - 초기화 상세과정 :
    - SecurityBuilder -> WebSecurity/HttpSecurity의 apply() 메소드의 파라미터를 통해 초기화 대상(SecurityConfigurer의 구현체) 적용
    - 초기화 대상 적용 -> build()를 호출하여 SecurityConfigurer(구현체) 내부의 init(), configure()를 호출함으로써 인증 및 인가처리에 필요한 객체 생성
    - 초기화 작업 완료 -> WebSecurity의 최종반환값은 FilterChainProxy | HttpSecurity의 최종반환값은 SecurityFilterChain
      - FilterChainProxy가 SecurityFilterChain를 가지고 있으며 사용자 요청과정에서 필요한 필터를 호출하는 관계이다
  - 구현 :
    - 스프링 시큐리티는 자동으로 SecurityConfigurer를 SecurityBuilder에 디폴트로 넣어둠으로써 라이브러리 추가만으로 인증 및 인가처리 기능을 사용할 수 있도록 제공한다
    - 여기서 SecurityConfigurer 설정클래스를 커스텀하여 설정클래스에서 SecurityBuilder에 파라미터로 추가함으로써 직접 구현한 기능의 인증 및 인가처리도 구현할 수 있는 것이다
  - 참고 : [SecurityBuilder/SecurityConfigurer](https://www.notion.so/hgene/Oauth2-792e15aa883746bc9d8296a8518184d6)
---------------------------------------------------
- 자동설정에 의한 초기화 진행
  - SpringBuilder 클래스의 구동과정 상세 :
    - SpringWebMvcImporterSelector 설정 클래스가 로드되어 실행된다
      - ImporterSelector 인터페이스를 구현한 클래스를 로드할 경우, 원하는 조건에 따른 설정클래스를 선택하여 로드할 수 있다 
      - WebMvcConfiguration을 로드한다
    - SecurityFilterAutoConfiguration 설정 클래스가 로드되어 실행된다
      - DelegatingFilterProxyRegistrationBean을 생성한다 
      - 이때 해당 클래스는 springSecurityFilterChain 이름의 빈 검색해서 빈에게 클라이언트의 요청을 위임하는 역할의 DelegatingFilterProxy를 등록한다
    - WebMvcSecurityConfiguration 설정 클래스가 로드되어 실행된다
      - ArgumentResolver 타입의 클래스인 AuthenticationPrincipalArgumentResolver, CurrentSecurityContextArgumentResolver, CsrfArgumentResolver를 생성한다 
      - 이때, AuthenticationPrincipalArgumentResolver을 통해 @AuthenticationPrincipal로 선언된 변수가 있다면 자동으로 인증을 받은 Principal 객체에 바인딩해준다
    - HttpSecurityConfiguration 
      - HttpSecurity를 통해 공통 설정 클래스와 필터들을 생성하고(설정클래스마다 각각의 필터생성) 최종적으로 SecurityFilterChain 빈을 반환한다
      - 프로토타입으로 생성되어 빈 생성마다 각각의 클래스가 생성된다
    
    - SpringBootWebSecurityConfiguration
      - SecurityFilterChain 타입의 빈을 실행시킨다(시큐리티 설정클래스에 빈으로 정의해둔 HttpSecurity 관련 인증 및 인가 관련 정책들)
      - 자동설정시 기본 SecurityFilterBean 빈은 Form 로그인과 Basic 로그인이 추가되어있다
    - WebSecurityConfiguration
      - WebSecurity를 생성한다
      - WebSecurity는 설정클래스에서 정의한 SecurityFilterChain 빈을 SecurityBuilder에 저장한다
      - 이후 WebSecurity가 build()를 실행하면 SecurityBuilder에서 SecurityFilterChain을 꺼내어 FilterChainProxy 생성자에게 전달한다
   
    - 결과 : 
      - HttpSecurity에 의해 생성된 여러개의 SecurityFilterChain이 생성된다
      - WebSecurity에 의해 해당 빈들을 SecurityBuilder에 저장하고, build() 실행시 FilterChainProxy에 전달한다 
    - 참고 : [자동설정에 의한 초기화 진행](https://www.notion.so/hgene/Oauth2-792e15aa883746bc9d8296a8518184d6)
---------------------------------------------------
- 커스텀 설정에 의한 초기화 진행
  - 설정 클래스를 커스텀하게 생성할 경우, SpringWebSecurityConfiguration의 SecurityFilterChainConfiguration 클래스가 구동되지 않는다
    - 즉, 디폴트 SecurityFilterChain 타입을 반환하는 설정들이 빈으로 생성되지 않는다
---------------------------------------------------
- AuthenticationEntryPoint 이해
  - 스프링 시큐리티는 디폴트 설정으로 form 로그인 인증 방식과 http 로그인 인증방식을 설정한다
  - 이때, 각각의 방식에 대한 인증 예외처리(AuthenticationEntryPoint) 방식을 지정한다 :
    - ExceptionHandlingConfigurer : 예외를 관장하는 설정 클래스가 초기화 과정에서 인증예외에 대한 설정을 진행한다
      - 초기화 과정에서 AuthenticationEntryPoint 객체를 생성하여 전달한다
      - ExceptionHandlingConfigurer는 전달받은 AuhthenticationEtryPoint 객체를 defaultEntryPointMappings(맵객체)에 전달한다

      - 만약 스프링 시큐리티의 디폴트 설정이 아닌 커스텀한 필터를 사용한다면 커스텀한 인증방식에 대한 커스텀 예외처리 후속방식 또한 직접 생성하여 적용할 수 있다
        - 따라서 CustomEntryPoint 객체가 있는지 여부를 가장 우선적으로 검사 후, 만약 존재한다면 해당 객체를 통해 예외처리하고 나머지 디폴트 방식(form, httpbasic)의 예외처리는 무시된다

  - ExceptionTranslationFilter : 최종적으로 사용할 AuthenticationEntryPoint 객체가 저장되어 인증예외 발생시 후속처리 작업에 사용된다
    - 만약 커스텀한 예외처리객체가 존재하지 않는다면, 스프링 시큐리티는 디폴트 인증 방식 중 현재 사용되고 있는 방식이 무엇인지 검사후 이에 알맞은 AuthenticationEntryPoint 객체를 ExceptionTranslationFilter에 넘겨준다

  - 참고 : [AuthenticationEntryPoint 이해](https://www.notion.so/hgene/Oauth2-792e15aa883746bc9d8296a8518184d6)
---------------------------------------------------
2. 기본 요소 이해
- 시큐리티 인증 및 인가 흐름 요약
  - 스프링 시큐리티 인증 및 인가 흐름의 공통된 패턴 :
    - SevletFilter 영역
      - 사용자 요청 > DelegatingFilterProxy > FitlerChainProxy
      - DelegatingFilterProxy : 
        - 사용자 요청시 이를 가장 먼저 받는 주체는 Sevlet(WAS서버)이다
        - 그러나 WAS는 스프링 컨테이너(DI 등)의 역할을 할 수 없다
        - 따라서 DelegatingFilterProxy를 통해 사용자 요청을 스프링 컨테이너에 속해있는 FilterChainProxy에 전달하여 스프링의 기능을 사용할 수 있도록 한다
      - FitlerChainProxy :
        - 인증을 위해 설정해둔 SecurityFilterChain들을 호출하며 처리한다

    - Authentication 영역
      - AuthenticationFilter :
        - 인증 필터가 요청을 받아와 요청에 포함된 정보(ID,PW)를 통해 Authentication 객체를 생성하여 저장한다
        - Authentication 인증 객체를 전달함과 동시에 AuthenticationManager를 호출한다
        - 전달받은 Authentication(UserDetails가 담긴) 인증객체를 SecurityContext에 저장한다
          - SecurityContext에 저장된 인증성공된 사용자 인증객체는 애플리케이션 어디에서든지 호출하여 사용가능하다
      - AuthenticationManager :
        - 인증처리를 실질적으로 할 수 있는 AuthenticationProvider 클래스를 찾아서 인증처리를 위임한다
      - AuthenticationProvider :
        - 전달받은 Authentication 객체를 통해 실질적으로 인증처리를 한다(인증성공/실패여부 결정)
        - 인증처리 과정에서 ID 검증과정에서 데이터계층(DB)로부터 해당 ID의 사용자가 존재하는지 확인하기 위해 UserDetailsService 클래스를 호출한다
        - PW 검증과정은 PasswordEncoder를 통해 암호화되어 저장된 PW와 요청받은 정보의 PW가 일치하는지 확인한다
        - 최종적으로 인증에 성공했을 경우, Authentication 객체를 새롭게 생성하고, UserDetails 타입의 사용자 정보를 저장하여 반환한다
      - UserDetailsService :
        - 데이터 계층으로부터 인증객체에 포함된 ID를 가진 사용자가 존재하는지 여부를 추출한다
        - 존재할 경우, 사용자에 대한 정보를 UserDetails 타입으로 생성하여 반환한다

    - Authorization 영역
      - 인증 이후, 특정한 권한이 필요한 자원에 사용자가 접근을 요청하는 경우 SecurityContext에 인증객체가 이미 존재하므로 인증영역은 건너뛰고 인가영역의 프로세스가 실행된다
      - ExceptionTranslationFilter :
        - 인증 및 인가예외가 발생할 경우 그에 대한 처리를 한다
        - FilterSecurityInterceptor을 호출하여 예외처리 발생여부를 확인하고, 에외발생할 경우 내부에 저장된 AuthenticationEntryPoint를 실행하여 예외처리를 한다
      - FilterSecurityInterceptor :
        - AccessDecisionManager에 예외처리에 해당 자원에 대한 권한심사를 위임한다
      - AccessDecisionManager :
        - 접근을 허가할 것인지 여부를 최종적으로 판단하기 위해 AccessDecisionVoter를 호출한다
      - AccessDecisionVoter :
        - 현재 사용자가 접근하고자 하는 자원에 권한이 있는지 여부를 판단한다
    
  - 참고 : [시큐리티 인증 및 인가 흐름 요약](https://www.notion.so/hgene/Oauth2-040722d68b4d4232aafba193890ce27f)
---------------------------------------------------
- HttpBasic 인증
  - HTTP는 엑세스 제어와 인증을 위한 프레임워크를 제공하며 가장 일반적인 인증 방식은 "Basic" 인증 방식이다
    - 클라이언트는 인증정보 없이 서버로 접속을 시도한다
    - 서버가 클라이언트에게 인증요구를 보낼 때, 401 Unauthorized 응답과 함께 WWW-Authenticate 헤더를 기술해서 realm(보안영역)과 Basic 인증방법을 보낸다
    - 클라이언트가 서버로 접속할 때 Base64로 username과 password를 인코딩하고 Authorization 헤더에 담아서 요청한다
    - 성공적으로 완료되면 정상적인 상태코드를 반환한다
  - 주의사항 
    - base64 인코딩된 값은 쉽게 디코딩될 수 있기 때문에 인증정보가 노출된다
    - HTTP Basic 인증은 반드시 HTTPS와 같이 TLS 기술과 함께 사용해야 한다

  - 스프링 시큐리티의 HTTP Basic 인증과정
    - HTTPBasicConfigurer 
      - HTTP Basic 인증에 대한 초기화를 진행하며 속성들에 대한 기본값들을 설정한다
      - 기본 AuthenticationEntryPoint는 BasicAuthenticationEntryPoint
      - 필터는 BasicAuthenticationFilter
    - BasicAuthenticationFilter
      - BasicAuthenticationConverter를 사용하여 요청 헤더에 기술된 인증정보의 유효성을 체크 > Base64로 인코딩된 username과 password 추출
      - 인증 성공시, SecurityContext에 인증객체 저장, 실패시 BasicAuthenticationEntryPoint(Basic 인증을 통해 다시 인증하라고 요구) 호출
      - 인증 이후 
        - 세션을 사용하는 경우 : 매 요청마다 인증과정 X
        - 세션을 사용하지 않는 경우 : 매 요청마다 인증과정 O

  - 참고 : [HTTPBasic 인증](https://www.notion.so/hgene/Oauth2-040722d68b4d4232aafba193890ce27f)
---------------------------------------------------
- Cors 이해(Cross-Origin Resource Shaing, 교차 출처 리소스 공유)
  - Http 헤더를 사용하여, 한 출처에서 실행 중인 웹 애플리케이션이 다른 출처의 선택한 자원에 접근할 수 있는 권한을 부여하도록 브라우저에 알려주는 체제이다
  - 웹 애플리케이션이 리소스가 자신의 출처와 다를 때 브라우저는 요청 헤더에 Origin 필드에 요청 출처를 함께 담아 교차 출처 HTTP 요청을 실행한다
  - 출처를 비교하는 로직은 서버에 구현된 스펙이 아닌 브라우저에 구현된 스펙 기준으로 처리되며 브라우저는 클라이언트의 요청 헤더와 서버의 응답헤더를 비교해서 최종 응답을 결정한다
    - 두개의 출처를 비교하는 방법은 URL의 구성요소 중 Protocol, Host, Port 이 세가지가 동일한지 확인하면 되고 나머지는 틀려도 상관없다
  
  - CORS 요청 종류 :
    - Simple Request 
      - 예비 요청(Preflight)을 과정 없이 바로 서버에 본 요청을 한 후, 서버가 응답의 헤더에 Access-Controll-Allow-Origin과 같은 값을 전송하면 브라우저가 서로 비교한 후 CORS 정핵 위반여부를 검사
      - 제약사항
        - GET, POST, HEAD 중의 한가지 메소드를 사용해야 한다
        - 헤더는 Accept, Accept-Language, Content-Language, Content-Type, DPR, Downlink, Save-Data, View-Width, Width만 가능(커스텀 헤더 불가)
        - Content-Type은 application/x-www-form-urlencoded, multipart/form-data, text/plain만 가능
    - Preflight Request 
      - 브라우저는 요청을 한번에 보내지 않고, 예비요청과 본요청으로 나누어 서버에 전달한다
      - 예비요청의 메소드에는 OPTIONS가 사용된다
      - 예비요청을 통해 브라우저 스스로 안전한 요청인지 확인하는 것이다

  - CORS 해결 : 서버에서 Access-Control-Allow-* 세팅
    - Access-Control-Allow-Origin : 헤더에 작성된 출처만 브라우저가 리소스를 접근할 수 있도록 허용
    - Access-Control-Allow-Methods : preflight request에 대한 응답으로 실제 요청 중에 사용가능한 메소드
    - Access-Control-Allow-Headers : preflight request에 대한 응답으로 실제 요청 중에 사용가능한 헤더 필드 이름
    - Access-Control-Allow-Credentials : 실제 요청 쿠키나 인증 등의 사용자 자격 증명이 포함될 수 있음을 나타낸다
    - Access-Control-Allow-Max-Age : preflight 요청 결과를 캐시할 수 있는 시간(해당 시간동안 preflight reques X)

  - 스프링 시큐리티의 CORS 처리 과정
    - CorsConfigurer 
      - 스프링 시큐리티 필터체인에 CorsFilter 추가
      - corsFilter 라는 이름의 Bean이 제공되면 해당 CorsFilter 사용
      - corsFilter 대신 CorsConfigurationSource 빈이 정의된 경우 해당 CorsConfiguration 사용
      - CorsConfigurationSource 빈이 정의되어 있지 않으면 Spring MVS가 클래스 경로에 있으면 HandlerMappingIntrospector가 사용된다
    - CorsFilter
      - CORS 예비 요청을 처리하고 CORS 단순 및 본 요청을 가로채고, 제공된 CorsConfigurationSource를 통해 일치된 정핵에 따라 CORS 응답 헤더와 같은 응답을 업데이트 하기 위한 필터

  - 참고 : [Cors 이해](https://www.notion.so/hgene/Oauth2-040722d68b4d4232aafba193890ce27f)
---------------------------------------------------
