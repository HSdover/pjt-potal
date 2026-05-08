# HIRA Raw Data

## Downloaded Dataset

- Dataset: 건강보험심사평가원_요양기관 개설 현황_20251231
- Source: https://www.data.go.kr/data/15051057/fileData.do
- Download URL:
  `https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000003601192&fileDetailSn=1&insertDataPrcus=N`
- Raw file:
  `hira_institution_open_status_2025_12_31.csv`
- Encoding:
  `CP949`
- Rows:
  `104,775`
- Columns:
  `암호화된요양기호`, `요양기관명`, `요양종별`, `시도명`, `시군구명`, `도로명주소`, `표시과목명`, `개설일자`

## Notes

The larger HIRA "전국 병의원 및 약국 현황" ZIP exists on the HIRA open data site, but its download is handled by the DEXT5 upload/download component. The simpler public CSV from data.go.kr was downloaded first because it is directly usable for local governance portal testing.
