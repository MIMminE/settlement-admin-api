Settlement Service
==================

개요
----
이 프로젝트는 스프링 부트 기반의 일일 정산(日次精算) 자동화 및 판매자(merchant) 백오피스 API를 구현한 포트폴리오용 애플리케이션입니다. 핵심 목표는 실제 운영에서 필요한 정산(정기 계산 →
확인/승인), 감사(audit), 관리자 인증/권한, 그리고 배치 자동화(스프링 배치)를 간단하면서도 현실감 있게 보여주는 것입니다.

주요 기능
---------

- 판매자(Seller) / 주문(Orders) / 환불(Refund) 모델링
- 일별 정산(Settlement) 배치(job) — `dailySettlementJob` (스케줄 또는 수동 실행 가능)
- 정산 항목(SettlementItem) 관리 및 금액 계산 로직
- 관리자용 인증: JWT 기반 로그인 (`/auth/register`, `/auth/login`)
- 감사 로그(AuditLog) 기록
- Flyway 기반 DB 마이그레이션
- 컨테이너화: Docker + Docker Compose (Postgres 포함)

인프라 구성
------------

- 데이터베이스: PostgreSQL (docker-compose.yml에 `postgres:16` 사용)
- 애플리케이션: Spring Boot 4.0.3 기반 (프로젝트 루트의 Gradle Wrapper 사용)
- 마이그레이션: Flyway (마이그레이션 스크립트는 `src/main/resources/db/migration`)

프로젝트 구조(간략)
------------------

- src/main/java : 애플리케이션 코드
    - domain.model: 엔티티(예: Settlement, SettlementItem, AuditLog, AdminUser 등)
    - domain.batch: 스프링 배치 설정/Tasklet (DailySettlementTasklet 등)
    - security: JWT 관련, `AuthController`, `AdminUserRepository`, `SecurityConfig`, `DataInitializer`
- src/main/resources/db/migration: Flyway SQL 스크립트 (V1..V4 등)
- docker-compose.yml: Postgres와 앱(도커) 구성

중요한 설정 및 실행 프로파일
-------------------------

- 프로파일
    - `dev` : 로컬 개발 (DB는 도커에서 띄운 Postgres를 사용)
    - `docker` : 앱과 DB를 모두 Docker Compose로 띄움

- JWT 시크릿
    - 프로퍼티: `app.jwt.secret` (application*.yml에 기본값이 들어있습니다)
    - 운영/포트폴리오 전시시에는 환경변수로 오버라이드하세요: `APP_JWT_SECRET` (권장)

초기 관리자 계정 (개발용)
-----------------------
앱 시작 시 자동으로 생성되는 관리자 계정 두 개(개발 편의):

- admin1 / password1
- admin2 / password2

배치(job) 실행
---------------
`dailySettlementJob`은 `DailySettlementTasklet`로 구현되어 있으며, 기본 동작은 다음과 같습니다:

- JobParameter `settlementDate`를 사용해 해당 날짜의 모든 판매자에 대해 정산을 계산합니다.
- 만약 `settlementDate`가 주어지지 않으면, 어제 날짜를 기본으로 사용하도록 구현되어 있습니다.

수동/CLI로 배치 실행 예시:

- Gradle bootRun으로 실행하면서 파라미터 전달

```powershell
.\gradlew.bat bootRun --args='--spring.batch.job.names=dailySettlementJob --spring.batch.job.parameters=settlementDate=2026-03-01'
```

API 엔드포인트(주요)
-------------------

- POST /auth/register — 관리자 등록 (입력: username, password)
- POST /auth/login — 로그인 (입력: username, password) → 반환: accessToken (JWT)

애플리케이션 로컬 실행 (Docker Compose)
------------------------------------

- Docker Compose가 Postgres와 앱을 띄우도록 구성되어 있습니다. 앱 이미지는 로컬에서 빌드됩니다(Gradle Wrapper 사용).

```powershell
# 빌드 (Windows PowerShell)
.\gradlew.bat clean build -x test
# 도커 컴포즈로 띄우기
docker-compose up --build
```

정산 관련 시나리오
------------------------------------------------

1. 일별 정산 생성 시나리오
    - 입력: settlementDate
    - 동작: 각 판매자별 주문/환불을 집계하여 Settlement, SettlementItem 생성
    - 결과: Settlement 상태는 CALCULATED, 총액/수수료/환불/순액 계산, 감사 로그 작성

2. 정산 확인/승인 시나리오
    - 관리자(Admin)는 계산된 정산을 확인하고 승인
    - 동작: Settlement.confirm(adminUsername) 호출 → 상태 변경(CONFIRMED), confirmedAt/confirmedBy 저장
    - 결과: 승인된 정산은 지급 처리 또는 보고에 사용됨

3. 재계산/대체 시나리오
    - 이미 계산된 정산이 있을 때 재계산이 필요한 경우 새로운 버전을 생성하거나 기존을 supersede
    - 동작: version 필드 사용하여 버전 관리(유니크 제약으로 관리)

4. 실패/예외 처리 시나리오
    - 특정 판매자에 대해 계산 중 예외 발생 → 실패 로그는 AuditLog에 기록되고 해당 판매자만 스킵
    - 전체 배치 실패 방지(개별 처리로 복구 가능하도록 설계)