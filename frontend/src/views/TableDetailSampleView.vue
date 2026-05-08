<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import {
  getSampleColumns,
  initialEntity,
  mountCommonStyle,
  unmountCommonStyle,
  type ColumnInfo,
} from "./TableDetailSampleView";

// 화면 상태 연결 영역
// 기준: .vue에는 템플릿이 직접 참조하는 반응형 상태와 이벤트 연결만 남긴다.
const entity = ref({ ...initialEntity });

const columns = ref<ColumnInfo[]>([]);
const dateRange = ref("");
const columnType = ref("");
const searchKeyword = ref("");
const searchText = ref("");

const filteredColumns = computed(() => {
  const keyword = searchText.value.trim().toLowerCase();
  if (!keyword) {
    return columns.value;
  }

  return columns.value.filter((col) =>
    [
      col.physicalName,
      col.logicalName,
      col.dataType,
      col.mappedTerm ?? "",
      col.isPk ? "pk" : "",
      col.isNullable ? "nullable y" : "not null n",
    ].some((value) => value.toLowerCase().includes(keyword)),
  );
});

// 화면 이벤트 영역
// 기준: 버튼 클릭처럼 템플릿 이벤트에 직접 연결되는 함수는 .vue에 두고, 데이터 생성은 .ts로 위임한다.
function fetchData() {
  console.log("OCI 카탈로그에서 메타데이터를 다시 수집합니다...");
  columns.value = getSampleColumns();
}

function requestAccess() {
  alert(`${entity.value.logicalName}에 대한 데이터 사용 권한 결재를 기안합니다.`);
}

function viewDataProfile(col: ColumnInfo) {
  alert(`${col.logicalName}의 데이터 분포도(Data Profile) 팝업을 엽니다.`);
}

function search() {
  searchText.value = [columnType.value, searchKeyword.value].filter(Boolean).join(" ");
}

onMounted(() => {
  mountCommonStyle();
  fetchData();
});

onUnmounted(() => {
  unmountCommonStyle();
});
</script>

<template>
  <main class="table-detail-sample">
    <div class="sys-container">
      <div class="header-action">
        <h2>{{ entity.logicalName }} ({{ entity.physicalName }}) 메타정보</h2>
        <div class="button-group">
          <button class="btn btn-outline" type="button" @click="fetchData">새로고침</button>
          <button class="btn btn-primary" type="button" @click="requestAccess">데이터 접근 권한 신청</button>
        </div>
      </div>

      <div class="grid-2">
        <div class="sys-card">
          <div class="sys-card-title">테이블 기본 정보</div>
          <ul class="meta-info-list">
            <li><span class="label">자산 ID</span> {{ entity.assetId }}</li>
            <li><span class="label">소유 부서</span> {{ entity.ownerDept }}</li>
            <li><span class="label">데이터 건수</span> {{ entity.rowCount.toLocaleString() }} 건</li>
            <li><span class="label">최근 수집일시</span> {{ entity.lastHarvestDate }}</li>
          </ul>
        </div>

        <div class="sys-card">
          <div class="sys-card-title">거버넌스 및 보안</div>
          <ul class="meta-info-list">
            <li>
              <span class="label">보안 등급</span>
              <strong :style="{ color: entity.securityLevel === '1등급' ? 'var(--color-danger)' : 'var(--color-text-main)' }">
                {{ entity.securityLevel }}
              </strong>
            </li>
            <li><span class="label">개인정보(PII)</span> {{ entity.hasPii ? "포함 (마스킹 필요)" : "미포함" }}</li>
            <li><span class="label">AI 분석 여부</span> {{ entity.isAiAnalyzed ? "Y (OCR 추출됨)" : "N" }}</li>
            <li><span class="label">품질 점수</span> {{ entity.qualityScore }} / 100</li>
          </ul>
        </div>
      </div>

      <div class="sys-card">
        <div class="table-toolbar">
          <div class="toolbar-left">
            <label class="condition-field">
              <span class="condition-label">날짜범위</span>
              <input v-model="dateRange" type="text" placeholder="YYYY-MM-DD ~ YYYY-MM-DD" />
            </label>
            <label class="condition-field">
              <span class="condition-label">유형</span>
              <select v-model="columnType">
                <option value="">전체</option>
                <option value="VARCHAR2">VARCHAR2</option>
                <option value="TIMESTAMP">TIMESTAMP</option>
                <option value="PK">PK</option>
              </select>
            </label>
            <label class="condition-field keyword-field">
              <span class="condition-label">검색어</span>
              <input
                v-model="searchKeyword"
                type="search"
                placeholder="물리명, 논리명, 타입, 표준 용어"
                @keyup.enter="search"
              />
            </label>
          </div>
          <button class="btn btn-primary" type="button" @click="search">조회</button>
        </div>
        <div class="toolbar-count">전체 {{ columns.length.toLocaleString() }}건</div>

        <table class="sys-table">
          <thead>
            <tr>
              <th class="text-center table-number">No</th>
              <th>물리 컬럼명</th>
              <th>논리 컬럼명</th>
              <th>데이터 타입</th>
              <th class="text-center">PK 여부</th>
              <th class="text-center">Null 허용</th>
              <th>표준 용어 매핑</th>
              <th class="text-center">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(col, index) in filteredColumns" :key="col.physicalName">
              <td class="text-center">{{ index + 1 }}</td>
              <td><strong>{{ col.physicalName }}</strong></td>
              <td>{{ col.logicalName }}</td>
              <td>{{ col.dataType }}</td>
              <td class="text-center">{{ col.isPk ? "PK" : "-" }}</td>
              <td class="text-center">{{ col.isNullable ? "Y" : "N" }}</td>
              <td>
                <span v-if="col.mappedTerm" class="mapped-term">
                  {{ col.mappedTerm }}
                </span>
                <span v-else class="unmapped-term">미매핑</span>
              </td>
              <td class="text-center">
                <button class="btn btn-outline btn-sm" type="button" @click="viewDataProfile(col)">프로파일링</button>
              </td>
            </tr>

            <tr v-if="filteredColumns.length === 0">
              <td colspan="8" class="text-center empty-cell">
                검색 조건에 맞는 컬럼 정보가 없습니다.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>

<style scoped>
.table-detail-sample {
  min-height: calc(100vh - 61px);
  background: var(--color-bg-body, #f4f6f8);
}

.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.meta-info-list {
  list-style: none;
  padding: 0;
  line-height: 2;
}

.meta-info-list li span.label {
  display: inline-block;
  width: 120px;
  color: var(--color-text-sub);
  font-weight: 500;
}

.table-number {
  width: 50px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 14px;
}

.toolbar-count {
  margin: -8px 0 12px;
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 600;
}

.toolbar-left {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.condition-field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.condition-label {
  color: var(--color-text-sub);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.condition-field input,
.condition-field select {
  width: 190px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 0 12px;
  color: var(--color-text-main);
  font-size: 13px;
  outline: none;
  background: #fff;
}

.keyword-field input {
  width: min(320px, 38vw);
}

.condition-field input:focus,
.condition-field select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.mapped-term {
  color: var(--color-secondary);
  font-weight: 500;
}

.unmapped-term {
  color: var(--color-text-light);
}

.empty-cell {
  padding: 30px;
}

@media (max-width: 900px) {
  .header-action {
    align-items: flex-start;
    flex-direction: column;
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }

  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left {
    width: 100%;
    align-items: flex-start;
    flex-direction: column;
  }

  .condition-field,
  .condition-field input,
  .condition-field select,
  .keyword-field input {
    width: 100%;
  }
}
</style>
