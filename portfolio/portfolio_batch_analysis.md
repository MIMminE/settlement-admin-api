# Batch 적용 분석

프로젝트 내 기술적 가치
-----------

- 설계·구현: Spring Batch를 사용해 대용량의 정산 작업(주기적/일괄 처리)을 안정적으로 처리하도록 설계했습니다. Chunk 기반 처리와 트랜잭션 경계 설정으로 실패 시 재시도/재시작이 가능하며,
  ItemReader/Processor/Writer 패턴을 통해 비즈니스 로직을 모듈화했습니다.
- 운영·검증: Job/Step 실행 상태는 Spring Batch 메타테이블에 기록되어 재시작 및 모니터링이 가능하고, Spring Boot Actuator와 결합해 운영 지표와 헬스체크를 제공합니다.

핵심 파일
-------------
`DailySettlementJobConfig.java`
`DailySettlementTasklet.java`
`BatchController.java`
`SettlementCommandService`

핵심 파일 상세
-------------

`DailySettlementJobConfig.java`

```
@Bean
public Job dailySettlementJob(JobRepository jobRepository, Step dailySettlementStep) {
    return new JobBuilder("dailySettlementJob", jobRepository)
            .start(dailySettlementStep)
            .build();
}
```

- `dailySettlementJob`을 JobRepository 기반으로 선언하고, `dailySettlementStep`을 시작점으로 구성합니다. `@EnableBatchProcessing`으로 Spring
  Batch 인프라가 자동 구성됩니다.

검증 방법:

- 애플리케이션 실행 후 `BATCH_JOB_INSTANCE`에 `dailySettlementJob`이 등록되는지 확인

`DailySettlementTasklet.java`

```
String dateStr = (String) chunkContext.getStepContext().getJobParameters().get("settlementDate");
LocalDate date = (dateStr == null || dateStr.isBlank()) ? LocalDate.now().minusDays(1) : LocalDate.parse(dateStr);

sellerRepository.findAll().forEach(seller -> {
    boolean exists = settlementRepository.findTopBySellerIdAndSettlementDateOrderByVersionDesc(seller.getId(), date).isPresent();
    if (!exists) settlementCommandService.calculateDaily(seller.getId(), date, 1, "batch");
});
```

- JobParameter인 `settlementDate`를 파싱해 대상 날짜를 결정한 뒤, 판매자들을 순회하며 기존 정산 존재 여부를 체크하고 없다면
  `SettlementCommandService.calculateDaily`로 정산을 수행합니다. 예외 발생 시 Audit 기록을 남깁니다.

검증 방법:

- 소량의 테스트 데이터로 Tasklet 실행 후 `Settlement`와 `SettlementItem`이 생성되는지 확인
- 실패 케이스에서 AuditService 호출 여부 확인

`BatchController.java`

```
JobParameters jobParameters = new JobParametersBuilder()
        .addString("settlementDate", date.toString())
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters();

JobExecution execution = jobOperator.start(dailySettlementJob, jobParameters);
```

- 관리자 엔드포인트(`/admin/batch/daily-settlement`)에서 `JobOperator`로 `dailySettlementJob`을 수동 트리거합니다. `run.id`로 유니크 파라미터를 생성해
  중복 실행 문제를 방지합니다.

검증 방법:

- API 호출로 Job이 시작되는지와 반환된 `JobExecution` ID를 통해 실행 이력을 조회

`SettlementCommandService.java`

```
List<Order> orders = orderRepository.findBySellerIdAndPaidAtBetween(sellerId, from, to);
List<Refund> refunds = refundRepository.findBySellerIdAndRefundedAtBetween(sellerId, from, to);

Settlement settlement = new Settlement(seller, date, version);
settlementRepository.save(settlement);
```

- 주어진 판매자와 날짜 범위에서 주문·환불을 조회해 `Settlement` 및 `SettlementItem`을 생성·저장하고, 수수료 계산 후 `markCalculated`로 상태를 반영합니다. 모든 변경은
  `@Transactional`로 묶여 원자성을 보장합니다.

검증 방법:

- 다양한 주문/환불 케이스로 `calculateDaily`의 결과(총액, 환불, 수수료, 순수액)가 기대값과 일치하는지 단위/통합 테스트

`SettlementRepository.java`

```
Optional<Settlement> findTopBySellerIdAndSettlementDateOrderByVersionDesc(Long sellerId, LocalDate settlementDate);

List<Settlement> findBySettlementDateAndStatus(LocalDate settlementDate, SettlementStatus status);
```

- 정산의 최신 버전 조회와 날짜별 상태 조회 등 배치 프로세스에서 필요한 쿼리를 제공하는 JpaRepository 확장입니다.

검증 방법:

- 테스트 DB에서 각 쿼리가 올바른 레코드를 반환하는지 검증

`SellerRepository.java`

```
public interface SellerRepository extends JpaRepository<Seller, Long> {}
```

- 판매자 목록 조회를 단순화하기 위한 JpaRepository입니다. Tasklet에서 `findAll()`로 모든 판매자를 순회합니다.

검증 방법:

- 테스트 데이터로 `findAll()` 반환값을 확인

배치 처리 흐름
--------------

1. 대상 추출(Reader): 미처리 주문을 조회
2. 변환(Processor): 수수료·정책 적용, 예외 판정
3. 적재(Writer): 정산 결과를 일괄 저장
4. 후처리(Listener): 집계, 알림, Job 상태 정리

추후 개선 사항
-------------

- 확장성: 대용량 처리 시 파티셔닝(partitioning)이나 병렬 Step을 도입해 처리량을 확보
- 안정성: 재시작성(idempotency) 보장, 실패 시 재시도/스킵 정책(tolerant skip/retry) 적용
- 운영: Job 모니터링 대시보드(예: Spring Batch Admin 또는 커스텀 UI)와 알림(실패 시 슬랙/메일) 연동
- 보안: 민감 데이터(예: 결제 정보)는 마스킹·암호화하고, 배치에 사용하는 DB 계정의 권한을 최소화
