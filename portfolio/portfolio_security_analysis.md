# Security 적용 분석

프로젝트 내 기술적 가치
-----------

- 설계·구현: JWT 기반 무상태 인증을 도입해 확장성과 보안성을 확보했습니다. Spring Security FilterChain에 커스텀 JWT 필터를 삽입하고, 비밀번호는 BCrypt로 해시하여 안전하게
  저장했습니다.
- 운영·검증: 정산 시스템의 관리자 API 접근 제어와 감사(audit) 기록을 통해 운영 신뢰성을 제공하며, OpenAPI/Swagger 기반 문서화로 수동 검증을 지원합니다.

핵심 파일
---------
`SecurityConfig.java`, `AuthController.java`, `AdminUser.java`, `AdminUserRepository.java`, `DataInitializer.java`,
`jwt/JwtTokenProvider.java`, `jwt/JwtAuthenticationFilter.java`, `OpenApiConfig.java`

핵심 파일 상세
-----------------

`SecurityConfig`

```
http.csrf().disable();
http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
http.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**").permitAll().anyRequest().authenticated());
http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
return http.build();
```

- Stateless JWT 기반 인증을 적용하고, 인증 필터를 SecurityFilterChain에 등록해 모든 요청에서 토큰을 검증하도록 구성했습니다.

검증 방법:

- 보호된 엔드포인트에 인증 없이 접근 시 401 응답 확인
- 테스트에서 SecurityContext에 인증 정보가 설정되는지 확인

`AuthController`
--------------

```
String hash = passwordEncoder.encode(req.password());
adminUserRepository.save(new AdminUser(req.username(), hash));

AdminUser user = adminUserRepository.findByUsername(req.username()).orElseThrow();
if (passwordEncoder.matches(req.password(), user.getPasswordHash()))
    return tokenProvider.createAccessToken(user.getUsername());
```

- 관리자 등록은 BCrypt 해시로 비밀번호를 저장하고, 로그인 성공 시 JWT를 발급합니다.

검증 방법:

- DB에 저장된 passwordHash가 평문이 아닌지 확인
- 발급된 토큰을 파싱하여 subject가 올바른지 확인

`JwtTokenProvider`
----------------

```
String token = Jwts.builder()
    .issuer(issuer)
    .subject(username)
    .issuedAt(now)
    .expiration(exp)
    .claim("role","ADMIN")
    .signWith(key)
    .compact();
String username = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
```

- HMAC 서명을 사용한 JWT 발급 및 파싱을 통해 토큰 위·변조를 방지하고, 간단한 클레임 기반 권한 확장 준비를 했습니다.

검증 방법:

- 서명이 변조된 토큰으로 접근 시 파싱 실패로 거부되는지 확인
- 토큰 만료 시 401 응답 확인

`JwtAuthenticationFilter`
-----------------------

```
String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
if (auth != null && auth.startsWith("Bearer ")) {
    String token = auth.substring(7);
    String username = tokenProvider.parseUsername(token);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
}
filterChain.doFilter(request, response);
```

- 요청의 Authorization 헤더에서 Bearer 토큰을 추출해 파싱한 후 SecurityContext에 인증 정보를 설정합니다. 현재는 ADMIN 권한을 부여합니다.

검증 방법:

- 유효 토큰으로 보호 API 호출 시 인증 통과 여부 확인
- 토큰의 role(claim) 기반 권한 매핑 필요성 검토

`AdminUser / AdminUserRepository`
-------------------------------

```
@Column(nullable = false, length = 80)
private String username;

@Column(name = "password_hash", nullable = false, length = 200)
private String passwordHash;
```

- 관리자 계정의 최소 필드를 저장하며 username에 유니크 제약을 적용했습니다.

검증 방법:

- `findByUsername`로 사용자 조회 시 올바른 엔티티 반환 확인

`DataInitializer`
---------------

```
@EventListener(ApplicationReadyEvent.class)
public void init() {
    createIfNotExists("admin1", "password1");
}
```

- 개발/시연 환경에서 기본 관리자 계정을 자동 생성해 재현성을 확보하도록 구성했습니다.

검증 방법:

- 애플리케이션 시작 후 DB에 초기 계정 존재 여부 확인

`OpenApiConfig`
-------------

```
new Components().addSecuritySchemes("bearerAuth",
    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
```

- OpenAPI에 Bearer JWT 스키마를 등록해 Swagger UI에서 토큰 입력을 통한 수동 테스트를 지원합니다.

검증 방법:

- Swagger UI에서 토큰 입력 후 보호 API 호출 가능 여부 확인

인증 흐름 요약
--------------

1. `POST /auth/register` — PasswordEncoder로 비밀번호 해시 저장
2. `POST /auth/login` — 인증 성공 시 JwtTokenProvider로 액세스 토큰 발급
3. 보호된 엔드포인트 요청 시 JwtAuthenticationFilter가 토큰을 파싱해 SecurityContext에 인증 정보를 설정

추후 개선 사항
-------------------

- 토큰의 role 클레임을 GrantedAuthority로 매핑하도록 JwtAuthenticationFilter 개선
- `app.jwt.secret`을 환경변수 또는 비밀관리 시스템으로 이전
- 인증/인가 실패에 대한 표준화된 JSON 응답 포맷 도입
- DataInitializer를 개발 전용 프로파일로 제한
- Refresh token 또는 토큰 무효화(블랙리스트) 전략 검토
