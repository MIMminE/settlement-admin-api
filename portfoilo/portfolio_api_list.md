# Settlement Service — API & Scenarios (Portfolio)

---

## 요약

이 문서는 Settlement 백오피스의 핵심 관리자 API를 간결히 정리하고, 포트폴리오·시연 중심의 시나리오(목적·흐름·의의·시연 포인트)를 상세히 기술합니다.

- 프로젝트: Settlement 백오피스(관리자용) — 일별 정산 집계·승인·재실행 및 운영 리포트(CSV) 제공
- 역할: 백엔드(도메인 모델링, API 설계, JWT 인증, 배치 재실행, CSV export)
- 핵심 흐름: 로그인 → 정산 조회(기간/판매자 필터) → 정산 승인 → CSV 다운로드
- 주요 엔드포인트: POST /auth/login, GET /admin/settlements, POST /admin/settlements/confirm, POST
  /admin/settlements/rerun, GET /admin/settlements/{id}/export.csv
- 기술 스택: Java, Spring Boot, Spring Security (JWT), Spring Data JPA, Spring Batch, Flyway, Docker

---

## 빠른 시작

1. 관리자 등록: POST /auth/register — 관리자 계정 생성(개발용)
2. 로그인: POST /auth/login → accessToken 수신
3. 보호된 관리자 API 호출: Authorization: Bearer <token>

---

## Endpoints

- POST /auth/register — 관리를 등록합니다.
- POST /auth/login — 로그인 후 JWT(accessToken)를 발급합니다.
- GET /admin/settlements — 정산 요약(검색/페이징)을 조회합니다.
- GET /admin/settlements/{id} — 특정 정산의 기본 메타 정보를 조회합니다.
- GET /admin/settlements/{id}/items — 정산에 포함된 항목(주문/환불)을 조회합니다.
- POST /admin/settlements/confirm — 계산된 정산을 승인합니다.
- POST /admin/settlements/rerun — 특정 판매자·날짜에 대해 정산을 재실행합니다.
- GET /admin/settlements/{id}/export.csv — 정산 결과를 CSV로 다운로드합니다.
- CRUD (관리자용): /admin/sellers, /admin/orders, /admin/refunds — 관리·테스트용 엔드포인트

간단 로그인 예시

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin1","password":"password1"}'
# 응답: {"accessToken":"eyJ..."}
```

---

## Scenarios — 시나리오 중심 설명 및 포트폴리오 포인트

아래 시나리오들은 포트폴리오 발표에서 실제로 시연하기 좋은 순서와 설명 포인트를 담고 있습니다.

### 시나리오 1 — 운영자 로그인 → 정산 목록 조회 → 정산 승인

- 목적: 관리자 인증, 검색(필터링·페이징), 승인 액션의 전체 플로우를 보여주기 위함.
- 흐름:
    1. 관리자 로그인으로 JWT 수신
    2. 정산 요약 조회(기간/판매자 필터)로 대상 정산 식별
    3. 특정 정산 선택 후 /admin/settlements/confirm 호출로 승인
- 기대 결과: 승인된 정산은 상태가 CONFIRMED로 변경되고, confirmedBy/confirmedAt이 기록됩니다.
- 기술적 의의:
    - Stateless JWT 인증 적용으로 운영 확장성과 세션 오버헤드 감소
    - 도메인 모델(Allocation: Settlement/SettlementItem)로 합계 계산 책임을 분리
    - 승인 로그(Audit)로 변경 내역 추적 및 운영 신뢰성 확보
- 검증 포인트:
    - 승인 전/후의 상태(status, confirmedBy, confirmedAt)과 관련 로그를 확인
    - 승인 처리 후 생성되는 감사 레코드의 존재 및 정확성 확인

### 시나리오 2 — 주문·환불 생성 → 정산 재실행(재계산) → 결과 검증

- 목적: 데이터 변경이 정산 결과에 미치는 영향을 시연하고 재실행 idempotency/버전 관리를 강조.
- 흐름:
    1. 테스트 판매자에 주문 생성(POST /admin/orders)
    2. 필요 시 환불 생성(POST /admin/refunds)
    3. /admin/settlements/rerun 호출로 해당 판매자·날짜 정산 재실행
    4. 재계산 결과 확인(정산 요약·상세·items) 및 CSV 다운로드
- 기대 결과: 재계산 결과는 새로운 버전으로 생성되거나 기존 버전이 업데이트되어 금액이 반영됩니다.
- 기술적 의의:
    - 재실행 시 버전 관리를 통해 idempotency 보장 및 중복 집계 방지
    - 배치의 부분 실패 허용 설계로 전체 작업의 가용성 유지
- 검증 포인트:
    - 재실행 전/후의 금액 합계(총액/환불/수수료/순액)과 version 증가 여부 검증
    - 실패 케이스가 AuditLog에 기록되고 다른 판매자는 정상 처리되는지 확인

### 시나리오 3 — 정산 CSV 내보내기(운영 보고용) 및 데이터 소비

- 목적: 운영자/재무가 활용할 수 있는 CSV 추출과 포맷을 시연.
- 흐름:
    1. 특정 정산 선택
    2. /admin/settlements/{id}/export.csv 호출로 CSV 다운로드
    3. CSV를 열어 요약 행(정산 메타)과 항목(rows)을 검토
- 기대 결과: CSV에 정산 요약과 항목이 포함되어 엑셀 리포트나 외부 시스템과 연동 가능
- 기술적 의의:
    - CSV 추출로 운영·재무팀의 데이터 소비를 직접 지원
    - 대용량 처리 시 스트리밍 방식으로 확장 가능하도록 설계 고려
- 검증 포인트:
    - CSV 헤더·요약·항목 수가 예상치와 일치하는지 확인
    - CSV 파일의 데이터 무결성을 엑셀 등으로 검증

### 시나리오 4 — 관리자·판매자 관리 및 권한 검토(보안 포인트)

- 목적: 관리 UI 호출과 권한(인증 흐름) 설계를 설명하고 보안 요소를 부각.
- 흐름:
    1. 관리자 등록/로그인
    2. /admin/sellers로 판매자 등록/조회
    3. 권한이 없는 요청(토큰 없음 또는 잘못된 토큰) 시 401 응답 시연
- 기대 결과: 관리자 기능 접근 및 데이터 조작에 대한 권한 통제가 적절히 이루어짐.
- 기술적 의의:
    - BCrypt 기반 비밀번호 해시 적용으로 인증 보안 확보
    - 초기 계정 자동 생성으로 데모 재현성 제공
    - 토큰 권한 검증 및 시크릿 관리 개선은 향후 보안 강화 대상
- 검증 포인트:
    - 인증되지 않은 요청 또는 잘못된 토큰에 대해 401 응답을 확인
    - AdminUser 테이블에 저장된 비밀번호가 해시인지 확인
