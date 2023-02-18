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
  - 참고 : https://www.notion.so/hgene/Oauth2-792e15aa883746bc9d8296a8518184d6


- 자동설정에 의한 초기화 진행
- 커스텀 설정에 의한 초기화 진행
- AuthenticationEntryPoint 이해