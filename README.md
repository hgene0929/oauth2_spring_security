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

