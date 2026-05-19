const loginForm = document.querySelector("#loginForm");
const filterForm = document.querySelector("#filterForm");
const batchForm = document.querySelector("#batchForm");
const refreshButton = document.querySelector("#refreshButton");
const rows = document.querySelector("#settlementRows");
const itemList = document.querySelector("#itemList");
const activityLog = document.querySelector("#activityLog");
const sessionState = document.querySelector("#sessionState");
const settlementCount = document.querySelector("#settlementCount");
const netTotal = document.querySelector("#netTotal");
const pendingCount = document.querySelector("#pendingCount");

let token = localStorage.getItem("settlement-token") ?? "";
let settlements = [];

const currency = new Intl.NumberFormat("ko-KR", {
  style: "currency",
  currency: "KRW",
  maximumFractionDigits: 0
});

function formatDate(date) {
  return date.toISOString().slice(0, 10);
}

function setDefaultDates() {
  const today = new Date();
  const from = new Date(today);
  from.setDate(today.getDate() - 7);
  const settlementDate = new Date(today);
  settlementDate.setDate(today.getDate() - 1);

  filterForm.elements.from.value = formatDate(from);
  filterForm.elements.to.value = formatDate(today);
  batchForm.elements.settlementDate.value = formatDate(settlementDate);
}

function log(message, data) {
  const time = new Date().toLocaleTimeString("ko-KR", { hour12: false });
  const detail = data ? `\n${JSON.stringify(data, null, 2)}` : "";
  activityLog.textContent = `[${time}] ${message}${detail}\n\n${activityLog.textContent}`;
}

function updateSession() {
  sessionState.textContent = token ? "관리자 접속 중" : "로그인 필요";
  sessionState.classList.toggle("is-authenticated", Boolean(token));
  document.body.classList.toggle("is-authenticated", Boolean(token));
}

async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers ?? {})
  };

  const response = await fetch(url, {
    ...options,
    headers
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`${response.status} ${response.statusText}: ${body}`);
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) return response.json();
  return response.text();
}

function settlementAmount(value) {
  return Number(value ?? 0);
}

function settlementSellerId(settlement) {
  return settlement.sellerId ?? settlement.seller?.id ?? "-";
}

function settlementIdOf(settlement) {
  return settlement.settlementId ?? settlement.id;
}

function renderMetrics() {
  const total = settlements.reduce((sum, settlement) => sum + settlementAmount(settlement.netAmount), 0);
  const pending = settlements.filter((settlement) => settlement.status !== "CONFIRMED").length;
  settlementCount.textContent = String(settlements.length);
  netTotal.textContent = currency.format(total);
  pendingCount.textContent = String(pending);
}

function renderRows() {
  rows.innerHTML = "";

  if (settlements.length === 0) {
    rows.innerHTML = `
      <tr>
        <td colspan="7"><div class="empty">조회된 정산이 없습니다.</div></td>
      </tr>
    `;
    renderMetrics();
    return;
  }

  for (const settlement of settlements) {
    const settlementId = settlementIdOf(settlement);
    const row = document.createElement("tr");
    const statusClass = settlement.status === "CONFIRMED" ? "confirmed" : "";
    row.innerHTML = `
      <td>${settlementId}</td>
      <td>${settlementSellerId(settlement)}</td>
      <td>${settlement.settlementDate}</td>
      <td><span class="status-pill ${statusClass}">${settlement.status}</span></td>
      <td><strong>${currency.format(settlementAmount(settlement.netAmount))}</strong></td>
      <td>v${settlement.version}</td>
      <td>
        <div class="row-actions">
          <button class="row-button" type="button" data-action="items" data-id="${settlementId}">항목</button>
          <button class="row-button" type="button" data-action="confirm" data-id="${settlementId}">확정</button>
          <button class="row-button" type="button" data-action="csv" data-id="${settlementId}">CSV</button>
        </div>
      </td>
    `;
    rows.append(row);
  }

  renderMetrics();
}

function readFilters() {
  const sellerId = filterForm.elements.sellerId.value.trim();
  const params = new URLSearchParams({
    from: filterForm.elements.from.value,
    to: filterForm.elements.to.value,
    page: "0",
    size: "20"
  });
  if (sellerId) params.set("sellerId", sellerId);
  return params;
}

async function loadSettlements() {
  const data = await request(`/admin/settlements?${readFilters().toString()}`);
  settlements = data.content ?? [];
  renderRows();
  log("정산 목록 조회", { count: settlements.length, totalElements: data.totalElements ?? settlements.length });
}

async function loadItems(settlementId) {
  const items = await request(`/admin/settlements/${settlementId}/items`);
  itemList.innerHTML = "";

  if (items.length === 0) {
    itemList.innerHTML = `<div class="empty">정산 항목이 없습니다.</div>`;
    return;
  }

  for (const item of items) {
    const row = document.createElement("div");
    row.className = "item-row";
    row.innerHTML = `
      <strong>${item.type} · ${currency.format(settlementAmount(item.amount))}</strong>
      <span>orderId ${item.orderId ?? "-"} · refundId ${item.refundId ?? "-"}</span>
    `;
    itemList.append(row);
  }

  log("정산 항목 조회", { settlementId, count: items.length });
}

async function confirmSettlement(settlementId) {
  const data = await request("/admin/settlements/confirm", {
    method: "POST",
    body: JSON.stringify({ settlementId: Number(settlementId) })
  });
  log("정산 확정", { settlementId: data.id ?? settlementId, status: data.status });
  await loadSettlements();
}

async function runBatch() {
  const settlementDate = batchForm.elements.settlementDate.value;
  const data = await request(`/admin/batch/daily-settlement?settlementDate=${encodeURIComponent(settlementDate)}`, {
    method: "POST",
    headers: {}
  });
  log("일별 정산 배치 실행", data);
  await loadSettlements();
}

async function downloadCsv(settlementId) {
  const response = await fetch(`/admin/settlements/${settlementId}/export.csv`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`${response.status} ${response.statusText}: ${body}`);
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = `settlement-${settlementId}.csv`;
  anchor.click();
  URL.revokeObjectURL(url);
  log("CSV 다운로드 요청", { settlementId });
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);

  try {
    const data = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({
        username: form.get("username"),
        password: form.get("password")
      })
    });
    token = data.accessToken;
    localStorage.setItem("settlement-token", token);
    updateSession();
    log("관리자 로그인 완료", { username: form.get("username") });
    await loadSettlements();
  } catch (error) {
    log("로그인 실패", { error: error.message });
  }
});

filterForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await loadSettlements();
  } catch (error) {
    log("정산 목록 조회 실패", { error: error.message });
  }
});

batchForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await runBatch();
  } catch (error) {
    log("배치 실행 실패", { error: error.message });
  }
});

refreshButton.addEventListener("click", async () => {
  try {
    await loadSettlements();
  } catch (error) {
    log("새로고침 실패", { error: error.message });
  }
});

rows.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;

  const action = button.dataset.action;
  const id = button.dataset.id;

  try {
    if (action === "items") await loadItems(id);
    if (action === "confirm") await confirmSettlement(id);
    if (action === "csv") await downloadCsv(id);
  } catch (error) {
    log("행 작업 실패", { action, id, error: error.message });
  }
});

setDefaultDates();
updateSession();
renderRows();

if (token) {
  loadSettlements().catch((error) => {
    log("초기 정산 조회 실패", { error: error.message });
  });
}
